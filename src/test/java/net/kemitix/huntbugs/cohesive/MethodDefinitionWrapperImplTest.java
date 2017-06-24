package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.Flags;
import com.strobel.assembler.metadata.MethodDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MethodDefinitionWrapperImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class MethodDefinitionWrapperImplTest {

    private MethodDefinitionWrapper wrapper = new MethodDefinitionWrapperImpl();

    private MyMethodDefinition methodDefinition = new MyMethodDefinition();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canTestIsNotConstructor() {
        //when
        final boolean result = wrapper.isConstructor(methodDefinition);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void canTestIsConstructor() {
        //given
        methodDefinition.setAsConstructor();
        //when
        final boolean result = wrapper.isConstructor(methodDefinition);
        //then
        assertThat(result).isTrue();
    }

    @Test
    public void canTestIsNotPrivate() {
        //when
        final boolean result = wrapper.isPrivate(methodDefinition);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void canTestIsPrivate() {
        //given
        methodDefinition.setAsPrivate();
        //when
        final boolean result = wrapper.isPrivate(methodDefinition);
        //then
        assertThat(result).isTrue();
    }

    private class MyMethodDefinition extends MethodDefinition {

        void setAsConstructor() {
            setName(CONSTRUCTOR_NAME);
        }

        void setAsPrivate() {
            setFlags(Flags.PRIVATE);
        }
    }
}
