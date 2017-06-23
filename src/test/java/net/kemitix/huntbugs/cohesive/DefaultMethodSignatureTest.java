package net.kemitix.huntbugs.cohesive;

import com.strobel.assembler.metadata.MemberReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Tests for {@link DefaultMethodSignature}.
 *
 * @author Paul Campbell (pcampbell@kemitix.net)
 */
public class DefaultMethodSignatureTest {

    @Mock
    private MemberReference memberReference;

    private DefaultMethodSignature methodSignature;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        methodSignature = new DefaultMethodSignature();
    }

    @Test
    public void canCreateSignature() {
        //given
        final String name = randomText();
        given(memberReference.getName()).willReturn(name);
        final String signature = randomText();
        given(memberReference.getSignature()).willReturn(signature);
        //when
        final String result = methodSignature.create(memberReference);
        //then
        assertThat(result).isEqualTo(name + signature);
    }

    private String randomText() {
        return UUID.randomUUID().toString();
    }

}
