package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for {@link SubscriptionManager}.
 *
 * <p>Although {@link SubscriptionManager} itself has been added in LMR1, this shadow is only
 * available since N because all shadowed methods have been added in N.
 */
@Implements(value = SubscriptionManager.class, minSdk = N)
public class ShadowSubscriptionManager {

  private static int defaultDataSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultSmsSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultVoiceSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

  private List<SubscriptionInfo> mSubscriptionInfoList;

  @Implementation
  public List<SubscriptionInfo> getActiveSubscriptionInfoList() {
    return mSubscriptionInfoList;
  }

  public void setActiveSubscriptionInfoList(List<SubscriptionInfo> subscriptionInfoList) {
    mSubscriptionInfoList = subscriptionInfoList;
  }

  /** Returns value set with {@link #setDefaultDataSubscriptionId(int)}. */
  @Implementation
  protected static int getDefaultDataSubscriptionId() {
    return defaultDataSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultSmsSubscriptionId(int)}. */
  @Implementation
  protected static int getDefaultSmsSubscriptionId() {
    return defaultSmsSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultVoiceSubscriptionId(int)}. */
  @Implementation
  protected static int getDefaultVoiceSubscriptionId() {
    return defaultVoiceSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultSubscriptionId(int)}. */
  @Implementation
  public static int getDefaultSubscriptionId() {
    return defaultSubscriptionId;
  }

  public static void setDefaultDataSubscriptionId(int defaultDataSubscriptionId) {
    ShadowSubscriptionManager.defaultDataSubscriptionId = defaultDataSubscriptionId;
  }

  public static void setDefaultSmsSubscriptionId(int defaultSmsSubscriptionId) {
    ShadowSubscriptionManager.defaultSmsSubscriptionId = defaultSmsSubscriptionId;
  }

  public static void setDefaultVoiceSubscriptionId(int defaultVoiceSubscriptionId) {
    ShadowSubscriptionManager.defaultVoiceSubscriptionId = defaultVoiceSubscriptionId;
  }

  public static void setDefaultSubscriptionId(int defaultSubscriptionId) {
    ShadowSubscriptionManager.defaultSubscriptionId = defaultSubscriptionId;
  }

  @Resetter
  public static void reset() {
    defaultDataSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultSmsSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultVoiceSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  }
}
