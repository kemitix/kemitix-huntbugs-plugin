package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TypeDefinitionWrapperImpl}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class TypeDefinitionWrapperImplTest {

    private TypeDefinitionWrapper wrapper = new TypeDefinitionWrapperImpl();

    private MyTypeDefinition typeDefinition;

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
        typeDefinition = MyTypeDefinition.withField(fieldDefinition);
        //when
        final List<FieldDefinition> declaredFields = wrapper.getDeclaredFields(typeDefinition);
        //then
        assertThat(declaredFields).containsExactly(fieldDefinition);
    }

    @Test
    public void canGetDeclaredMethods() {
        //given
        typeDefinition = MyTypeDefinition.withMethod(methodDefinition);
        //when
        final List<MethodDefinition> declaredMethods = wrapper.getDeclaredMethods(typeDefinition);
        //then
        assertThat(declaredMethods).containsExactly(methodDefinition);
    }

    private static class MyTypeDefinition extends TypeDefinition {

        private String fullname;

        static MyTypeDefinition withField(final FieldDefinition fieldDefinition) {
            final MyTypeDefinition typeDefinition = new MyTypeDefinition();
            typeDefinition.getDeclaredFieldsInternal()
                          .add(fieldDefinition);
            return typeDefinition;
        }

        static MyTypeDefinition withMethod(final MethodDefinition methodDefinition) {
            final MyTypeDefinition typeDefinition = new MyTypeDefinition();
            typeDefinition.getDeclaredMethodsInternal()
                          .add(methodDefinition);
            return typeDefinition;
        }

        static MyTypeDefinition withName(final String name) {
            final MyTypeDefinition typeDefinition = new MyTypeDefinition();
            typeDefinition.setName(name);
            return typeDefinition;
        }

        static MyTypeDefinition withFullName(final String name) {
            final MyTypeDefinition typeDefinition = new MyTypeDefinition();
            typeDefinition.fullname = name;
            return typeDefinition;
        }

        @Override
        protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
            return sb.append(fullname);
        }
    }

    @Test
    public void canGetName() {
        //given
        final String name = randomText();
        typeDefinition = MyTypeDefinition.withName(name);
        //when
        final String wrapperName = wrapper.getName(typeDefinition);
        //then
        assertThat(wrapperName).isSameAs(name);
    }

    private String randomText() {
        return UUID.randomUUID()
                   .toString();
    }

    @Test
    public void canGetFullName() {
        //given
        final String name = randomText();
        typeDefinition = MyTypeDefinition.withFullName(name);
        assertThat(typeDefinition.getFullName()).isEqualTo(name);
        //when
        final String wrapperName = wrapper.getFullName(typeDefinition);
        //then
        assertThat(wrapperName).isEqualTo(name);
    }

}
