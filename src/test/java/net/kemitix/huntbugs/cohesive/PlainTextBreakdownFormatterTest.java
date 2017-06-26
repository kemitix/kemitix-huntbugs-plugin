package net.kemitix.huntbugs.cohesive;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PlainTextBreakdownFormatter}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
public class PlainTextBreakdownFormatterTest {

    private BreakdownFormatter formatter;

    @Before
    public void setUp() {
        formatter = new PlainTextBreakdownFormatter();
    }

    @Test
    public void canFormatASingleComponentWithSingleMethodAndNoFields() {
        //given
        final Component component = Component.from(Collections.singletonList("doSomething()V"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo("#1:" + System.lineSeparator() + " - doSomething()V");
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
                "#1:" + System.lineSeparator() + " - doSomething()V" + System.lineSeparator() + "Using fields: aField");
    }

    @Test
    public void canFormatASingleComponentWithSingleMethodAndMultipleFields() {
        //given
        final Component component = Component.from(Arrays.asList("doSomething()V", "aField", "bField"));
        //when
        final String result = formatComponents(component);
        //then
        assertThat(result).isEqualTo("#1:" + System.lineSeparator() + " - doSomething()V" + System.lineSeparator()
                                     + "Using fields: aField, bField");
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
                "#1:" + System.lineSeparator() + " - doSomething()V" + System.lineSeparator() + " - doSomethingElse()V"
                + System.lineSeparator() + "Using fields: aField, " + "bField");
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
                " - doSomething()V" + System.lineSeparator() + " - doSomethingElse()V" + System.lineSeparator()
                + "Using fields: aField, " + "bField";
        final String formatted2 = " - alsoDoSomething()V" + System.lineSeparator() + " - alsoDoSomethingElse()" + "V"
                                  + System.lineSeparator() + "Using fields: aValue, bValue";
        assertThat(result).startsWith("#1:" + System.lineSeparator() + "")
                          .contains(formatted1)
                          .contains("#2:" + System.lineSeparator() + "")
                          .contains(formatted2);
    }

}
