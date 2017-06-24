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

import java.util.Set;

/**
 * Detect Bean methods..
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface BeanMethods {

    /**
     * Identify if the method is a bean method or not.
     *
     * <p>Bean methods are the standard getters and setters for a field, including the boolean is* variants.</p>
     *
     * @param methodDefinition definition of the method to examine
     * @param fields           the data fields of the class
     *
     * @return true if the method matches the pattern for a bean method.
     */
    boolean isNotBeanMethod(MethodDefinition methodDefinition, Set<String> fields);

    /**
     * Create an instance of the default implementation of {@link BeanMethods}.
     *
     * @param methodSignature the method signature builder
     *
     * @return an instance of BeanMethods
     */
    static BeanMethods defaultInstance(MethodSignature methodSignature) {
        return new BeanMethodsImpl(methodSignature);
    }
}
