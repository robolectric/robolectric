package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.view.displayhash.DisplayHash;
import android.view.displayhash.DisplayHashManager;
import android.view.displayhash.VerifiedDisplayHash;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowDisplayHashManager}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = 31)
public final class ShadowDisplayHashManagerTest {

  private DisplayHashManager displayHashManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();

    displayHashManager = context.getSystemService(DisplayHashManager.class);
  }

  @Test
  public void getSupportedHashAlgorithms() {
    assertThat(displayHashManager.getSupportedHashAlgorithms()).containsExactly("PHASH");
  }

  @Test
  public void verifyDisplayHash() {
    Parcel parcel = Parcel.obtain();
    parcel.writeLong(12345L);
    parcel.writeTypedObject(new Rect(0, 0, 100, 100), 0);
    parcel.writeString("PHASH");
    parcel.writeByteArray(new byte[15]);
    parcel.writeByteArray(new byte[21]);
    parcel.setDataPosition(0);
    DisplayHash displayHash = DisplayHash.CREATOR.createFromParcel(parcel);

    assertThat(displayHashManager.verifyDisplayHash(displayHash)).isNull();

    VerifiedDisplayHash verifiedDisplayHash =
        new VerifiedDisplayHash(54321L, new Rect(0, 0, 100, 100), "PHASH", new byte[8]);
    ShadowDisplayHashManager.setVerifyDisplayHashResult(verifiedDisplayHash);
    assertThat(displayHashManager.verifyDisplayHash(displayHash)).isEqualTo(verifiedDisplayHash);
  }
}
