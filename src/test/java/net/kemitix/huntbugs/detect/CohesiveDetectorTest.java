package net.kemitix.huntbugs.detect;

import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Expression;
import net.kemitix.huntbugs.cohesive.Analyser;
import net.kemitix.huntbugs.cohesive.BeanMethods;
import net.kemitix.huntbugs.cohesive.BreakdownFormatter;
import net.kemitix.huntbugs.cohesive.MethodDefinitionWrapper;
import net.kemitix.huntbugs.cohesive.MethodFilter;
import net.kemitix.huntbugs.cohesive.MethodSignature;
import net.kemitix.huntbugs.cohesive.TypeDefinitionWrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
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
import static org.mockito.BDDMockito.then;

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

    private Map<String, Collection<String>> usedByMethod = new HashMap<>();

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

    @Mock
    private MethodReference methodReference;

    @Mock
    private Analyser analyser;

    @Mock
    private BreakdownFormatter breakdownFormatter;

    @Mock
    private PrintStream console;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        final MethodFilter methodFilter = MethodFilter.defaultInstance(methodDefinitionWrapper);
        detector = new CohesiveDetector(beanMethods, methodSignature, typeDefinitionWrapper, methodDefinitionWrapper,
                                        breakdownFormatter, breakdownFormatter, analyser, nonPrivateMethodNames,
                                        usedByMethod, methodFilter, console
        );
        given(typeDefinitionWrapper.getDeclaredMethods(typeDefinition)).willReturn(declaredMethods);
        expression = new Expression(AstCode.Nop, null, 0);
        nonPrivateMethodSignature = randomString();
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
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isTrue();
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
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isTrue();
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
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isTrue();
        assertThat(nonPrivateMethodNames).contains(nonPrivateMethodSignature);
    }

    private void hasNonPrivateNonBeanMethod() {
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
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isTrue();
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
        final boolean visit = detector.visit(expression, constructorMethodDefinition);
        //then
        assertThat(visit).isFalse();
    }

    @Test
    public void handleFieldInSameClass() {
        //given
        final String fieldName = hasFieldInSameClass();
        //when
        final boolean visit = detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(visit).isTrue();
        assertThat(usedByMethod).containsOnlyKeys(nonPrivateMethodSignature);
        assertThat(usedByMethod.get(nonPrivateMethodSignature)).contains(fieldName);
    }

    private String hasFieldInSameClass() {
        hasNonPrivateNonBeanMethod();
        final String fieldName = randomString();
        setAsFieldReference(fieldName);
        setAsInSameClass(fieldReference, true);
        return fieldName;
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
        final boolean visit = detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(visit).isTrue();
        assertThat(usedByMethod).isEmpty();
    }

    @Test
    public void handleMethodCallInSameClass() {
        //given
        hasMethodCallInSameClass();
        //when
        final boolean visit = detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(visit).isTrue();
        assertThat(usedByMethod).containsOnlyKeys(nonPrivateMethodSignature);
        assertThat(usedByMethod.get(nonPrivateMethodSignature)).contains(privateMethodSignature);
    }

    private void hasMethodCallInSameClass() {
        hasNonPrivateNonBeanMethod();
        hasPrivateMethod();
        setAsMethodReference(privateMethodSignature);
        setAsInSameClass(methodReference, true);
    }

    private void setAsMethodReference(final String signature) {
        given(methodSignature.create(methodReference)).willReturn(signature);
        expression.setOperand(methodReference);
    }

    @Test
    public void ignoreMethodCallInOtherClass() {
        //given
        hasNonPrivateNonBeanMethod();
        hasPrivateMethod();
        setAsMethodReference(privateMethodSignature);
        setAsInSameClass(methodReference, false);
        //when
        final boolean visit = detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(visit).isTrue();
        assertThat(usedByMethod).isEmpty();
    }

    @Test
    public void methodCanUseMulitpleItems() {
        //given
        hasMethodCallInSameClass();
        //when
        final boolean visit1 = detector.visit(expression, nonPrivateMethodDefinition);
        //given
        final String fieldName = hasFieldInSameClass();
        //when
        final boolean visit2 = detector.visit(expression, nonPrivateMethodDefinition);
        //then
        assertThat(visit1).isTrue();
        assertThat(visit2).isTrue();
        assertThat(usedByMethod).containsOnlyKeys(nonPrivateMethodSignature);
        assertThat(usedByMethod.get(nonPrivateMethodSignature)).contains(privateMethodSignature, fieldName);
    }

    @Test
    public void skipClassesNamedImmutable() {
        //given
        given(typeDefinitionWrapper.getName(typeDefinition)).willReturn("ImmutableClass");
        //when
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isFalse();
    }

    @Test
    public void writesClassNameToConsole() {
        //given
        final String className = hasClassName();
        //when
        final boolean init = detector.init(typeDefinition);
        //then
        assertThat(init).isTrue();
        then(console).should().println("Class: " + className);
    }

    private String hasClassName() {
        final String className = randomString();
        given(typeDefinitionWrapper.getFullName(typeDefinition)).willReturn(className);
        return className;
    }
}
