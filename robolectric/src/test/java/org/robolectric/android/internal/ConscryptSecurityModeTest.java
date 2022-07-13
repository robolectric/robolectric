package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.annotation.SecurityMode.Mode.CONSCRYPT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BootstrapDeferringRobolectricTestRunner;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.SecurityMode;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

@RunWith(BootstrapDeferringRobolectricTestRunner.class)
@LooperMode(LEGACY)
public class ConscryptSecurityModeTest {

    @Test
    @SecurityMode(CONSCRYPT)
    public void ensureConscryptInstalled() throws CertificateException {

        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        assertThat(factory.getProvider().getName()).isEqualTo("Conscrypt");

    }

}
