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
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Stream;

/**
 * Detects classes that are not cohesive.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@RequiredArgsConstructor
@WarningDefinition(category = "?", name = "CohesiveDetector", maxScore = CohesiveDetector.MAX_SCORE)
public class CohesiveDetector {

    static final int MAX_SCORE = 100;

    private Set<String> nonPrivateMethodNames;

    private Map<String, Set<String>> usedByMethod;

    private final BeanMethods beanMethods;

    private final MethodSignature methodSignature;

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
        final Stream<MethodDefinition> methodDefinitionStream = td.getDeclaredMethods()
                                                                  .stream()
                                                                  .filter(isNotConstructor)
                                                                  .filter(isNonBeanMethod);
        nonPrivateMethodNames = methodDefinitionStream.filter(nonPrivate)
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

    @AstVisitor(nodes = AstNodes.EXPRESSIONS)
    public boolean visit(final Expression expression, final MethodDefinition methodDefinition) {
        if (methodDefinition.isConstructor()) {
            return false;
        }
        if (expression.getOperand() instanceof MethodReference) {
            final MethodReference methodReference = (MethodReference) expression.getOperand();
            final TypeDefinition myClass = methodDefinition.getDeclaringType();
            if (methodReference.getDeclaringType()
                               .isEquivalentTo(myClass)) {
                final String calledMethod = createSignature(methodReference);
                addUsedByMethod(createSignature(methodDefinition), calledMethod);
            }
        }
        if (expression.getOperand() instanceof FieldReference) {
            final FieldReference fieldReference = (FieldReference) expression.getOperand();
            final TypeDefinition myClass = methodDefinition.getDeclaringType();
            if (fieldReference.getDeclaringType()
                              .isEquivalentTo(myClass)) {
                final String usedField = fieldReference.getName();
                addUsedByMethod(createSignature(methodDefinition), usedField);
            }
        }
        return true;
    }

    private String createSignature(final MemberReference memberReference) {
        return methodSignature.create(memberReference);
    }

    private void addUsedByMethod(final String method, final String used) {
        Optional.ofNullable(usedByMethod.get(method))
                .orElseGet(() -> {
                    final HashSet<String> set = new HashSet<>();
                    usedByMethod.put(method, set);
                    return set;
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
