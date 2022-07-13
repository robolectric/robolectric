package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSubscriptionManager.SubscriptionInfoBuilder;

/** Test for {@link ShadowSubscriptionManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public class ShadowSubscriptionManagerTest {

  private SubscriptionManager subscriptionManager;

  @Before
  public void setUp() throws Exception {
    subscriptionManager =
        (SubscriptionManager)
            getApplicationContext().getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
  }

  @Test
  public void shouldGiveDefaultSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultDataSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultDataSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultDataSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultSmsSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultSmsSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultSmsSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultVoiceSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultVoiceSubscriptionId(testId);
    assertThat(SubscriptionManager.getDefaultVoiceSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void addOnSubscriptionsChangedListener_shouldCallbackImmediately() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);

    assertThat(listener.subscriptionChangedCount).isEqualTo(1);
  }

  @Test
  public void addOnSubscriptionsChangedListener_shouldAddListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);

    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount).isEqualTo(2);
  }

  @Test
  public void removeOnSubscriptionsChangedListener_shouldRemoveListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    DummySubscriptionsChangedListener listener2 = new DummySubscriptionsChangedListener();
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener);
    shadowOf(subscriptionManager).addOnSubscriptionsChangedListener(listener2);

    shadowOf(subscriptionManager).removeOnSubscriptionsChangedListener(listener);
    shadowOf(subscriptionManager)
        .setActiveSubscriptionInfos(
            SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChangedCount).isEqualTo(1);
    assertThat(listener2.subscriptionChangedCount).isEqualTo(2);
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnInfoWithSubId() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoList_shouldReturnInfoList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoList())
        .containsExactly(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoList_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class,
        () -> shadowOf(subscriptionManager).getActiveSubscriptionInfoList());
  }

  @Test
  public void getActiveSubscriptionInfoForSimSlotIndex_shouldReturnInfoWithSlotIndex() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setSimSlotIndex(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoForSimSlotIndex(123))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoForSimSlotIndex_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class,
        () -> shadowOf(subscriptionManager).getActiveSubscriptionInfoForSimSlotIndex(123));
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullVarargsList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfos((SubscriptionInfo[]) null);
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForEmptyList() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void isNetworkRoaming_shouldReturnTrueIfSet() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+unset. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseIfUnset() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();

    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ false);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isFalse();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+clear. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseOnClear() {
    shadowOf(subscriptionManager).setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isTrue();

    shadowOf(subscriptionManager).clearNetworkRoamingStatus();
    assertThat(shadowOf(subscriptionManager).isNetworkRoaming(123)).isFalse();
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnZeroIfActiveSubscriptionInfoListNotSet() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoCount()).isEqualTo(0);
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnSizeOfActiveSubscriptionInfosList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowOf(subscriptionManager).getActiveSubscriptionInfoCount()).isEqualTo(1);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_returnsSubscriptionListCount() {
    SubscriptionInfo subscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowOf(subscriptionManager).setActiveSubscriptionInfos(subscriptionInfo);

    assertThat(subscriptionManager.getActiveSubscriptionInfoCountMax()).isEqualTo(1);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_nullInfoListIsZero() {
    shadowOf(subscriptionManager).setActiveSubscriptionInfoList(null);

    assertThat(subscriptionManager.getActiveSubscriptionInfoCountMax()).isEqualTo(0);
  }

  @Test
  public void getActiveSubscriptionInfoCountMax_shouldThrowExceptionWhenNoPermissions() {
    shadowOf(subscriptionManager).setReadPhoneStatePermission(false);
    assertThrows(
        SecurityException.class, () -> subscriptionManager.getActiveSubscriptionInfoCountMax());
  }

  @Test
  public void getAvailableSubscriptionInfoList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();

    // default
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).isEmpty();

    // null condition
    shadowOf(subscriptionManager).setAvailableSubscriptionInfos();
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).isEmpty();

    // set a specific subscription
    shadowOf(subscriptionManager).setAvailableSubscriptionInfos(expectedSubscriptionInfo);
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList()).hasSize(1);
    assertThat(shadowOf(subscriptionManager).getAvailableSubscriptionInfoList().get(0))
        .isSameInstanceAs(expectedSubscriptionInfo);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnPhoneIdIfSet() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    assertThat(SubscriptionManager.getPhoneId(123)).isEqualTo(456);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfNotSet() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    assertThat(SubscriptionManager.getPhoneId(456))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfRemoved() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.removePhoneId(123);
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfCleared() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.clearPhoneIds();
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  @Config(maxSdk = P)
  public void getPhoneId_shouldReturnInvalidIfReset() {
    ShadowSubscriptionManager.putPhoneId(123, 456);
    ShadowSubscriptionManager.reset();
    assertThat(SubscriptionManager.getPhoneId(123))
        .isEqualTo(ShadowSubscriptionManager.INVALID_PHONE_INDEX);
  }

  @Test
  public void setMcc() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setMcc("123")
                .buildSubscriptionInfo()
                .getMcc())
        .isEqualTo(123);
  }

  @Test
  public void setMnc() {
    assertThat(
            ShadowSubscriptionManager.SubscriptionInfoBuilder.newBuilder()
                .setMnc("123")
                .buildSubscriptionInfo()
                .getMnc())
        .isEqualTo(123);
  }

  private static class DummySubscriptionsChangedListener
      extends SubscriptionManager.OnSubscriptionsChangedListener {
    private int subscriptionChangedCount;

    @Override
    public void onSubscriptionsChanged() {
      subscriptionChangedCount++;
    }
  }
}
