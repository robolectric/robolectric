package org.robolectric.shadows;

import static android.content.Context.TELEPHONY_SUBSCRIPTION_SERVICE;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

import android.telephony.SubscriptionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
}
