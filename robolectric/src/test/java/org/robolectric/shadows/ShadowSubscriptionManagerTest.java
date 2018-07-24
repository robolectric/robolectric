package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSubscriptionManager.SubscriptionInfoBuilder;

/** Test for {@link ShadowSubscriptionManager}. */
@RunWith(RobolectricTestRunner.class)
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
  public void shouldGiveDefaultSubscriptionId() {
    int expectedDefaultSubscriptionId = 1234;
    ShadowSubscriptionManager.setDefaultSubscriptionId(expectedDefaultSubscriptionId);

    assertThat(SubscriptionManager.getDefaultSubscriptionId())
        .isEqualTo(expectedDefaultSubscriptionId);
  }

  @Test
  public void shouldGivePhoneId() {
    int subId = 1;
    int expectedPhoneId = 111;
    ShadowSubscriptionManager.setPhoneId(subId, expectedPhoneId);

    assertThat(SubscriptionManager.getPhoneId(subId)).isEqualTo(expectedPhoneId);
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

  private static class DummySubscriptionsChangedListener
      extends SubscriptionManager.OnSubscriptionsChangedListener {
    private boolean subscriptionChanged = false;

    @Override
    public void onSubscriptionsChanged() {
      subscriptionChanged = true;
    }
  }
}
