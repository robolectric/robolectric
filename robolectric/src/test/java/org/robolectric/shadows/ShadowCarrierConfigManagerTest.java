package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.telephony.PhoneConstants;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadows.ShadowSubscriptionManager.SubscriptionInfoBuilder;

/** Junit test for {@link ShadowCarrierConfigManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowCarrierConfigManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private CarrierConfigManager carrierConfigManager;

  private static final String STRING_KEY = "key1";
  private static final String STRING_VALUE = "test";
  private static final String STRING_OVERRIDE_VALUE = "override";
  private static final String INT_KEY = "key2";
  private static final int INT_VALUE = 100;
  private static final String BOOLEAN_KEY = "key3";
  private static final boolean BOOLEAN_VALUE = true;

  private static final int SUB_ID_1 = 1;
  private static final int LOGICAL_SLOT_INDEX_0 = 0;
  private static final int LOGICAL_SLOT_INDEX_1 = 1;
  private static final int CARRIER_ID = 1989;
  private static final int SPECIFIC_CARRIER_ID = 10014;

  private static final SubscriptionInfo subInfo =
      SubscriptionInfoBuilder.newBuilder()
          .setId(SUB_ID_1)
          .setSimSlotIndex(LOGICAL_SLOT_INDEX_1)
          .buildSubscriptionInfo();

  private final ShadowApplication shadowApplication =
      shadowOf((Application) getApplicationContext());
  private final ShadowSubscriptionManager shadowSubscriptionManager =
      shadowOf(
          (SubscriptionManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE));
  private final ShadowTelephonyManager shadowTelephonyManager =
      shadowOf(
          (TelephonyManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.TELEPHONY_SERVICE));

  @Before
  public void setUp() {
    carrierConfigManager =
        (CarrierConfigManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CARRIER_CONFIG_SERVICE);
  }

  @Test
  @Config(maxSdk = O_MR1)
  public void setConfigForSubId_untilSdkO_subInfoConfigured_sendsBroadcastWithCorrectExtras() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(ImmutableList.of(subInfo));

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1,
        LOGICAL_SLOT_INDEX_1,
        // These won't actually be set/verified since they're only on P/Q+ respectively.
        /* expectedCarrierId= */ TelephonyManager.UNKNOWN_CARRIER_ID,
        /* expectedSpecificCarrierId= */ TelephonyManager.UNKNOWN_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  @Config(sdk = P)
  public void
      setConfigForSubId_sdkP_subInfoAndCarrierIdConfigured_sendsBroadcastWithCorrectExtras() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(ImmutableList.of(subInfo));
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
    when(mockTelephonyManager.getSimCarrierId()).thenReturn(CARRIER_ID);
    shadowTelephonyManager.setTelephonyManagerForSubscriptionId(SUB_ID_1, mockTelephonyManager);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    // In addition to the carrier ID, SDK P also begins to set the unified slot and sub ID extras.
    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1,
        LOGICAL_SLOT_INDEX_1,
        CARRIER_ID,
        // This won't actually be set/verified since it's only on Q+.
        /* expectedSpecificCarrierId= */ TelephonyManager.UNKNOWN_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void
      setConfigForSubId_fromSdkQ_subInfoAndCarrierIdsConfigured_sendsBroadcastWithCorrectExtras() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(ImmutableList.of(subInfo));
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
    when(mockTelephonyManager.getSimCarrierId()).thenReturn(CARRIER_ID);
    when(mockTelephonyManager.getSimSpecificCarrierId()).thenReturn(SPECIFIC_CARRIER_ID);
    shadowTelephonyManager.setTelephonyManagerForSubscriptionId(SUB_ID_1, mockTelephonyManager);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1, LOGICAL_SLOT_INDEX_1, CARRIER_ID, SPECIFIC_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  public void
      setConfigForSubId_subInfoAndCarrierIdsNotConfigured_broadcastDefaultsToLogicalSlot0() {
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1,
        LOGICAL_SLOT_INDEX_0,
        // These extras are only checked if the test is running on the appropriate SDK.
        TelephonyManager.UNKNOWN_CARRIER_ID,
        TelephonyManager.UNKNOWN_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void
      setConfigForSubId_fromSdkU_withListener_subInfoAndTelephonyManagerConfigured_invokesCallbackAndSendsBroadcast() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(ImmutableList.of(subInfo));
    TelephonyManager mockTelephonyManager = mock(TelephonyManager.class);
    when(mockTelephonyManager.getSimCarrierId()).thenReturn(CARRIER_ID);
    when(mockTelephonyManager.getSimSpecificCarrierId()).thenReturn(SPECIFIC_CARRIER_ID);
    shadowTelephonyManager.setTelephonyManagerForSubscriptionId(SUB_ID_1, mockTelephonyManager);
    TestOneShotCarrierConfigChangeListener carrierConfigChangeListener =
        new TestOneShotCarrierConfigChangeListener();
    carrierConfigManager.registerCarrierConfigChangeListener(
        directExecutor(), carrierConfigChangeListener);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertThat(carrierConfigChangeListener.onCarrierConfigChangedCount.get()).isEqualTo(1);
    assertThat(carrierConfigChangeListener.mostRecentSlotIndex).isEqualTo(1);
    assertThat(carrierConfigChangeListener.mostRecentSubId).isEqualTo(SUB_ID_1);
    assertThat(carrierConfigChangeListener.mostRecentCarrierId).isEqualTo(CARRIER_ID);
    assertThat(carrierConfigChangeListener.mostRecentSpecificCarrierId)
        .isEqualTo(SPECIFIC_CARRIER_ID);
    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1, LOGICAL_SLOT_INDEX_1, CARRIER_ID, SPECIFIC_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void
      setConfigForSubId_fromSdkU_withListener_subInfoAndCarrierIdsNotConfigured_invokesCallbackAndSendsBroadcastForLogicalSlot0() {
    TestOneShotCarrierConfigChangeListener carrierConfigChangeListener =
        new TestOneShotCarrierConfigChangeListener();
    carrierConfigManager.registerCarrierConfigChangeListener(
        directExecutor(), carrierConfigChangeListener);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertThat(carrierConfigChangeListener.onCarrierConfigChangedCount.get()).isEqualTo(1);
    assertThat(carrierConfigChangeListener.mostRecentSlotIndex).isEqualTo(LOGICAL_SLOT_INDEX_0);
    assertThat(carrierConfigChangeListener.mostRecentSubId).isEqualTo(SUB_ID_1);
    assertThat(carrierConfigChangeListener.mostRecentCarrierId)
        .isEqualTo(TelephonyManager.UNKNOWN_CARRIER_ID);
    assertThat(carrierConfigChangeListener.mostRecentSpecificCarrierId)
        .isEqualTo(TelephonyManager.UNKNOWN_CARRIER_ID);
    assertSingleCarrierConfigChangedBroadcastReceived(
        SUB_ID_1,
        LOGICAL_SLOT_INDEX_0,
        TelephonyManager.UNKNOWN_CARRIER_ID,
        TelephonyManager.UNKNOWN_CARRIER_ID);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).isEmpty()).isTrue();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void
      registerCarrierConfigChangeListener_registersCallback_callbackInvokedByConfigUpdate() {
    TestOneShotCarrierConfigChangeListener carrierConfigChangeListener =
        new TestOneShotCarrierConfigChangeListener();
    carrierConfigManager.registerCarrierConfigChangeListener(
        directExecutor(), carrierConfigChangeListener);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());

    assertThat(carrierConfigChangeListener.onCarrierConfigChangedCount.get()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void unregisterCarrierConfigChangeListener_callbackNoLongerGetsInvokedByConfigUpdates() {
    TestOneShotCarrierConfigChangeListener carrierConfigChangeListener =
        new TestOneShotCarrierConfigChangeListener();
    carrierConfigManager.registerCarrierConfigChangeListener(
        directExecutor(), carrierConfigChangeListener);

    // Sanity check an initial callback invocation.
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());
    assertThat(carrierConfigChangeListener.onCarrierConfigChangedCount.get()).isEqualTo(1);

    // Now unregister the callback.
    carrierConfigManager.unregisterCarrierConfigChangeListener(carrierConfigChangeListener);

    // New updates should not trigger the callback.
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, new PersistableBundle());
    assertThat(carrierConfigChangeListener.onCarrierConfigChangedCount.get()).isEqualTo(1);
  }

  @Test
  public void getConfigForSubId_bundleNotConfigured_returnsEmpty() {
    PersistableBundle persistableBundle =
        carrierConfigManager.getConfigForSubId(SubscriptionManager.INVALID_SUBSCRIPTION_ID);

    assertThat(persistableBundle.isEmpty()).isTrue();
  }

  @Test
  public void getConfigForSubId_validBundleExists_returnsBundle() {
    PersistableBundle persistableBundle = new PersistableBundle();
    persistableBundle.putString(STRING_KEY, STRING_VALUE);
    persistableBundle.putInt(INT_KEY, INT_VALUE);
    persistableBundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, persistableBundle);

    PersistableBundle verifyBundle = carrierConfigManager.getConfigForSubId(SUB_ID_1);
    assertThat(verifyBundle).isNotNull();

    assertThat(verifyBundle.get(STRING_KEY)).isEqualTo(STRING_VALUE);
    assertThat(verifyBundle.getInt(INT_KEY)).isEqualTo(INT_VALUE);
    assertThat(verifyBundle.getBoolean(BOOLEAN_KEY)).isEqualTo(BOOLEAN_VALUE);
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getConfigForSubId_specificKeysOverload_returnsOnlySpecifiedKeys() {
    PersistableBundle persistableBundle = new PersistableBundle();
    persistableBundle.putString(STRING_KEY, STRING_VALUE);
    persistableBundle.putInt(INT_KEY, INT_VALUE);

    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, persistableBundle);

    PersistableBundle verifyBundle =
        carrierConfigManager.getConfigForSubId(SUB_ID_1, INT_KEY, BOOLEAN_KEY);
    assertThat(verifyBundle).isNotNull();

    assertThat(verifyBundle.keySet())
        .containsExactly(
            CarrierConfigManager.KEY_CARRIER_CONFIG_VERSION_STRING,
            CarrierConfigManager.KEY_CARRIER_CONFIG_APPLIED_BOOL,
            INT_KEY);
    assertThat(verifyBundle.getInt(INT_KEY)).isEqualTo(INT_VALUE);
  }

  @Test
  public void getConfigForSubId_nullBundleSet_returnsNull() {
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, /* config= */ null);

    PersistableBundle persistableBundle = carrierConfigManager.getConfigForSubId(SUB_ID_1);

    assertThat(persistableBundle).isNull();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void getConfigForSubId_specificKeysOverload_nullBundleSet_returnsBaseBundle() {
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, /* config= */ null);

    PersistableBundle verifyBundle =
        carrierConfigManager.getConfigForSubId(SUB_ID_1, INT_KEY, BOOLEAN_KEY);

    assertThat(verifyBundle).isNotNull();
    PersistableBundle baseBundle = ShadowCarrierConfigManager.BASE;
    assertThat(verifyBundle.keySet()).isEqualTo(baseBundle.keySet());
  }

  @Test
  public void overrideConfig_setNullConfig_removesOverride() {
    // Set value
    PersistableBundle existingBundle = new PersistableBundle();
    existingBundle.putString(STRING_KEY, STRING_VALUE);
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, existingBundle);
    // Set override value
    PersistableBundle overrideBundle = new PersistableBundle();
    overrideBundle.putString(STRING_KEY, STRING_OVERRIDE_VALUE);
    shadowOf(carrierConfigManager).overrideConfig(SUB_ID_1, overrideBundle);
    // Assert override is applied
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).get(STRING_KEY))
        .isEqualTo(STRING_OVERRIDE_VALUE);

    shadowOf(carrierConfigManager).overrideConfig(SUB_ID_1, null);

    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).get(STRING_KEY))
        .isEqualTo(STRING_VALUE);
  }

  @Test
  public void overrideConfig_setBundleWithValues_overridesExistingConfig() {
    PersistableBundle existingBundle = new PersistableBundle();
    existingBundle.putString(STRING_KEY, STRING_VALUE);
    shadowOf(carrierConfigManager).setConfigForSubId(SUB_ID_1, existingBundle);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1)).isNotNull();
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).get(STRING_KEY))
        .isEqualTo(STRING_VALUE);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).getInt(INT_KEY)).isEqualTo(0);
    assertThat(carrierConfigManager.getConfigForSubId(SUB_ID_1).getBoolean(BOOLEAN_KEY)).isFalse();

    PersistableBundle overrideBundle = new PersistableBundle();
    overrideBundle.putString(STRING_KEY, STRING_OVERRIDE_VALUE);
    overrideBundle.putInt(INT_KEY, INT_VALUE);
    overrideBundle.putBoolean(BOOLEAN_KEY, BOOLEAN_VALUE);
    shadowOf(carrierConfigManager).overrideConfig(SUB_ID_1, overrideBundle);

    PersistableBundle verifyBundle = carrierConfigManager.getConfigForSubId(SUB_ID_1);
    assertThat(verifyBundle).isNotNull();
    assertThat(verifyBundle.get(STRING_KEY)).isEqualTo(STRING_OVERRIDE_VALUE);
    assertThat(verifyBundle.getInt(INT_KEY)).isEqualTo(INT_VALUE);
    assertThat(verifyBundle.getBoolean(BOOLEAN_KEY)).isEqualTo(BOOLEAN_VALUE);
  }

  @Test
  @Config(minSdk = O)
  public void carrierConfigManager_activityContextEnabled_retrievesSameConfigs() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      CarrierConfigManager applicationCarrierConfigManager =
          (CarrierConfigManager)
              ApplicationProvider.getApplicationContext()
                  .getSystemService(Context.CARRIER_CONFIG_SERVICE);

      Activity activity = controller.get();
      CarrierConfigManager activityCarrierConfigManager =
          (CarrierConfigManager) activity.getSystemService(Context.CARRIER_CONFIG_SERVICE);

      assertThat(applicationCarrierConfigManager).isNotSameInstanceAs(activityCarrierConfigManager);

      int subId = SubscriptionManager.getDefaultSubscriptionId();

      PersistableBundle applicationConfigs =
          applicationCarrierConfigManager.getConfigForSubId(subId);
      PersistableBundle activityConfigs = activityCarrierConfigManager.getConfigForSubId(subId);

      Parcel applicationParcel = Parcel.obtain();
      Parcel activityParcel = Parcel.obtain();

      applicationConfigs.writeToParcel(applicationParcel, 0);
      activityConfigs.writeToParcel(activityParcel, 0);

      byte[] applicationBytes = applicationParcel.marshall();
      byte[] activityBytes = activityParcel.marshall();

      assertThat(activityBytes).isEqualTo(applicationBytes);

      applicationParcel.recycle();
      activityParcel.recycle();
    }
  }

  /**
   * Verifies that a single {@link CarrierConfigManager#ACTION_CARRIER_CONFIG_CHANGED} was
   * broadcasted with the provided extras.
   *
   * <p>Carrier ID extras are only verified on the SDKs where they're available.
   */
  private void assertSingleCarrierConfigChangedBroadcastReceived(
      int expectedSubId,
      int expectedSlotIndex,
      int expectedCarrierId,
      int expectedSpecificCarrierId) {
    List<Intent> intents = shadowApplication.getBroadcastIntents();
    assertThat(intents).hasSize(1);
    Intent intent = intents.get(0);

    assertThat(intent.getAction()).isEqualTo(CarrierConfigManager.ACTION_CARRIER_CONFIG_CHANGED);
    // Legacy phone constants are included in all SDKs.
    assertThat(
            intent.getIntExtra(
                PhoneConstants.PHONE_KEY, SubscriptionManager.INVALID_SIM_SLOT_INDEX))
        .isEqualTo(expectedSlotIndex);
    assertThat(
            intent.getIntExtra(
                PhoneConstants.SUBSCRIPTION_KEY, SubscriptionManager.INVALID_SUBSCRIPTION_ID))
        .isEqualTo(expectedSubId);
    if (Build.VERSION.SDK_INT >= O) {
      assertThat(
              intent.getIntExtra(
                  CarrierConfigManager.EXTRA_SUBSCRIPTION_INDEX,
                  SubscriptionManager.INVALID_SUBSCRIPTION_ID))
          .isEqualTo(expectedSubId);
    }
    if (Build.VERSION.SDK_INT >= P) {
      assertThat(
              intent.getIntExtra(
                  CarrierConfigManager.EXTRA_SLOT_INDEX,
                  SubscriptionManager.INVALID_SIM_SLOT_INDEX))
          .isEqualTo(expectedSlotIndex);
      // We assert for the presence here since the fallback value will match the default if it isn't
      // configured.
      assertThat(intent.hasExtra(TelephonyManager.EXTRA_CARRIER_ID)).isTrue();
      assertThat(
              intent.getIntExtra(
                  TelephonyManager.EXTRA_CARRIER_ID, TelephonyManager.UNKNOWN_CARRIER_ID))
          .isEqualTo(expectedCarrierId);
    }
    if (Build.VERSION.SDK_INT >= Q) {
      assertThat(intent.hasExtra(TelephonyManager.EXTRA_SPECIFIC_CARRIER_ID)).isTrue();
      assertThat(
              intent.getIntExtra(
                  TelephonyManager.EXTRA_SPECIFIC_CARRIER_ID, TelephonyManager.UNKNOWN_CARRIER_ID))
          .isEqualTo(expectedSpecificCarrierId);
    }
  }

  /**
   * Lightweight wrapper that provides the most recent values received by the callback intended for
   * verifying a one-off invocation.
   */
  static class TestOneShotCarrierConfigChangeListener
      implements CarrierConfigManager.CarrierConfigChangeListener {
    // Sanity check that the callback isn't being invoked multiple times.
    AtomicInteger onCarrierConfigChangedCount = new AtomicInteger(0);
    // Somewhat ugly, but it's not worth the Java boilerplate of creating a wrapper class.
    int mostRecentSlotIndex;
    int mostRecentSubId;
    int mostRecentCarrierId;
    int mostRecentSpecificCarrierId;

    @Override
    public void onCarrierConfigChanged(
        int slotIndex, int subId, int carrierId, int specificCarrierId) {
      onCarrierConfigChangedCount.incrementAndGet();
      mostRecentSlotIndex = slotIndex;
      mostRecentSubId = subId;
      mostRecentCarrierId = carrierId;
      mostRecentSpecificCarrierId = specificCarrierId;
    }
  }
}
