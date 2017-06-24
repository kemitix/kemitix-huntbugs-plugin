package net.kemitix.huntbugs.cohesive;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultAnalyser}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class DefaultAnalyserTest {

    private Analyser analyser;

    private Map<String, Set<String>> usedByMethod;

    private Set<String> nonPrivateMethods;

    private AnalysisResult analysisResult;

    @Before
    public void setUp() {
        usedByMethod = new HashMap<>();
        nonPrivateMethods = new HashSet<>();
        analyser = new DefaultAnalyser();
    }

    @Test
    public void canDetectNonBeanMethods() {
        //given
        final String beanGetMethod = "java.lang.String getValue()";
        nonPrivateMethods.add(beanGetMethod);
        final String beanSetMethod = "void setValue(java.lang.String)";
        nonPrivateMethods.add(beanSetMethod);
        final String nonBeanMethod = "void nonBean()";
        nonPrivateMethods.add(nonBeanMethod);
        final String booleanBeanMethod = "java.lang.Boolean isEnabled()";
        nonPrivateMethods.add(booleanBeanMethod);
        final String primitiveBooleanBeanMethod = "boolean isValid()";
        nonPrivateMethods.add(primitiveBooleanBeanMethod);
        usedByMethod.put(beanGetMethod, setOf("value"));
        usedByMethod.put(beanSetMethod, setOf("value"));
        usedByMethod.put(nonBeanMethod, setOf("other"));
        usedByMethod.put(booleanBeanMethod, setOf("enabled"));
        usedByMethod.put(primitiveBooleanBeanMethod, setOf("valid"));
        //when
        performAnalysis();
        //then
        assertThat(analysisResult.getNonBeanMethods()).containsExactly(nonBeanMethod);
    }

    private HashSet<String> setOf(final String... values) {
        return Sets.newHashSet(Arrays.asList(values));
    }

    private void performAnalysis() {
        analysisResult = analyser.analyse(usedByMethod, nonPrivateMethods);
    }

    @Test
    public void canDetectASingleComponentFromASingleMethodAndField() {
        //given
        final String method = "getValue()";
        nonPrivateMethods.add(method);
        final String fieldName = "fieldName";
        usedByMethod.put(method, setOf(fieldName));
        //when
        performAnalysis();
        //then
        final Set<Component> components = analysisResult.getComponents();
        assertThat(components).hasSize(1);
        final Component component = components.toArray(new Component[]{})[0];
        assertThat(component.getMembers()).containsExactlyInAnyOrder(method, fieldName);
    }

    @Test
    public void requiresNonNullUseByMethod() {
        //when
        final ThrowableAssert.ThrowingCallable action =
                () -> analyser.analyse(null, nonPrivateMethods);
        //then
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class)
                                  .hasMessage("usedByMethod");
    }

    @Test
    public void requiresNonNullNonPrivateMethods() {
        //when
        final ThrowableAssert.ThrowingCallable action =
                () -> analyser.analyse(usedByMethod, null);
        //then
        assertThatThrownBy(action).isInstanceOf(NullPointerException.class)
                                  .hasMessage("nonPrivateMethods");
    }

    @Test
    public void acceptWhenANonPrivateMethodsIsNotInUsedByMethod() {
        //given
        final String method = "getValue()";
        nonPrivateMethods.add(method);
        //when
        performAnalysis();
        //then
        // no exception is thrown
    }
}
