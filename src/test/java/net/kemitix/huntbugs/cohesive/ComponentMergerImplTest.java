package net.kemitix.huntbugs.cohesive;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ComponentMergerImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class ComponentMergerImplTest {

    private ComponentMerger merger;

    @Before
    public void setUp() {
        merger = new ComponentMergerImpl();
    }

    @Test
    public void singleComponentDoesNotChange() {
        //given
        final Component component = Component.from(list("member1", "member2"));
        //when
        final Collection<Component> merged = merger.merge(list(component));
        //then
        assertThat(merged).containsExactly(component);
        assertThat(component.getMembers()).containsExactlyInAnyOrder("member1", "member2");
    }

    @SafeVarargs
    private static <T> Collection<T> list(final T... members) {
        return Arrays.asList(members);
    }

    @Test
    public void twoComponentWithNoOverlapDoNotChange() {
        //given
        final Component a = Component.from(list("member1"));
        final Component b = Component.from(list("member2"));
        //when
        final Collection<Component> merged = merger.merge(list(a, b));
        //then
        assertThat(merged).containsExactlyInAnyOrder(a, b);
        assertThat(a.getMembers()).containsExactly("member1");
        assertThat(b.getMembers()).containsExactly("member2");
    }

    @Test
    public void twoComponentsWithOverlapAreMerged() {
        //given
        final Component a = Component.from(list("member1", "member3"));
        final Component b = Component.from(list("member2", "member1"));
        //when
        final Collection<Component> merged = merger.merge(list(a, b));
        //then
        assertThat(merged).hasSize(1);
        final Component c = new ArrayList<>(merged).get(0);
        assertThat(c.getMembers()).containsExactlyInAnyOrder("member1", "member2", "member3");
    }

    @Test
    public void emptyComponentIsRemoved() {
        //given
        final Component a = Component.from(list("member1"));
        final Component b = Component.from(list());
        //when
        final Collection<Component> merged = merger.merge(list(a, b));
        //then
        assertThat(merged).hasSize(1);
        final Component c = new ArrayList<>(merged).get(0);
        assertThat(c.getMembers()).containsExactly("member1");
    }

    @Test
    public void multipleComponentsAreMergedWhenJoinedLater() {
        //given
        final Component a = Component.from(list("member1", "member2"));
        final Component b = Component.from(list("member3", "member4"));
        final Component c = Component.from(list("member1", "member4", "member5"));
        //when
        final Collection<Component> merged = merger.merge(list(a, b, c));
        //then
        assertThat(merged).hasSize(1);
        final Component d = new ArrayList<>(merged).get(0);
        assertThat(d.getMembers()).containsExactlyInAnyOrder("member1", "member2", "member3", "member4", "member5");
    }
}
