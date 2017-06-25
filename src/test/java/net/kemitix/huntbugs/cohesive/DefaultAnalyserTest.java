package net.kemitix.huntbugs.cohesive;

import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

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

    private Set<String> fields = new HashSet<>();

    @Mock
    private BeanMethods beanMethods;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        usedByMethod = new HashMap<>();
        nonPrivateMethods = new HashSet<>();
        analyser = new DefaultAnalyser(beanMethods);
    }

    @Test
    public void canDetectNonBeanMethods() {
        //given
        final String beanGetMethod = "java.lang.String getValue()";
        whenIsABeanMethod(beanGetMethod, true);
        nonPrivateMethods.add(beanGetMethod);

        final String beanSetMethod = "void setValue(java.lang.String)";
        whenIsABeanMethod(beanSetMethod, true);
        nonPrivateMethods.add(beanSetMethod);

        final String nonBeanMethod = "void nonBean()";
        whenIsABeanMethod(nonBeanMethod, false);
        nonPrivateMethods.add(nonBeanMethod);

        final String booleanBeanMethod = "java.lang.Boolean isEnabled()";
        whenIsABeanMethod(booleanBeanMethod, true);
        nonPrivateMethods.add(booleanBeanMethod);

        final String primitiveBooleanBeanMethod = "boolean isValid()";
        whenIsABeanMethod(primitiveBooleanBeanMethod, true);
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
        analysisResult = analyser.analyse(usedByMethod, nonPrivateMethods, fields);
    }

    @Test
    public void canDetectASingleComponentFromASingleMethodAndField() {
        //given
        final String method = "getValue()";
        whenIsABeanMethod(method, false);
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
        final ThrowableAssert.ThrowingCallable action = () -> analyser.analyse(null, nonPrivateMethods, fields);
        //then
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("usedByMethod");
    }

    @Test
    public void requiresNonNullNonPrivateMethods() {
        //when
        final ThrowableAssert.ThrowingCallable action = () -> analyser.analyse(usedByMethod, null, fields);
        //then
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("nonPrivateMethods");
    }

    @Test
    public void acceptWhenANonPrivateMethodsIsNotInUsedByMethod() {
        //given
        final String method = "getValue()";
        nonPrivateMethods.add(method);
        whenIsABeanMethod(method, false);
        //when
        performAnalysis();
        //then
        // no exception is thrown
    }

    private void whenIsABeanMethod(final String method, final boolean isBeanMethod) {
        given(beanMethods.isNotBeanMethod(eq(method), any())).willReturn(!isBeanMethod);
    }

    @Test
    public void requiresNonNullFields() {
        //when
        final ThrowableAssert.ThrowingCallable action = () -> analyser.analyse(usedByMethod, nonPrivateMethods, null);
        //when
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("fields");
    }
}
