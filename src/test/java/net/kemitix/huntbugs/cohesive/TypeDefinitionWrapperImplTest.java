package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeDefinitionWrapperImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class TypeDefinitionWrapperImplTest {

    private TypeDefinitionWrapper wrapper = new TypeDefinitionWrapperImpl();

    private MyTypeDefinition typeDefinition = new MyTypeDefinition();

    @Mock
    private FieldDefinition fieldDefinition;

    @Mock
    private MethodDefinition methodDefinition;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void canGetDeclaredFields() {
        //given
        typeDefinition.addDeclaredField(fieldDefinition);
        //when
        final List<FieldDefinition> declaredFields = wrapper.getDeclaredFields(typeDefinition);
        //then
        assertThat(declaredFields).containsExactly(fieldDefinition);
    }

    @Test
    public void canGetDeclaredMethods() {
        //given
        typeDefinition.addDeclaredMethod(methodDefinition);
        //when
        final List<MethodDefinition> declaredMethods = wrapper.getDeclaredMethods(typeDefinition);
        //then
        assertThat(declaredMethods).containsExactly(methodDefinition);
    }

    private class MyTypeDefinition extends TypeDefinition {

        void addDeclaredField(final FieldDefinition fieldDefinition) {
            getDeclaredFieldsInternal().add(fieldDefinition);
        }

        void addDeclaredMethod(final MethodDefinition methodDefinition) {
            getDeclaredMethodsInternal().add(methodDefinition);
        }
    }
}
