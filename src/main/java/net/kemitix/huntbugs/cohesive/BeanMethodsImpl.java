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

import com.strobel.assembler.metadata.MethodDefinition;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * Implementation of BeanMethods.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@RequiredArgsConstructor
class BeanMethodsImpl implements BeanMethods {

    private final MethodSignature methodSignature;

    @Override
    public final boolean isNotBeanMethod(
            @NonNull final MethodDefinition methodDefinition, @NonNull final Set<String> fields
                                        ) {
        return !isBeanMethod(methodDefinition, fields);
    }

    @Override
    public final boolean isNotBeanMethod(final String methodName, final Set<String> fields) {
        return !isBeanMethod(methodName, fields);
    }

    private boolean isBeanMethod(final MethodDefinition methodDefinition, final Set<String> fields) {
        return isBeanMethod(methodSignature.create(methodDefinition), fields);
    }

    private boolean isBeanMethod(final String method, final Set<String> fields) {
        final String methodName = method.toLowerCase();
        return fields.stream()
                     .anyMatch(field -> isBeanMethod(methodName, field));
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
        return method.startsWith(("set" + field) + "(") && method.endsWith(")v") && !method.contains("()");
    }

}
