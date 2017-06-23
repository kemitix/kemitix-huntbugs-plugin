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

package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.decompiler.ast.Expression;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.AstNodes;
import one.util.huntbugs.registry.anno.AstVisitor;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.VisitOrder;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.HashMap;
import java.util.HashSet;
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
@WarningDefinition(category = "?", name = "CohesiveDetector", maxScore = CohesiveDetector.MAX_SCORE)
public class CohesiveDetector {

    static final int MAX_SCORE = 100;

    private final BeanMethods beanMethods;

    private final MethodSignature methodSignature;

    private Set<String> nonPrivateMethodNames;

    private Map<String, Set<String>> usedByMethod;

    /**
     * Default constructor.
     */
    public CohesiveDetector() {
        methodSignature = new DefaultMethodSignature();
        beanMethods = new BeanMethodsImpl(methodSignature);
    }

    /**
     * Analyse the class.
     *
     * @param td the class
     */
    @ClassVisitor(order = VisitOrder.BEFORE)
    public void init(final TypeDefinition td) {
        final Set<String> fields = getDeclaredFieldNames(td);
        final Predicate<MethodDefinition> isNonBeanMethod =
                methodDefinition -> beanMethods.isNotBeanMethod(methodDefinition, fields);
        final Predicate<MethodDefinition> isNotConstructor = methodDefinition -> !methodDefinition.isConstructor();
        final Predicate<MethodDefinition> nonPrivate = methodDefinition -> !methodDefinition.isPrivate();
        nonPrivateMethodNames = td.getDeclaredMethods()
                                  .stream()
                                  .filter(isNotConstructor)
                                  .filter(isNonBeanMethod)
                                  .filter(nonPrivate)
                                  .map(this::createSignature)
                                  .collect(Collectors.toSet());
        usedByMethod = new HashMap<>();
    }

    /**
     * Analyse the results of scanning the class.
     *
     * @param cc the context for reporting errors
     */
    @ClassVisitor(order = VisitOrder.AFTER)
    public void analyse(final ClassContext cc) {
        nonPrivateMethodNames.stream()
                             .map(m -> "method: " + m)
                             .forEach(System.out::println);
        usedByMethod.keySet()
                    .forEach(method -> {
                        System.out.println("method = " + method);
                        usedByMethod.get(method)
                                    .forEach(used -> {
                                        System.out.println("  used = " + used);
                                    });
                    });
        System.out.println();
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
        if (methodDefinition.isConstructor()) {
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

    private Set<String> getDeclaredFieldNames(final TypeDefinition td) {
        return td.getDeclaredFields()
                 .stream()
                 .map(FieldDefinition::getName)
                 .collect(Collectors.toSet());
    }
}
