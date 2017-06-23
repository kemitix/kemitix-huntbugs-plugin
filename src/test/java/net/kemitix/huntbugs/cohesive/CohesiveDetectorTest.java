package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Expression;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private MethodDefinition nonPrivateMethodDefinition;

    private String nonPrivateMethodSignature;

    @Mock
    private MethodDefinition beanMethodDefinition;

    private String beanMethodSignature;

    private Expression expression;

    @Mock
    private FieldReference fieldReference;

    @Mock
    private TypeReference memberDeclaringType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        detector = new CohesiveDetector(beanMethods, methodSignature, typeDefinitionWrapper, methodDefinitionWrapper,
                                        nonPrivateMethodNames, usedByMethod
        );
        given(typeDefinitionWrapper.getDeclaredMethods(typeDefinition)).willReturn(declaredMethods);
        expression = new Expression(AstCode.Nop, null, 0);
    }

    private String randomString() {
        return UUID.randomUUID()
                   .toString();
    }

    @Test
    public void excludePrivateMethods() {
        //given
        hasPrivateMethod();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).doesNotContain(privateMethodSignature);
    }

    private void hasPrivateMethod() {
        privateMethodSignature = randomString();
        setAsSignature(privateMethodDefinition, privateMethodSignature);
        setAsConstructor(privateMethodDefinition, false);
        setAsPrivate(privateMethodDefinition, true);
        declaredMethods.add(privateMethodDefinition);
    }

    private void setAsSignature(final MethodDefinition methodDefinition, final String signature) {
        given(methodSignature.create(methodDefinition)).willReturn(signature);
    }

    private void setAsPrivate(final MethodDefinition methodDefinition, final boolean value) {
        given(methodDefinitionWrapper.isPrivate(methodDefinition)).willReturn(value);
    }

    private void setAsConstructor(
            final MethodDefinition methodDefinition, final boolean value
                                 ) {
        given(methodDefinitionWrapper.isConstructor(methodDefinition)).willReturn(value);
    }

    @Test
    public void excludeConstructorMethods() {
        //given
        hasConstructor();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).doesNotContain(constructorMethodSignature);
    }

    private void hasConstructor() {
        constructorMethodSignature = randomString();
        setAsSignature(constructorMethodDefinition, constructorMethodSignature);
        setAsConstructor(constructorMethodDefinition, true);
        declaredMethods.add(constructorMethodDefinition);
    }

    @Test
    public void includeNonPrivateNonBeanMethods() {
        //given
        hasNonPrivateNonBeanMethod();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).contains(nonPrivateMethodSignature);
    }

    private void hasNonPrivateNonBeanMethod() {
        nonPrivateMethodSignature = randomString();
        setAsSignature(nonPrivateMethodDefinition, nonPrivateMethodSignature);
        setAsConstructor(nonPrivateMethodDefinition, false);
        setAsPrivate(nonPrivateMethodDefinition, false);
        setAsBean(nonPrivateMethodDefinition, false);
        declaredMethods.add(nonPrivateMethodDefinition);
    }

    private void setAsBean(final MethodDefinition methodDefinition, final boolean value) {
        given(beanMethods.isNotBeanMethod(eq(methodDefinition), any())).willReturn(!value);
    }

    @Test
    public void excludeBeanMethods() {
        //given
        hasBeanMethod();
        //when
        detector.init(typeDefinition);
        //then
        assertThat(nonPrivateMethodNames).doesNotContain(beanMethodSignature);
    }

    private void hasBeanMethod() {
        beanMethodSignature = randomString();
        setAsSignature(beanMethodDefinition, beanMethodSignature);
        setAsConstructor(beanMethodDefinition, false);
        setAsPrivate(beanMethodDefinition, false);
        setAsBean(beanMethodDefinition, true);
        declaredMethods.add(beanMethodDefinition);
    }

    @Test
    public void skipMethodWhenConstructor() {
        //given
        hasConstructor();
        //when
        final boolean result = detector.visit(expression, constructorMethodDefinition);
        //then
        assertThat(result).isFalse();
    }

    @Test
    public void handleFieldInSameClass() {
        //given
        hasNonPrivateNonBeanMethod();
        final String fieldName = randomString();
        setAsFieldReference(fieldName);
        setAsInSameClass(fieldReference, true);
        //when
        detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(usedByMethod.get(nonPrivateMethodSignature)).contains(fieldName);
    }

    private void setAsInSameClass(final MemberReference memberReference, final boolean value) {
        given(memberReference.getDeclaringType()).willReturn(memberDeclaringType);
        given(memberDeclaringType.isEquivalentTo(any())).willReturn(value);
    }

    private void setAsFieldReference(final String fieldName) {
        given(fieldReference.getName()).willReturn(fieldName);
        expression.setOperand(fieldReference);
    }

    @Test
    public void ignoreFieldInOtherClass() {
        //given
        hasNonPrivateNonBeanMethod();
        final String fieldName = randomString();
        setAsFieldReference(fieldName);
        setAsInSameClass(fieldReference, false);
        //when
        detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(usedByMethod).isEmpty();
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
