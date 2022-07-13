package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.SecurityMode.Mode.CONSCRYPT;

import org.junit.Test;
import org.robolectric.annotation.SecurityMode;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class ConscryptSecurityModeTest {

    @Test
    @SecurityMode(CONSCRYPT)
    public void ensureConscryptInstalled() throws CertificateException {
//    bootstrapWrapper.callSetUpApplicationState();
//    MessageDigest digest = MessageDigest.getInstance("SHA256");
//    assertThat(digest.getProvider().getName()).isEqualTo(CONSCRYPT_PROVIDER);
//
//    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//    assertThat(aesCipher.getProvider().getName()).isEqualTo(CONSCRYPT_PROVIDER);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        assertThat(factory.getProvider().getName()).isEqualTo("Conscrypt");

    }

}
