package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
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
  private ShadowSubscriptionManager shadowSubscriptionManager;

  @Before
  public void setUp() throws Exception {
    subscriptionManager =
        (SubscriptionManager) application.getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
    shadowSubscriptionManager = shadowOf(subscriptionManager);
  }

  @Test
  public void shouldGiveDefaultSubscriptionId() {
    int testId = 42;
    ShadowSubscriptionManager.setDefaultSubscriptionId(testId);
    assertThat(subscriptionManager.getDefaultSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultDataSubscriptionId() {
    int testId = 42;
    shadowSubscriptionManager.setDefaultDataSubscriptionId(testId);
    assertThat(subscriptionManager.getDefaultDataSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultSmsSubscriptionId() {
    int testId = 42;
    shadowSubscriptionManager.setDefaultSmsSubscriptionId(testId);
    assertThat(subscriptionManager.getDefaultSmsSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void shouldGiveDefaultVoiceSubscriptionId() {
    int testId = 42;
    shadowSubscriptionManager.setDefaultVoiceSubscriptionId(testId);
    assertThat(subscriptionManager.getDefaultVoiceSubscriptionId()).isEqualTo(testId);
  }

  @Test
  public void addOnSubscriptionsChangedListener_shouldAddListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    shadowSubscriptionManager.addOnSubscriptionsChangedListener(listener);

    shadowSubscriptionManager.setActiveSubscriptionInfos(
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChanged).isTrue();
  }

  @Test
  public void removeOnSubscriptionsChangedListener_shouldRemoveListener() {
    DummySubscriptionsChangedListener listener = new DummySubscriptionsChangedListener();
    DummySubscriptionsChangedListener listener2 = new DummySubscriptionsChangedListener();
    shadowSubscriptionManager.addOnSubscriptionsChangedListener(listener);
    shadowSubscriptionManager.addOnSubscriptionsChangedListener(listener2);

    shadowSubscriptionManager.removeOnSubscriptionsChangedListener(listener);
    shadowSubscriptionManager.setActiveSubscriptionInfos(
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo());

    assertThat(listener.subscriptionChanged).isFalse();
    assertThat(listener2.subscriptionChanged).isTrue();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnInfoWithSubId() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowSubscriptionManager.setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfo(123))
        .isSameAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfoForSimSlotIndex_shouldReturnInfoWithSlotIndex() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setSimSlotIndex(123).buildSubscriptionInfo();
    shadowSubscriptionManager.setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(123))
        .isSameAs(expectedSubscriptionInfo);
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullList() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(null);
    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForNullVarargsList() {
    shadowSubscriptionManager.setActiveSubscriptionInfos((SubscriptionInfo[]) null);
    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void getActiveSubscriptionInfo_shouldReturnNullForEmptyList() {
    shadowSubscriptionManager.setActiveSubscriptionInfos();
    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfo(123)).isNull();
  }

  @Test
  public void isNetworkRoaming_shouldReturnTrueIfSet() {
    shadowSubscriptionManager.setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowSubscriptionManager.isNetworkRoaming(123)).isTrue();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+unset. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseIfUnset() {
    shadowSubscriptionManager.setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowSubscriptionManager.isNetworkRoaming(123)).isTrue();

    shadowSubscriptionManager.setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ false);
    assertThat(shadowSubscriptionManager.isNetworkRoaming(123)).isFalse();
  }

  /** Multi act-asserts are discouraged but here we are testing the set+clear. */
  @Test
  public void isNetworkRoaming_shouldReturnFalseOnClear() {
    shadowSubscriptionManager.setNetworkRoamingStatus(123, /*isNetworkRoaming=*/ true);
    assertThat(shadowSubscriptionManager.isNetworkRoaming(123)).isTrue();

    shadowSubscriptionManager.clearNetworkRoamingStatus();
    assertThat(shadowSubscriptionManager.isNetworkRoaming(123)).isFalse();
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnZeroIfActiveSubscriptionInfoListNotSet() {
    shadowSubscriptionManager.setActiveSubscriptionInfoList(null);

    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfoCount()).isEqualTo(0);
  }

  @Test
  public void getActiveSubscriptionInfoCount_shouldReturnSizeOfActiveSubscriotionInfosList() {
    SubscriptionInfo expectedSubscriptionInfo =
        SubscriptionInfoBuilder.newBuilder().setId(123).buildSubscriptionInfo();
    shadowSubscriptionManager.setActiveSubscriptionInfos(expectedSubscriptionInfo);

    assertThat(shadowSubscriptionManager.getActiveSubscriptionInfoCount()).isEqualTo(1);
  }

  private static class DummySubscriptionsChangedListener
      extends SubscriptionManager.OnSubscriptionsChangedListener {
    private boolean subscriptionChanged = false;

    @Override
    public void onSubscriptionsChanged() {
      subscriptionChanged = true;
    }
  }
}
