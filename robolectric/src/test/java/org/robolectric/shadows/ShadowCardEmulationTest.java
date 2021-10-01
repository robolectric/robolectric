package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

/** Test the shadow implementation of {@link CardEmulation}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowCardEmulationTest {

  private static final String TEST_CATEGORY = "test_category";

  private Activity activity;
  private CardEmulation cardEmulation;
  private ComponentName service;

  @Before
  public void setUp() throws Exception {
    Application context = ApplicationProvider.getApplicationContext();
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_NFC, /* supported= */ true);
    shadowOf(context.getPackageManager())
        .setSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION, /* supported= */ true);
    NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
    cardEmulation = CardEmulation.getInstance(adapter);
    service = new ComponentName(context, "my_service");
    activity = Robolectric.buildActivity(Activity.class).setup().get();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public void isDefaultServiceForCategory_canOverride() {
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isFalse();
    ShadowCardEmulation.setDefaultServiceForCategory(service, TEST_CATEGORY);
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isTrue();
    ShadowCardEmulation.setDefaultServiceForCategory(null, TEST_CATEGORY);
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public void setPreferredService_canCapture() {
    assertThat(ShadowCardEmulation.getPreferredService() == null).isTrue();
    cardEmulation.setPreferredService(activity, service);
    assertThat(ShadowCardEmulation.getPreferredService().equals(service)).isTrue();
    cardEmulation.unsetPreferredService(activity);
    assertThat(ShadowCardEmulation.getPreferredService() == null).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public void categoryAllowsForegroundPreference_canSet() {
    assertThat(cardEmulation.categoryAllowsForegroundPreference(CardEmulation.CATEGORY_PAYMENT))
        .isFalse();
    ShadowCardEmulation.setCategoryPaymentAllowsForegroundPreference(true);
    assertThat(cardEmulation.categoryAllowsForegroundPreference(CardEmulation.CATEGORY_PAYMENT))
        .isTrue();
    ShadowCardEmulation.setCategoryPaymentAllowsForegroundPreference(false);
    assertThat(cardEmulation.categoryAllowsForegroundPreference(CardEmulation.CATEGORY_PAYMENT))
        .isFalse();
  }
}
