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
import java.util.Map;

/**
 * Analyses the method invocations of a class to determine the cohesiveness of a class..
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public interface Analyser {

    /**
     * Analyse the cohesion of a class from the items used by each method.
     *
     * @param usedByMethod      a map of fields and methods used grouped by each method
     * @param nonPrivateMethods a list of methods
     * @param fields            the fields in the class
     *
     * @return an AnalysisResult object
     */
    AnalysisResult analyse(
            Map<String, Collection<String>> usedByMethod, Collection<String> nonPrivateMethods,
            Collection<String> fields
                          );

    /**
     * Create an instance of the default implementation of {@link Analyser}.
     *
     * @param beanMethods     bean method identifier
     * @param componentMerger merges overlapping components
     *
     * @return an instance of Analyser
     */
    static Analyser defaultInstance(
            final BeanMethods beanMethods, final ComponentMerger componentMerger
                                   ) {
        return new DefaultAnalyser(beanMethods, componentMerger);
    }
}
