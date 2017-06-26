package net.kemitix.huntbugs.cohesive;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HtmlBreakdownFormatter}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
public class HtmlBreakdownFormatterTest {

    private BreakdownFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HtmlBreakdownFormatter();
    }

    @Test
    public void canFormatASingleComponentWithSingleMethodAndNoFields() {
        //given
        final Component component = Component.from(Collections.singletonList("doSomething()V"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo("<ul><li>#1:\n<ul><li>doSomething()V</li>\n</ul>\n</li>\n</ul>\n");
    }

    private String formatComponents(final Component... components) {
        return formatter.apply(Sets.newHashSet(components));
    }

    @Test
    public void canFormatASingleComponentWithSingleMethodAndAField() {
        //given
        final Component component = Component.from(Arrays.asList("doSomething()V", "aField"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo(
                "<ul><li>#1:\n<ul><li>doSomething()V</li>\n</ul>\nUsing fields: aField</li>\n</ul>\n");
    }

    @Test
    public void canFormatASingleComponentWithSingleMethodAndMultipleFields() {
        //given
        final Component component = Component.from(Arrays.asList("doSomething()V", "aField", "bField"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo(
                "<ul><li>#1:\n<ul><li>doSomething()V</li>\n</ul>\nUsing fields: aField, bField</li>\n</ul>\n");
    }

    @Test
    public void canFormatASingleComponentsWithMultipleMethodsAndMultipleFields() {
        //given
        final Component component =
                Component.from(Arrays.asList("doSomething()V", "doSomethingElse()V", "aField", "bField"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo(
                "<ul><li>#1:\n<ul><li>doSomething()V</li>\n<li>doSomethingElse()V</li>\n</ul>\nUsing fields: aField, "
                + "bField</li>\n</ul>\n");
    }

    @Test
    public void canFormatMultipleComponentsWithMultipleMethodsAndMultipleFields() {
        //given
        final Component component1 =
                Component.from(Arrays.asList("doSomething()V", "doSomethingElse()V", "aField", "bField"));
        final Component component2 =
                Component.from(Arrays.asList("alsoDoSomething()V", "alsoDoSomethingElse()V", "aValue", "bValue"));
        //when
        final String result = formatComponents(component1, component2);
        //then
        final String formatted1 =
                "<ul><li>doSomething()V</li>\n<li>doSomethingElse()V</li>\n</ul>\nUsing fields: aField, "
                + "bField</li>\n";
        final String formatted2 = "<ul><li>alsoDoSomething()V</li>\n<li>alsoDoSomethingElse()"
                                  + "V</li>\n</ul>\nUsing fields: aValue, bValue</li>\n";
        assertThat(result).startsWith("<ul><li>#1:\n")
                          .contains(formatted1)
                          .contains("<li>#2:\n")
                          .contains(formatted2)
                          .endsWith("</ul>\n");
    }
}
