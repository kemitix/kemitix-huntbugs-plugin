package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.MethodDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link BeanMethodsImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
public class BeanMethodsImplTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("methodDefinition");
        //when
        beanMethods.isNotBeanMethod(null, fields);
    }

    @Test
    public void NPEWhenFieldsIsNull() {
        //given
        exception.expect(NullPointerException.class);
        exception.expectMessage("fields");
        //when
        beanMethods.isNotBeanMethod(methodDefinition, null);
    }
}
