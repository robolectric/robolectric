package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.N;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.util.SparseIntArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link SubscriptionManager}. */
@Implements(value = SubscriptionManager.class, minSdk = LOLLIPOP_MR1)
public class ShadowSubscriptionManager {

  private static int defaultDataSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultSmsSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultVoiceSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultSubscriptionId = SubscriptionManager.DEFAULT_SUBSCRIPTION_ID;
  private static SparseIntArray phoneIds = new SparseIntArray();

  /** Returns value set with {@link #setDefaultDataSubscriptionId(int)}. */
  @Implementation(minSdk = N)
  protected static int getDefaultDataSubscriptionId() {
    return defaultDataSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultSmsSubscriptionId(int)}. */
  @Implementation(minSdk = N)
  protected static int getDefaultSmsSubscriptionId() {
    return defaultSmsSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultVoiceSubscriptionId(int)}. */
  @Implementation(minSdk = N)
  protected static int getDefaultVoiceSubscriptionId() {
    return defaultVoiceSubscriptionId;
  }

  /** Returns value set with {@link #setDefaultSubscriptionId(int)}. */
  @Implementation(minSdk = N)
  protected static int getDefaultSubscriptionId() {
    return defaultSubscriptionId;
  }

  /** Returns value set with {@link #setPhoneId(int, int)}. */
  @Implementation
  @HiddenApi
  protected static int getPhoneId(int subId) {
    return phoneIds.get(subId);
  }

  /** Set the value to be returned by {@link #getDefaultDataSubscriptionId()} */
  public static void setDefaultDataSubscriptionId(int defaultDataSubscriptionId) {
    ShadowSubscriptionManager.defaultDataSubscriptionId = defaultDataSubscriptionId;
  }

  /** Set the value to be returned by {@link #getDefaultSmsSubscriptionId()} */
  public static void setDefaultSmsSubscriptionId(int defaultSmsSubscriptionId) {
    ShadowSubscriptionManager.defaultSmsSubscriptionId = defaultSmsSubscriptionId;
  }

  /** Set the value to be returned by {@link #getDefaultVoiceSubscriptionId()} */
  public static void setDefaultVoiceSubscriptionId(int defaultVoiceSubscriptionId) {
    ShadowSubscriptionManager.defaultVoiceSubscriptionId = defaultVoiceSubscriptionId;
  }

  /** Set the value to be returned by {@link #getDefaultSubscriptionId()} */
  public static void setDefaultSubscriptionId(int defaultSubscriptionId) {
    ShadowSubscriptionManager.defaultSubscriptionId = defaultSubscriptionId;
  }

  /** Set the phone id to be returned for the given {@code subId}. */
  public static void setPhoneId(int subId, int phoneId) {
    phoneIds.put(subId, phoneId);
  }

  /** Clears the subId to phoneId mapping. */
  public static void clearPhoneIds() {
    phoneIds.clear();
  }

  private List<SubscriptionInfo> subscriptionList = new ArrayList<>();
  private List<OnSubscriptionsChangedListener> listeners = new ArrayList<>();

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected List<SubscriptionInfo> getActiveSubscriptionInfoList() {
    return subscriptionList;
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected SubscriptionInfo getActiveSubscriptionInfo(int subId) {
    for (SubscriptionInfo info : subscriptionList) {
      if (info.getSubscriptionId() == subId) {
        return info;
      }
    }
    return null;
  }

  /**
   * Sets the active list of {@link SubscriptionInfo}. This call internally triggers {@link
   * OnSubscriptionsChangedListener#onSubscriptionsChanged()} to all the listeners.
   */
  public void setActiveSubscriptionInfoList(List<SubscriptionInfo> list) {
    subscriptionList = list;
    dispatchOnSubscriptionsChanged();
  }

  /**
   * Sets the active list of {@link SubscriptionInfo}. This call internally triggers {@link
   * OnSubscriptionsChangedListener#onSubscriptionsChanged()} to all the listeners.
   */
  public void setActiveSubscriptionInfos(SubscriptionInfo... infos) {
    setActiveSubscriptionInfoList(Arrays.asList(infos));
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
    listeners.add(listener);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
    listeners.remove(listener);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  @HiddenApi
  protected int[] getActiveSubscriptionIdList() {
    final List<SubscriptionInfo> infos = getActiveSubscriptionInfoList();
    if (infos == null) {
      return new int[0];
    }
    int[] ids = new int[infos.size()];
    for (int i = 0; i < infos.size(); i++) {
      ids[i] = infos.get(i).getSubscriptionId();
    }
    return ids;
  }

  private void dispatchOnSubscriptionsChanged() {
    for (OnSubscriptionsChangedListener listener : listeners) {
      listener.onSubscriptionsChanged();
    }
  }

  /** Builder class to create instance of {@link SubscriptionInfo}. */
  public static class SubscriptionInfoBuilder {
    private final SubscriptionInfo subscriptionInfo =
        ReflectionHelpers.callConstructor(SubscriptionInfo.class);

    public static SubscriptionInfoBuilder newBuilder() {
      return new SubscriptionInfoBuilder();
    }

    public SubscriptionInfo buildSubscriptionInfo() {
      return subscriptionInfo;
    }

    public SubscriptionInfoBuilder setId(int id) {
      ReflectionHelpers.setField(subscriptionInfo, "mId", id);
      return this;
    }

    public SubscriptionInfoBuilder setIccId(String iccId) {
      ReflectionHelpers.setField(subscriptionInfo, "mIccId", iccId);
      return this;
    }

    public SubscriptionInfoBuilder setSimSlotIndex(int index) {
      ReflectionHelpers.setField(subscriptionInfo, "mSimSlotIndex", index);
      return this;
    }

    public SubscriptionInfoBuilder setDisplayName(String name) {
      ReflectionHelpers.setField(subscriptionInfo, "mDisplayName", name);
      return this;
    }

    public SubscriptionInfoBuilder setCarrierName(String carrierName) {
      ReflectionHelpers.setField(subscriptionInfo, "mCarrierName", carrierName);
      return this;
    }

    public SubscriptionInfoBuilder setIconTint(int iconTint) {
      ReflectionHelpers.setField(subscriptionInfo, "mIconTint", iconTint);
      return this;
    }

    public SubscriptionInfoBuilder setNumber(String number) {
      ReflectionHelpers.setField(subscriptionInfo, "mNumber", number);
      return this;
    }

    public SubscriptionInfoBuilder setDataRoaming(int dataRoaming) {
      ReflectionHelpers.setField(subscriptionInfo, "mDataRoaming", dataRoaming);
      return this;
    }

    public SubscriptionInfoBuilder setCountryIso(String countryIso) {
      ReflectionHelpers.setField(subscriptionInfo, "mCountryIso", countryIso);
      return this;
    }

    // Use {@link #newBuilder} to construct builders.
    private SubscriptionInfoBuilder() {}
  }
}
