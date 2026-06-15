package org.robolectric.integrationtests.securityproviders;

import static com.google.common.truth.Truth.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Random;
import javax.crypto.Cipher;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** RSA-related tests. */
@RunWith(RobolectricTestRunner.class)
public class RsaTest {
  @Ignore("Re-enable when performing a benchmark.")
  @Test
  public void rsaBenchmark() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

    System.err.println("Using Cipher class " + cipher.getClass().getName());
    System.err.println(
        "Using Provider: "
            + cipher.getProvider().getName()
            + " ("
            + cipher.getProvider().getClass().getName()
            + ")");

    byte[] data = new byte[128];
    new Random(100).nextBytes(data);

    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
    for (int i = 0; i < 50_000; i++) {
      byte[] encrypted = cipher.doFinal(data);
      assertThat(encrypted).isNotEmpty();
    }
  }
}
