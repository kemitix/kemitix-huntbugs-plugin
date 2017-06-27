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
 * Implementation of {@link TypeDefinitionWrapper}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
class TypeDefinitionWrapperImpl implements TypeDefinitionWrapper {

    @Override
    public final List<FieldDefinition> getDeclaredFields(final TypeDefinition typeDefinition) {
        return typeDefinition.getDeclaredFields();
    }

    @Override
    public final List<MethodDefinition> getDeclaredMethods(final TypeDefinition typeDefinition) {
        return typeDefinition.getDeclaredMethods();
    }

    @Override
    public String getName(final TypeDefinition typeDefinition) {
        return typeDefinition.getName();
    }

    @Override
    public String getFullName(final TypeDefinition typeDefinition) {
        return typeDefinition.getFullName();
    }
}
