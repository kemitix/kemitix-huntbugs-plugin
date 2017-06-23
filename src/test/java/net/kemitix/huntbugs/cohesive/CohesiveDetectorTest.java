package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link CohesiveDetector}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net).
 */
public class CohesiveDetectorTest {

    private CohesiveDetector detector;

    @Mock
    private BeanMethods beanMethods;

    @Mock
    private MethodSignature methodSignature;

    private Set<String> nonPrivateMethodNames = new HashSet<>();

    private Map<String, Set<String>> usedByMethod = new HashMap<>();

    @Mock
    private TypeDefinition typeDefinition;

    private List<MethodDefinition> declaredMethods = new ArrayList<>();

    @Mock
    private MethodDefinition privateMethodDefinition;

    private Set<String> fields = new HashSet<>();

    private String privateMethodSignature;

    @Mock
    private TypeDefinitionWrapper typeDefinitionWrapper;

    @Mock
    private MethodDefinitionWrapper methodDefinitionWrapper;

    @Mock
    private MethodDefinition constructorMethodDefinition;

    private String constructorMethodSignature;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        detector = new CohesiveDetector(beanMethods, methodSignature, typeDefinitionWrapper, methodDefinitionWrapper,
                                        nonPrivateMethodNames, usedByMethod
        );
        given(typeDefinitionWrapper.getDeclaredMethods(typeDefinition)).willReturn(declaredMethods);
    }

    private String randomString() {
        return UUID.randomUUID()
                   .toString();
    }

    @Test
    public void canDetectNonPrivateMethods() {
        //given
        hasPrivateMethod();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).doesNotContain(privateMethodSignature);
    }

    private void hasPrivateMethod() {
        privateMethodSignature = randomString();
        given(methodDefinitionWrapper.isConstructor(privateMethodDefinition)).willReturn(false);
        given(methodDefinitionWrapper.isPrivate(privateMethodDefinition)).willReturn(true);
        given(methodSignature.create(privateMethodDefinition)).willReturn(privateMethodSignature);
        declaredMethods.add(privateMethodDefinition);
    }

    @Test
    public void canDetectConstructorMethods() {
        //given
        hasConstructor();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).doesNotContain(constructorMethodSignature);
    }

    private void hasConstructor() {
        constructorMethodSignature = randomString();
        given(methodDefinitionWrapper.isConstructor(constructorMethodDefinition)).willReturn(true);
        given(methodSignature.create(constructorMethodDefinition)).willReturn(constructorMethodSignature);
        declaredMethods.add(constructorMethodDefinition);
    }

    @Test
    @Ignore("TODO")
    public void canDetectPrivateMethods() {
    }

    @Test
    @Ignore("TODO")
    public void canDetectBeanMethods() {
    }

    @Test
    @Ignore("TODO")
    public void canDetectNonBeanMethods() {
    }

    @Test
    @Ignore("TODO")
    public void skipsWhenConstructor() {
    }

    @Test
    @Ignore("TODO")
    public void handleFieldInSameClass() {
    }

    @Test
    @Ignore("TODO")
    public void ignoreFieldInOtherClass() {
    }

    @Test
    @Ignore("TODO")
    public void handleMethodCallInSameClass() {
    }

    @Test
    @Ignore("TODO")
    public void ignoreMethodCallInOtherClass() {
    }

    @Test
    @Ignore("TODO")
    public void methodCanUseMulitpleItems() {
    }
}
