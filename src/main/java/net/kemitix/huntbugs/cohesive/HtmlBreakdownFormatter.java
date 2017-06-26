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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * HTML implementation of {@link BreakdownFormatter}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
class HtmlBreakdownFormatter implements BreakdownFormatter {

    private static final String LIST_START = "<ul><li>";

    private static final String LIST_SEPARATOR = "</li>\n<li>";

    private static final String LIST_END = "</li>\n</ul>\n";

    @Override
    public String apply(final Collection<Component> components) {
        final AtomicInteger counter = new AtomicInteger();
        return unsortedList(components.stream()
                                      .map(formatComponent(counter))
                                      .collect(Collectors.toList()));
    }

    private Function<Component, String> formatComponent(final AtomicInteger counter) {
        return c -> {
            return formatMethods(counter, methods(c.getMembers())) + formatFields(fields(c.getMembers()));
        };
    }

    private List<String> fields(final Set<String> members) {
        return members.stream()
                      .sorted()
                      .filter(isNotMethod())
                      .collect(Collectors.toList());
    }

    private Predicate<String> isNotMethod() {
        return isMethod().negate();
    }

    private String formatFields(final Collection<String> fields) {
        if (fields.size() > 0) {
            return "Using fields: " + String.join(", ", fields);
        }
        return "";
    }

    private String formatMethods(final AtomicInteger counter, final Collection<String> methods) {
        return asNestedList(counter).apply(methods);
    }

    private Function<Collection<String>, String> asNestedList(final AtomicInteger counter) {
        return items -> String.format("#%d:\n%s", counter.incrementAndGet(), unsortedList(items));
    }

    private List<String> methods(final Set<String> members) {
        return members.stream()
                      .sorted()
                      .filter(isMethod())
                      .collect(Collectors.toList());
    }

    private Predicate<String> isMethod() {
        return m -> m.contains("(");
    }

    private String unsortedList(final Collection<String> items) {
        return LIST_START + String.join(LIST_SEPARATOR, items) + LIST_END;
    }
}
