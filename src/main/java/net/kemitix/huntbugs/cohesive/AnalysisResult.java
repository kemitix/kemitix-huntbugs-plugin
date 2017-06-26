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

import java.util.Collection;
import java.util.HashSet;

/**
 * Represents the results of performing a Cohesive Analysis on a class.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public final class AnalysisResult {

    private Collection<String> nonBeanMethods = new HashSet<>();

    private Collection<Component> components = new HashSet<>();

    /**
     * Gets the components found within a class.
     *
     * @return a set of components
     */
    public Collection<Component> getComponents() {
        return new HashSet<>(components);
    }

    /**
     * Gets the non-bean methods of a class.
     *
     * @return a set of method signatures
     */
    public Collection<String> getNonBeanMethods() {
        return new HashSet<>(nonBeanMethods);
    }

    /**
     * Add the methods.
     *
     * @param methods the methods to add
     */
    public void addNonBeanMethods(final Collection<String> methods) {
        nonBeanMethods.addAll(methods);
    }

    /**
     * Add the components.
     *
     * @param items the components to add
     */
    public void addComponents(final Collection<Component> items) {
        components.addAll(items);
    }
}
