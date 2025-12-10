package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.CardEmulation;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;

/** Test the shadow implementation of {@link CardEmulation}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowCardEmulationTest {

  private static final String TEST_CATEGORY = "test_category";

  private Activity activity;
  private CardEmulation cardEmulation;
  private ComponentName service;
  private ShadowCardEmulation shadowCardEmulation;

  private Executor executor = MoreExecutors.directExecutor();

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
    shadowCardEmulation = shadowOf(cardEmulation);
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
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void registerPollingLoopPatternFilterForService_shouldRegisterPollingLoopFilter() {
    String pollingLoopFilter = "6A02..01.*";
    shadowCardEmulation.registerPollingLoopPatternFilterForService(
        service, pollingLoopFilter, true);

    Map<String, Boolean> result =
        ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service);
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.containsKey(pollingLoopFilter)).isTrue();
    assertThat(result.get(pollingLoopFilter)).isTrue();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void registerPollingLoopPatternFilterForService_shouldOverrideSamePollingLoopFilter() {
    // Register a polling loop filter with auto-transact enabled.
    String pollingLoopFilter = "6A02..01.*";
    cardEmulation.registerPollingLoopPatternFilterForService(service, pollingLoopFilter, true);

    Map<String, Boolean> result =
        ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service);
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.containsKey(pollingLoopFilter)).isTrue();
    assertThat(result.get(pollingLoopFilter)).isTrue();

    // Register the same polling loop filter with auto-transact disabled.
    cardEmulation.registerPollingLoopPatternFilterForService(service, pollingLoopFilter, false);
    result = ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service);
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.containsKey(pollingLoopFilter)).isTrue();
    assertThat(result.get(pollingLoopFilter)).isFalse();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void
      registerPollingLoopPatternFilterForService_shouldRegisterMultiplePollingLoopFilters() {
    // Register multiple polling loop filter with auto-transact enabled.
    String pollingLoopFilter1 = "6A02..01.*";
    String pollingLoopFilter2 = "6A03..02.*";
    cardEmulation.registerPollingLoopPatternFilterForService(service, pollingLoopFilter1, true);
    cardEmulation.registerPollingLoopPatternFilterForService(service, pollingLoopFilter2, false);

    Map<String, Boolean> result =
        ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service);
    assertThat(result.keySet().size()).isEqualTo(2);
    assertThat(result.containsKey(pollingLoopFilter1)).isTrue();
    assertThat(result.containsKey(pollingLoopFilter2)).isTrue();
    assertThat(result.get(pollingLoopFilter1)).isTrue();
    assertThat(result.get(pollingLoopFilter2)).isFalse();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void registerPollingLoopPatternFilterForService_returnsFalseForInvalidPollingLoopFilter() {
    String invalidPollingLoopFilter = "6Z02!.01.*";
    assertThat(
            cardEmulation.registerPollingLoopPatternFilterForService(
                service, invalidPollingLoopFilter, true))
        .isFalse();

    assertThat(ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service))
        .isNull();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void removePollingLoopPatternFilterForService_shouldRemovePollingLoopFilter() {
    String pollingLoopFilter = "6A02..01.*";
    cardEmulation.registerPollingLoopPatternFilterForService(service, pollingLoopFilter, true);
    Map<String, Boolean> result =
        ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service);
    assertThat(result.keySet().size()).isEqualTo(1);
    assertThat(result.containsKey(pollingLoopFilter)).isTrue();
    assertThat(result.get(pollingLoopFilter)).isTrue();

    cardEmulation.removePollingLoopPatternFilterForService(service, pollingLoopFilter);
    assertThat(ShadowCardEmulation.getRegisteredPollingLoopPatternFiltersForService(service))
        .isEmpty();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void removePollingLoopPatternFilterForService_returnsFalseForInvalidPollingLoopFilter() {
    String invalidPollingLoopFilter = "6Z02!.01.*";

    assertThat(
            cardEmulation.removePollingLoopPatternFilterForService(
                service, invalidPollingLoopFilter))
        .isFalse();
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void registerNfcEventCallback_shouldRegisterNfcEventCallback() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);

    assertThat(shadowCardEmulation.getNfcEventCallbackListeners().keySet())
        .containsExactly(testCallback);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void unregisterNfcEventCallback_shouldRegisterNfcEventCallback() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);
    assertThat(shadowCardEmulation.getNfcEventCallbackListeners().keySet())
        .containsExactly(testCallback);

    cardEmulation.unregisterNfcEventCallback(testCallback);
    assertThat(shadowCardEmulation.getNfcEventCallbackListeners()).isEmpty();
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnNfcStateChanged_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);

    shadowCardEmulation.triggerOnNfcStateChanged(NfcAdapter.STATE_ON);

    verify(testCallback).onNfcStateChanged(NfcAdapter.STATE_ON);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnRemoteFieldChanged_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);

    shadowCardEmulation.triggerOnRemoteFieldChanged(/* isDetected= */ true);

    verify(testCallback).onRemoteFieldChanged(/* isDetected= */ true);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnAidConflictOccurred_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);
    String testAid = "0000";

    shadowCardEmulation.triggerOnAidConflictOccurred(testAid);

    verify(testCallback).onAidConflictOccurred(testAid);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnAidNotRouted_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);
    String testAid = "0000";

    shadowCardEmulation.triggerOnAidNotRouted(testAid);

    verify(testCallback).onAidNotRouted(testAid);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnInternalErrorReported_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);
    int error = CardEmulation.NFC_INTERNAL_ERROR_NFC_HARDWARE_ERROR;

    shadowCardEmulation.triggerOnInternalErrorReported(error);

    verify(testCallback).onInternalErrorReported(error);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnPreferredServiceChanged_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);

    shadowCardEmulation.triggerOnPreferredServiceChanged(true);

    verify(testCallback).onPreferredServiceChanged(true);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void triggerOnObserveModeStateChanged_shouldTriggerNfcEventCallbackListeners() {
    CardEmulation.NfcEventCallback testCallback = mock(CardEmulation.NfcEventCallback.class);
    cardEmulation.registerNfcEventCallback(executor, testCallback);

    shadowCardEmulation.triggerOnObserveModeStateChanged(false);

    verify(testCallback).onObserveModeStateChanged(false);
  }

  // TODO: delete when this test compiles against V sdk
  @ForType(CardEmulation.class)
  interface CardEmulationVReflector {
    boolean setShouldDefaultToObserveModeForService(ComponentName component, boolean enable);
  }
}
