package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Parcel;
import android.os.Parcelable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = SigningInfo.class, minSdk = P)
public class ShadowSigningInfo {
  public static final Parcelable.Creator<SigningInfo> CREATOR =
      new Parcelable.Creator<SigningInfo>() {
        @Override
        public SigningInfo createFromParcel(Parcel in) {
          SigningInfo signingInfo = Shadow.newInstanceOf(SigningInfo.class);
          shadowOf(signingInfo).setSignatures(in.createTypedArray(Signature.CREATOR));
          shadowOf(signingInfo).setPastSigningCertificates(in.createTypedArray(Signature.CREATOR));
          return signingInfo;
        }

        @Override
        public SigningInfo[] newArray(int size) {
          return new SigningInfo[size];
        }
      };

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

  @Implementation
  public void writeToParcel(Parcel parcel, int flags) {
    // Overwrite the CREATOR so that we can simulate reading from parcel.
    ReflectionHelpers.setStaticField(SigningInfo.class, "CREATOR", CREATOR);

    parcel.writeTypedArray(signatures, flags);
    parcel.writeTypedArray(pastSigningCertificates, flags);
  }
}
