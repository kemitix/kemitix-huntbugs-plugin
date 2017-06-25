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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link Analyser}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
@RequiredArgsConstructor
class DefaultAnalyser implements Analyser {

    private final BeanMethods beanMethods;

    @Override
    public final AnalysisResult analyse(
            @NonNull final Map<String, Set<String>> usedByMethod, @NonNull final Set<String> nonPrivateMethods
                             ) {
        final AnalysisResult result = new AnalysisResult();
        result.addNonBeanMethods(getNonBeanNonPrivateMethods(usedByMethod, nonPrivateMethods));
        result.addComponents(findComponents(usedByMethod));
        return result;
    }

    private Set<String> getNonBeanNonPrivateMethods(
            final Map<String, Set<String>> usedByMethod, final Set<String> nonPrivateMethods
                                                   ) {
        return nonPrivateMethods.stream()
                                .filter(m -> beanMethods.isNotBeanMethod(m, usedByMethod.get(m)))
                                .collect(Collectors.toSet());
    }

    private Set<Component> findComponents(final Map<String, Set<String>> usedByMethod) {
        final Set<Component> components = usedByMethodAsComponents(usedByMethod);
        return mergeComponents(components);
    }

    private Set<Component> usedByMethodAsComponents(final Map<String, Set<String>> usedByMethod) {
        return usedByMethod.entrySet()
                           .stream()
                           .filter(e -> beanMethods.isNotBeanMethod(e.getKey(), usedByMethod.get(e.getKey())))
                           .map(this::componentFromEntry)
                           .collect(Collectors.toSet());
    }

    private Set<Component> mergeComponents(final Set<Component> components) {
        final Set<Component> merged = new HashSet<>();
        components.forEach(component -> {
            final Optional<Component> existing = merged.stream()
                                                       .filter(target -> overlap(component, target))
                                                       .findFirst();
            if (existing.isPresent()) {
                existing.get()
                        .merge(component);
            } else {
                merged.add(component);
            }
        });
        return merged;
    }

    private boolean overlap(final Component a, final Component b) {
        final Set<String> aMembers = a.getMembers();
        final Set<String> bMembers = b.getMembers();
        final boolean hasCommonMembers = Sets.intersection(aMembers, bMembers)
                                             .isEmpty();
        return !hasCommonMembers;
    }

    private Component componentFromEntry(final Map.Entry<String, Set<String>> entry) {
        final Set<String> members = new HashSet<>(entry.getValue());
        members.add(entry.getKey());
        return Component.from(members);
    }

}
