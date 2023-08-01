package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.Context;
import android.graphics.Rect;
import android.os.Parcel;
import android.view.displayhash.DisplayHash;
import android.view.displayhash.DisplayHashManager;
import android.view.displayhash.VerifiedDisplayHash;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
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
    // Default value is PHASH
    assertThat(displayHashManager.getSupportedHashAlgorithms()).containsExactly("PHASH");

    ShadowDisplayHashManager.setSupportedHashAlgorithms(ImmutableSet.of("TESTHASH"));
    assertThat(displayHashManager.getSupportedHashAlgorithms()).containsExactly("TESTHASH");
  }

  @Test
  public void verifyDisplayHash() {
    DisplayHash displayHash = createDisplayHash();

    assertThat(displayHashManager.verifyDisplayHash(displayHash)).isNull();

    VerifiedDisplayHash verifiedDisplayHash =
        new VerifiedDisplayHash(54321L, new Rect(0, 0, 100, 100), "PHASH", new byte[8]);
    ShadowDisplayHashManager.setVerifyDisplayHashResult(verifiedDisplayHash);
    assertThat(displayHashManager.verifyDisplayHash(displayHash)).isEqualTo(verifiedDisplayHash);
  }

  private DisplayHash createDisplayHash() {
    Parcel parcel = Parcel.obtain();
    parcel.writeLong(12345L);
    parcel.writeTypedObject(new Rect(0, 0, 100, 100), 0);
    parcel.writeString("PHASH");
    parcel.writeByteArray(new byte[15]);
    parcel.writeByteArray(new byte[21]);
    parcel.setDataPosition(0);
    return DisplayHash.CREATOR.createFromParcel(parcel);
  }

  @Test
  public void testSetSupportedHashAlgorithmsToNull() {
    Set<String> previousSupportedHashAlgorithms = displayHashManager.getSupportedHashAlgorithms();
    ShadowDisplayHashManager.setSupportedHashAlgorithms(previousSupportedHashAlgorithms);
    ShadowDisplayHashManager.setSupportedHashAlgorithms(null);
    assertThrows(NullPointerException.class, () -> displayHashManager.getSupportedHashAlgorithms());

    // Restore previous value
    ShadowDisplayHashManager.setSupportedHashAlgorithms(previousSupportedHashAlgorithms);
  }
}
