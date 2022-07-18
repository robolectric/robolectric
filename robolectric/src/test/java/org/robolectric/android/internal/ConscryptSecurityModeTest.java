package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.SecurityMode.Mode.CONSCRYPT;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.SecurityMode;

@RunWith(RobolectricTestRunner.class)
public class ConscryptSecurityModeTest {

  @Test
  @SecurityMode(CONSCRYPT)
  public void ensureConscryptInstalled() throws CertificateException {

    CertificateFactory factory = CertificateFactory.getInstance("X.509");
    assertThat(factory.getProvider().getName()).isEqualTo("Conscrypt");
  }
}
