package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.MethodDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link MethodFilterImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class MethodFilterImplTest {

    private MethodFilter methodFilter;

    @Mock
    private MethodDefinitionWrapper methodDefinitionWrapper;

    @Mock
    private MethodDefinition constructor;

    @Mock
    private MethodDefinition nonConstructor;

    @Mock
    private MethodDefinition privateMethod;

    @Mock
    private MethodDefinition nonPrivateMethod;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        methodFilter = new MethodFilterImpl(methodDefinitionWrapper);
        given(methodDefinitionWrapper.isConstructor(constructor)).willReturn(true);
        given(methodDefinitionWrapper.isConstructor(nonConstructor)).willReturn(false);
        given(methodDefinitionWrapper.isPrivate(privateMethod)).willReturn(true);
        given(methodDefinitionWrapper.isPrivate(nonPrivateMethod)).willReturn(false);
    }

    @Test
    public void isConstructorReturnsTrueWhenIsConstructor() {
        assertThat(isConstructor().test(constructor)).isTrue();
    }

    @Test
    public void isConstructorReturnsFalseWhenIsNotConstructor() {
        assertThat(isConstructor().test(nonConstructor)).isFalse();
    }

    @Test
    public void isNotConstructorReturnsFalseWhenIsConstructor() {
        assertThat(isNotConstructor().test(constructor)).isFalse();
    }

    @Test
    public void isNotConstructorReturnsTrueWhenIsNotConstructor() {
        assertThat(isNotConstructor().test(nonConstructor)).isTrue();
    }

    private Predicate<? super MethodDefinition> isConstructor() {
        return methodFilter.isConstructor(true);
    }

    private Predicate<? super MethodDefinition> isNotConstructor() {
        return methodFilter.isConstructor(false);
    }

    @Test
    public void isPrivateReturnsTrueWhenIsPrivate() {
        assertThat(isPrivate().test(privateMethod)).isTrue();
    }

    @Test
    public void isPrivateReturnsFalseWhenIsNotPrivate() {
        assertThat(isPrivate().test(nonPrivateMethod)).isFalse();
    }

    @Test
    public void isNotPrivateReturnsFalseWhenIsPrivate() {
        assertThat(isNotPrivate().test(privateMethod)).isFalse();
    }

    @Test
    public void isNotPrivateReturnsTrueWhenIsNotPrivate() {
        assertThat(isNotPrivate().test(nonPrivateMethod)).isTrue();
    }

    private Predicate<? super MethodDefinition> isPrivate() {
        return methodFilter.isPrivate(true);
    }

    private Predicate<? super MethodDefinition> isNotPrivate() {
        return methodFilter.isPrivate(false);
    }
}
