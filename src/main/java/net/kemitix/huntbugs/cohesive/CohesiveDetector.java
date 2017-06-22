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
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import one.util.huntbugs.registry.ClassContext;
import one.util.huntbugs.registry.anno.ClassVisitor;
import one.util.huntbugs.registry.anno.WarningDefinition;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Detects classes that are not cohesive.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@WarningDefinition(category = "?", name = "CohesiveDetector", maxScore = CohesiveDetector.MAX_SCORE)
public class CohesiveDetector {

    static final int MAX_SCORE = 100;

    /**
     * Analyse the class.
     *
     * @param td the class
     * @param cc the context for reporting errors
     */
    @ClassVisitor
    public void analyse(final TypeDefinition td, final ClassContext cc) {
        System.out.println();
        final String className = td.getFullName();
        System.out.println("className = " + className);
        final Set<String> fields = getDeclaredFieldNames(td);
        System.out.println("fields = " + fields);
        final Stream<MethodDefinition> methodDefinitionStream = td.getDeclaredMethods()
                                                                  .stream()
                                                                  .filter(methodDefinition -> !methodDefinition
                                                                          .isConstructor())
                                                                  .filter(methodDefinition -> !isBeanMethod(
                                                                          methodDefinition, fields));
        final Set<String> nonPrivateMethodNames =
                methodDefinitionStream.filter(methodDefinition -> !methodDefinition.isPrivate())
                                      .map(md -> md.getName() + md.getSignature())
                                      .collect(Collectors.toSet());
        System.out.println("nonPrivateMethodNames = \n" + nonPrivateMethodNames.stream()
                                                                               .map(m -> " - " + m)
                                                                               .collect(Collectors.joining("\n")));

        System.out.println();
    }

    private boolean isBeanMethod(final MethodDefinition methodDefinition, final Set<String> fields) {
        return isBeanMethod(methodDefinition.getName() + methodDefinition.getSignature(), fields);
    }

    private boolean isBeanMethod(final String method, final Set<String> fields) {
        if (fields != null) {
            final String methodName = method.toLowerCase();
            return fields.stream()
                         .anyMatch(field -> isBeanMethod(methodName, field));
        } else {
            return false;
        }
    }

    private boolean isBeanMethod(final String method, final String field) {
        return isSetter(method, field) || isGetter(method, field);
    }

    private boolean isGetter(final String method, final String field) {
        final boolean isPlainGetter = method.startsWith(String.format("get%s()", field));
        final boolean isBooleanGetter = String.format("is%s()ljava/lang/boolean;", field)
                                              .equals(method);
        final boolean isPrimitiveBooleanGetter = String.format("is%s()z", field)
                                                       .equals(method);
        return isPlainGetter || isBooleanGetter || isPrimitiveBooleanGetter;
    }

    private boolean isSetter(final String method, final String field) {
        return method.startsWith("set" + field + "([^)]") && method.endsWith(")v");
    }

    private Set<String> getDeclaredFieldNames(final TypeDefinition td) {
        return td.getDeclaredFields()
                 .stream()
                 .map(FieldDefinition::getName)
                 .collect(Collectors.toSet());
    }
}
