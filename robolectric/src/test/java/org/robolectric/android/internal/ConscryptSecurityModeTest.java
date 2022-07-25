package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ConscryptSecurityModeTest {

  @Test
  public void ensureConscryptInstalled() throws CertificateException {

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo("Conscrypt");
  }
}
