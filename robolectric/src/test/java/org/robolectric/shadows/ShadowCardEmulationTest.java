package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.V;

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
  public void isDefaultServiceForCategory_canOverride() {
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isFalse();
    ShadowCardEmulation.setDefaultServiceForCategory(service, TEST_CATEGORY);
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isTrue();
    ShadowCardEmulation.setDefaultServiceForCategory(null, TEST_CATEGORY);
    assertThat(cardEmulation.isDefaultServiceForCategory(service, TEST_CATEGORY)).isFalse();
  }

  @Test
  public void setPreferredService_canCapture() {
    assertThat(ShadowCardEmulation.getPreferredService()).isNull();
    cardEmulation.setPreferredService(activity, service);
    assertThat(ShadowCardEmulation.getPreferredService()).isEqualTo(service);
    cardEmulation.unsetPreferredService(activity);
    assertThat(ShadowCardEmulation.getPreferredService()).isNull();
  }

  @Test
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

  @Test
  @Config(minSdk = V.SDK_INT)
  public void getShouldDefaultToObserveModeForService_shouldReturnDefaultToObserveMode() {
    final CardEmulationVReflector cardEmulationVReflector =
        reflector(CardEmulationVReflector.class, cardEmulation);
    assertThat(ShadowCardEmulation.getShouldDefaultToObserveModeForService(service)).isFalse();

    cardEmulationVReflector.setShouldDefaultToObserveModeForService(service, true);
    assertThat(ShadowCardEmulation.getShouldDefaultToObserveModeForService(service)).isTrue();

    cardEmulationVReflector.setShouldDefaultToObserveModeForService(service, false);
    assertThat(ShadowCardEmulation.getShouldDefaultToObserveModeForService(service)).isFalse();
  }

  // TODO: delete when this test compiles against V sdk
  @ForType(CardEmulation.class)
  interface CardEmulationVReflector {
    boolean setShouldDefaultToObserveModeForService(ComponentName component, boolean enable);
  }
}
