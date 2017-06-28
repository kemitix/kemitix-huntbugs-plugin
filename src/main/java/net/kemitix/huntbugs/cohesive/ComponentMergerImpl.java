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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementation of {@link ComponentMerger}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
class ComponentMergerImpl implements ComponentMerger {

    @Override
    public Collection<Component> merge(final Collection<Component> components) {
        final Collection<Component> merged = Sets.newHashSet();
        components.stream()
                  .filter(c -> c.getMembers()
                                .size() > 0)
                  .forEach(component -> {
                      final Optional<Component> existing = merged.stream()
                                                                 .filter(membersOverlap(component))
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

    private Predicate<Component> membersOverlap(final Component component) {
        return membersDoNotOverlap(component).negate();
    }

    private Predicate<Component> membersDoNotOverlap(final Component component) {
        return target -> Sets.intersection(component.getMembers(), target.getMembers())
                             .isEmpty();
    }

}
