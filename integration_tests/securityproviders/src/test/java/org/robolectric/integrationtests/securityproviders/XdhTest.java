package org.robolectric.integrationtests.securityproviders;

import static com.google.common.truth.Truth.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.NamedParameterSpec;
import javax.crypto.KeyAgreement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.ConscryptMode;

/**
 * Regression tests for https://github.com/robolectric/robolectric/issues/11345. Conscrypt's XDH
 * KeyPairGenerator cannot be initialized with an {@link java.security.spec.AlgorithmParameterSpec},
 * so leaving it installed breaks unrelated HTTPS connections, such as the android-all SDK
 * downloads.
 */
@RunWith(RobolectricTestRunner.class)
@ConscryptMode(ConscryptMode.Mode.ON)
public class XdhTest {

  @Test
  public void conscryptIsTheHighestPriorityProvider() {
    assertThat(Security.getProviders()[0].getName()).isAnyOf("Conscrypt", "AndroidOpenSSL");
  }

  @Test
  public void xdhKeyPairGenerator_canBeInitializedWithNamedParameterSpec() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("XDH");

    // Would throw InvalidAlgorithmParameterException if Conscrypt's XDH generator were installed.
    keyPairGenerator.initialize(NamedParameterSpec.X25519);

    assertThat(keyPairGenerator.generateKeyPair()).isNotNull();
  }

  @Test
  public void x25519KeyAgreement_worksEndToEnd() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("XDH");
    keyPairGenerator.initialize(NamedParameterSpec.X25519);
    KeyPair alice = keyPairGenerator.generateKeyPair();
    KeyPair bob = keyPairGenerator.generateKeyPair();

    assertThat(agree(alice, bob)).isEqualTo(agree(bob, alice));
  }

  private static byte[] agree(KeyPair self, KeyPair peer) throws Exception {
    KeyAgreement keyAgreement = KeyAgreement.getInstance("XDH");
    keyAgreement.init(self.getPrivate());
    keyAgreement.doPhase(peer.getPublic(), /* lastPhase= */ true);
    return keyAgreement.generateSecret();
  }
}
