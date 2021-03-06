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

import java.util.List;

/**
 * Wrapper for accessing {@link com.strobel.assembler.metadata.TypeDefinition} data.
 *
 * <p>Using this wrapper makes methods accessing TypeDefinition's final methods more testable.</p>
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface TypeDefinitionWrapper {

    /**
     * Get a list of the declared fields.
     *
     * @param typeDefinition the type definition
     *
     * @return a List of FieldDefinition
     */
    List<FieldDefinition> getDeclaredFields(TypeDefinition typeDefinition);

    /**
     * Get a list of the declared methods.
     *
     * @param typeDefinition the type definition
     *
     * @return a List of MethodDefinition
     */
    List<MethodDefinition> getDeclaredMethods(TypeDefinition typeDefinition);

    /**
     * Create an instance of the default implementation of {@link TypeDefinitionWrapper}.
     *
     * @return an instance of TypeDefinitionWrapper
     */
    static TypeDefinitionWrapper defaultInstance() {
        return new TypeDefinitionWrapperImpl();
    }
}
