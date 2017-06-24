/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Paul Campbell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.kemitix.huntbugs.detect;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.ast.Expression;
import lombok.RequiredArgsConstructor;
import net.kemitix.huntbugs.cohesive.Analyser;
import net.kemitix.huntbugs.cohesive.AnalysisResult;
import net.kemitix.huntbugs.cohesive.BeanMethods;
import net.kemitix.huntbugs.cohesive.Component;
import net.kemitix.huntbugs.cohesive.MethodDefinitionWrapper;
import net.kemitix.huntbugs.cohesive.MethodSignature;
import net.kemitix.huntbugs.cohesive.TypeDefinitionWrapper;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;
import one.util.huntbugs.warning.Role;
import one.util.huntbugs.warning.Roles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Detects classes that are not cohesive.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@SuppressWarnings("classfanoutcomplexity")
@RequiredArgsConstructor
@WarningDefinition(category = "BadPractice", name = "CohesiveDetector", maxScore = CohesiveDetector.MAX_SCORE)
public class CohesiveDetector {

    static final int MAX_SCORE = 50;

    private static final Role.NumberRole COMPONENT_COUNT = Role.NumberRole.forName("COMPONENT_COUNT");

    private static final Role.StringRole COMPONENT_BREAKDOWN = Role.StringRole.forName("COMPONENT_BREAKDOWN");

    private final BeanMethods beanMethods;

    private final MethodSignature methodSignature;

    private final TypeDefinitionWrapper typeDefinitionWrapper;

    private final MethodDefinitionWrapper methodDefinitionWrapper;

    private final Analyser analyser;

    private final Set<String> nonPrivateMethodNames;

    private final Map<String, Set<String>> usedByMethod;

    /**
     * Default constructor.
     */
    public CohesiveDetector() {
        methodSignature = MethodSignature.defaultInstance();
        beanMethods = BeanMethods.defaultInstance(methodSignature);
        typeDefinitionWrapper = TypeDefinitionWrapper.defaultInstance();
        methodDefinitionWrapper = MethodDefinitionWrapper.defaultInstance();
        nonPrivateMethodNames = new HashSet<>();
        usedByMethod = new HashMap<>();
        analyser = Analyser.defaultInstance();
    }

    /**
     * Prepare to analyse the class.
     *
     * @param td the class
     */
    @ClassVisitor(order = VisitOrder.BEFORE)
    public void init(final TypeDefinition td) {
        final Set<String> fields = getDeclaredFieldNames(td);
        final Predicate<MethodDefinition> isNonBeanMethod =
                methodDefinition -> beanMethods.isNotBeanMethod(methodDefinition, fields);
        final Predicate<MethodDefinition> isNotConstructor =
                methodDefinition -> !methodDefinitionWrapper.isConstructor(methodDefinition);
        final Predicate<MethodDefinition> isNonPrivate =
                methodDefinition -> !methodDefinitionWrapper.isPrivate(methodDefinition);
        usedByMethod.clear();
        nonPrivateMethodNames.clear();
        nonPrivateMethodNames.addAll(getDeclaredMethods(td).stream()
                                                           .filter(isNotConstructor)
                                                           .filter(isNonPrivate)
                                                           .filter(isNonBeanMethod)
                                                           .map(this::createSignature)
                                                           .collect(Collectors.toSet()));
    }

    private List<MethodDefinition> getDeclaredMethods(final TypeDefinition td) {
        return typeDefinitionWrapper.getDeclaredMethods(td);
    }

    private Set<String> getDeclaredFieldNames(final TypeDefinition td) {
        return typeDefinitionWrapper.getDeclaredFields(td)
                                    .stream()
                                    .map(FieldDefinition::getName)
                                    .collect(Collectors.toSet());
    }

    /**
     * Analyse the results of scanning the class.
     *
     * @param td the class
     * @param cc the context for reporting errors
     */
    @ClassVisitor(order = VisitOrder.AFTER)
    public void analyse(final TypeDefinition td, final ClassContext cc) {
        final AnalysisResult analysisResult = analyser.analyse(usedByMethod, nonPrivateMethodNames);
        final Set<Component> components = analysisResult.getComponents();
        final int size = components.size();
        if (size > 1) {
            final String format = "Class consists of %d discrete components:%n%s";
            final String message = String.format(format, size, components.stream()
                                                                         .map(Object::toString)
                                                                         .collect(Collectors.joining(
                                                                                 System.lineSeparator())));
            cc.report("CohesiveDetector", 0, Roles.TYPE.create(td), COMPONENT_COUNT.create(size),
                      COMPONENT_BREAKDOWN.create(message)
                     );
        }
    }

    /**
     * Visitor for each expression within each method that records each field and method used.
     *
     * <p>n.b. excludes constructors</p>
     *
     * @param expression       the expression with
     * @param methodDefinition the method containing the expression
     *
     * @return false if the method is a constructor and should not be processed any further, true for other methods
     */
    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public final boolean visit(final Expression expression, final MethodDefinition methodDefinition) {
        if (methodDefinitionWrapper.isConstructor(methodDefinition)) {
            return false;
        }
        final Object operand = expression.getOperand();
        handleMethodReference(operand, methodDefinition);
        handleFieldReference(operand, methodDefinition);
        return true;
    }

    private void handleFieldReference(final Object operand, final MethodDefinition methodDefinition) {
        if (operand instanceof FieldReference) {
            visitFieldReference((FieldReference) operand, methodDefinition);
        }
    }

    private void visitFieldReference(final FieldReference fieldReference, final MethodDefinition methodDefinition) {
        if (areEquivalent(fieldReference, methodDefinition)) {
            addUsedByMethod(createSignature(methodDefinition), fieldReference.getName());
        }
    }

    private void handleMethodReference(final Object operand, final MethodDefinition methodDefinition) {
        if (operand instanceof MethodReference) {
            visitMethodReference((MethodReference) operand, methodDefinition);
        }
    }

    private void visitMethodReference(final MethodReference methodReference, final MethodDefinition methodDefinition) {
        if (areEquivalent(methodReference, methodDefinition)) {
            addUsedByMethod(createSignature(methodDefinition), createSignature(methodReference));
        }
    }

    private boolean areEquivalent(final MemberReference memberReference, final MethodDefinition methodDefinition) {
        return memberReference.getDeclaringType()
                              .isEquivalentTo(methodDefinition.getDeclaringType());
    }

    private String createSignature(final MemberReference memberReference) {
        return methodSignature.create(memberReference);
    }

    private void addUsedByMethod(final String method, final String used) {
        Optional.ofNullable(usedByMethod.get(method))
                .orElseGet(() -> {
                    usedByMethod.put(method, new HashSet<>());
                    return usedByMethod.get(method);
                })
                .add(used);
    }
}
