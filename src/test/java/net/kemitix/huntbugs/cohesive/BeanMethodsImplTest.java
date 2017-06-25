package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.MethodDefinition;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link BeanMethodsImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
public class BeanMethodsImplTest {

    private BeanMethods beanMethods;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private MethodDefinition methodDefinition;

    private Set<String> fields;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        beanMethods = new BeanMethodsImpl(methodSignature);
        fields = new HashSet<>();
    }

    @Test
    public void beanWhenPlainGetter() {
        //given
        final String signature = "getName()";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void beanWhenBooleanGetter() {
        //given
        final String signature = "isName()Ljava/lang/Boolean;";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void beanWhenPrimitiveBooleanGetter() {
        //given
        final String signature = "isName()Z";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void beanWhenSetter() {
        //given
        final String signature = "setName(Ljava/lang/String;)V";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void nonBeanWhenSetterWithNoParameters() {
        //given
        final String signature = "setName()V";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void nonBeanWhenSetterWithNonVoidReturn() {
        //given
        final String signature = "setName()I";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("name");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void nonBeanWhenNoFields() {
        //given
        final String signature = "getName()";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.clear();
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void nonBeanWhenNoMatchingFields() {
        //given
        final String signature = "getName()";
        given(methodSignature.create(methodDefinition)).willReturn(signature);
        fields.add("other");
        //when
        final boolean result = beanMethods.isNotBeanMethod(methodDefinition, fields);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void NPEWhenMethodDefinitionIsNull() {
        //when
        final ThrowableAssert.ThrowingCallable action =
                () -> beanMethods.isNotBeanMethod((MethodDefinition) null, fields);
        //then
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("methodDefinition");
    }

    @Test
    public void NPEWhenFieldsIsNullWithMethodDefinition() {
        //when
        final ThrowableAssert.ThrowingCallable action = () -> beanMethods.isNotBeanMethod(methodDefinition, null);
        //then
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("fields");
    }

    @Test
    public void NPEWhenFieldsIsNullWithMethodName() {
        //when
        final ThrowableAssert.ThrowingCallable action = () -> beanMethods.isNotBeanMethod("methodName", null);
        //then
        assertThatNullPointerException().isThrownBy(action)
                                        .withMessage("fields");
    }
}
