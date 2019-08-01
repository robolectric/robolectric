package org.robolectric.shadows;

import android.security.AttestedKeyPair;
import java.security.KeyPair;
import java.security.cert.Certificate;

/** Factory to create AttestedKeyPair. */
public class AttestedKeyPairFactory {

  /** Create AttestedKeyPair. */
  public static AttestedKeyPair create(KeyPair keyPair, Certificate[] attestationRecord) {
    return new AttestedKeyPair(keyPair, attestationRecord);
  }
}