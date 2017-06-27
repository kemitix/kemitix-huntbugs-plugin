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

import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link Analyser}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@RequiredArgsConstructor
class DefaultAnalyser implements Analyser {

    private static final String PARENS_OPEN = "(";

    private final BeanMethods beanMethods;

    private final ComponentMerger componentMerger;

    @Override
    public final AnalysisResult analyse(
            @NonNull final Map<String, Collection<String>> usedByMethod,
            @NonNull final Collection<String> nonPrivateMethods, @NonNull final Collection<String> fields
                                       ) {
        final AnalysisResult result = new AnalysisResult();
        result.addNonBeanMethods(getNonBeanNonPrivateMethods(usedByMethod, nonPrivateMethods));
        result.addComponents(findComponents(usedByMethod, fields));
        return result;
    }

    private Collection<String> getNonBeanNonPrivateMethods(
            final Map<String, Collection<String>> usedByMethod, final Collection<String> nonPrivateMethods
                                                          ) {
        return nonPrivateMethods.stream()
                                .filter(m -> isNotABeanMethod(m, membersUsedByMethod(usedByMethod, m)))
                                .collect(Collectors.toSet());
    }

    private Collection<String> membersUsedByMethod(
            final Map<String, Collection<String>> usedByMethod, final String methodName
                                                  ) {
        return Optional.ofNullable(usedByMethod.get(methodName))
                       .orElseGet(Collections::emptySet);
    }

    private boolean isNotABeanMethod(final String m, final Collection<String> fields) {
        return isAField(m) || beanMethods.isNotBeanMethod(m, fields);
    }

    private Collection<Component> findComponents(
            final Map<String, Collection<String>> usedByMethod, final Collection<String> fields
                                                ) {
        final Collection<Component> allComponents = usedByMethodAsComponents(usedByMethod);
        final Collection<Component> mergedComponents = componentMerger.merge(allComponents);
        return filterComponents(mergedComponents, fields);
    }

    private Collection<Component> filterComponents(
            final Collection<Component> components, final Collection<String> fields
                                                  ) {
        return components.stream()
                         .map(removeBaseObjectMethods())
                         .map(removeConstructors())
                         .map(removeBeanMethods(fields))
                         .collect(Collectors.toSet());
    }

    private Function<Component, Component> removeBaseObjectMethods() {
        return c -> Component.from(c.getMembers()
                                    .stream()
                                    .filter(isNotBaseObjectMethod())
                                    .collect(Collectors.toSet()));
    }

    private Predicate<String> isNotBaseObjectMethod() {
        return isBaseObjectMethod().negate();
    }

    private Predicate<String> isBaseObjectMethod() {
        final List<String> signatures = new ArrayList<>();
        signatures.add("toString\\(\\)Ljava/lang/String;");
        signatures.add("equals\\(Ljava/lang/Object;\\)Z");
        signatures.add("hashCode\\(\\)I");
        signatures.add("clone\\(\\)Ljava/lang/Object;");
        final String pattern = "^(" + String.join("|", signatures) + ")$";
        return m -> m.matches(pattern);
    }

    private Function<Component, Component> removeBeanMethods(final Collection<String> fields) {
        return c -> Component.from(c.getMembers()
                                    .stream()
                                    .filter(m -> isNotABeanMethod(m, fields))
                                    .collect(Collectors.toSet()));
    }

    private Function<Component, Component> removeConstructors() {
        return c -> Component.from(c.getMembers()
                                    .stream()
                                    .filter(isNotAConstructor())
                                    .filter(isNotClassInitializer())
                                    .collect(Collectors.toSet()));
    }

    private Predicate<String> isNotClassInitializer() {
        return isClassInitializer().negate();
    }

    private Predicate<String> isClassInitializer() {
        return m -> m.startsWith("<clinit>");
    }

    private Predicate<String> isNotAConstructor() {
        return isAConstructor().negate();
    }

    private Predicate<String> isAConstructor() {
        return m -> m.startsWith("<init>");
    }

    private boolean isAField(final String m) {
        return !m.contains(PARENS_OPEN);
    }

    private Collection<Component> usedByMethodAsComponents(final Map<String, Collection<String>> usedByMethod) {
        return usedByMethod.entrySet()
                           .stream()
                           .filter(e -> isNotABeanMethod(e.getKey(), membersUsedByMethod(usedByMethod, e.getKey())))
                           .map(this::componentFromEntry)
                           .collect(Collectors.toSet());
    }

    private Component componentFromEntry(final Map.Entry<String, Collection<String>> entry) {
        final Collection<String> members = Sets.newHashSet(entry.getValue());
        members.add(entry.getKey());
        return Component.from(members);
    }

}
