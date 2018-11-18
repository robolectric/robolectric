package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Junit test for {@link ShadowCarrierConfigManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowCarrierConfigManagerTest {

  private CarrierConfigManager carrierConfigManager;

  private static final int TEST_ID = 123;

  @Before
  public void setUp() {
    carrierConfigManager =
        (CarrierConfigManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
  }

  @Test
  public void getConfigForSubId_shouldReturnNonNullValue() throws Exception {
    PersistableBundle persistableBundle = carrierConfigManager.getConfigForSubId(-1);
    assertThat(persistableBundle).isNotNull();
  }

  @Test
  public void testGetConfigForSubId() throws Exception {
    PersistableBundle persistableBundle = new PersistableBundle();
    persistableBundle.putString("key1", "test");
    persistableBundle.putInt("key2", 100);
    persistableBundle.putBoolean("key3", true);

    shadowOf(carrierConfigManager).setConfigForSubId(TEST_ID, persistableBundle);

    PersistableBundle verifyBundle = carrierConfigManager.getConfigForSubId(TEST_ID);
    assertThat(verifyBundle).isNotNull();

    assertThat(verifyBundle.get("key1")).isEqualTo("test");
    assertThat(verifyBundle.getInt("key2")).isEqualTo(100);
    assertThat(verifyBundle.getBoolean("key3")).isTrue();
  }
}
