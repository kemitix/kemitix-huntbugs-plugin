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

import java.util.function.Predicate;

/**
 * Provides {@link java.util.function.Predicate}s for filtering methods.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface MethodFilter {

    /**
     * Creates an instance of the default implementation.
     *
     * @param methodDefinitionWrapper the wrapper for accessing the method definition
     *
     * @return a instance of MethodFilter
     */
    static MethodFilter defaultInstance(
            final MethodDefinitionWrapper methodDefinitionWrapper
                                       ) {
        return new MethodFilterImpl(methodDefinitionWrapper);
    }

    /**
     * Creates a predicate to filter {@link MethodDefinition}s by whether they are a constructor or not.
     *
     * @param value {@code true} when the predicate should return {@code true} for a constructor or {@code false} when
     *              the predicate should return {@code true} for non-constructors
     *
     * @return {@code true} only when the method definition is a constructor and the value is {@code true}, or when the
     * method definition is not a constructor and the value is {@code false}.
     */
    Predicate<MethodDefinition> isConstructor(boolean value);

    /**
     * Creates a predicate to filter {@link MethodDefinition}s by whether they are private or not.
     *
     * @param value {@code true} when the predicate should return {@code true} for a private method or {@code false}
     *              when the predicate should return {@code true} for non-private methods
     *
     * @return {@code true} only when the method definition is private and the value is {@code true}, or when the method
     * definition is not private and the value is {@code false}.
     */
    Predicate<MethodDefinition> isPrivate(boolean value);
}
