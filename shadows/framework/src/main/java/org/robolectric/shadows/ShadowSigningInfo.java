package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = SigningInfo.class, minSdk = P)
public class ShadowSigningInfo {
  private Signature[] signatures;
  private Signature[] pastSigningCertificates;

  /**
   * Set the current Signatures for this package. If signatures has a size greater than 1,
   * {@link #hasMultipleSigners} will be true and {@link #getSigningCertificateHistory} will return
   * null.
   */
  public void setSignatures(Signature[] signatures) {
    this.signatures = signatures;
  }

  /**
   * Sets the history of Signatures for this package.
   */
  public void setPastSigningCertificates(Signature[] pastSigningCertificates) {
    this.pastSigningCertificates = pastSigningCertificates;
  }

  @Implementation
  protected boolean hasMultipleSigners() {
    return signatures != null && signatures.length > 1;
  }

  @Implementation
  protected boolean hasPastSigningCertificates() {
    return signatures != null && pastSigningCertificates != null;
  }

  @Implementation
  protected Signature[] getSigningCertificateHistory() {
    if (hasMultipleSigners()) {
      return null;
    } else if (!hasPastSigningCertificates()) {
      // this package is only signed by one signer with no history, return it
      return signatures;
    } else {
      // this package has provided proof of past signing certificates, include them
      return pastSigningCertificates;
    }
  }

  @Implementation
  protected Signature[] getApkContentsSigners() {
    return signatures;
  }
}
