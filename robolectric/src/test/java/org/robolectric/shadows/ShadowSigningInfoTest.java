package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowSigningInfo}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.P)
public final class ShadowSigningInfoTest {
  @Test
  public void testParceling_preservesCurrentSignatures() throws Exception {
    Signature[] signatures = { new Signature("0123"), new Signature("4657") };
    SigningInfo signingInfo = Shadow.newInstanceOf(SigningInfo.class);
    shadowOf(signingInfo).setSignatures(signatures);

    SigningInfo copy = copyViaParcel(signingInfo);

    assertThat(signingInfo.getApkContentsSigners()).isEqualTo(signatures);
    assertThat(copy.getSigningCertificateHistory()).isNull();
    assertThat(copy.hasPastSigningCertificates()).isFalse();
    assertThat(copy.hasMultipleSigners()).isTrue();
  }

  @Test
  public void testParceling_preservesPastSigningCertificates() throws Exception {
    Signature[] signatures = { new Signature("0123")};
    Signature[] pastSignatures = { new Signature("0123"), new Signature("4567") };
    SigningInfo signingInfo = Shadow.newInstanceOf(SigningInfo.class);
    shadowOf(signingInfo).setSignatures(signatures);
    shadowOf(signingInfo).setPastSigningCertificates(pastSignatures);

    SigningInfo copy = copyViaParcel(signingInfo);

    assertThat(signingInfo.getApkContentsSigners()).isEqualTo(signatures);
    assertThat(copy.getSigningCertificateHistory()).isEqualTo(pastSignatures);
    assertThat(copy.hasPastSigningCertificates()).isTrue();
    assertThat(copy.hasMultipleSigners()).isFalse();
  }

  private static SigningInfo copyViaParcel(SigningInfo orig) {
    Parcel parcel = Parcel.obtain();
    orig.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    return SigningInfo.CREATOR.createFromParcel(parcel);
  }
}
