package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.N;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = SubscriptionManager.class, minSdk = LOLLIPOP_MR1)
public class ShadowSubscriptionManager {

  private static int defaultSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultDataSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultSmsSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
  private static int defaultVoiceSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;

  /** Returns value set with {@link #setDefaultSubscriptionId(int)}. */
  @Implementation(minSdk = N)
  protected static int getDefaultSubscriptionId() {
    return defaultSubscriptionId;
  }

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

  /** Sets the value that will be returned by {@link #getDefaultSubscriptionId()}. */
  public static void setDefaultSubscriptionId(int defaultSubscriptionId) {
    ShadowSubscriptionManager.defaultSubscriptionId = defaultSubscriptionId;
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

  /**
   * Cache of {@link SubscriptionInfo} used by {@link #getActiveSubscriptionInfoList}.
   * Managed by {@link #setActiveSubscriptionInfoList}.
   */
  private List<SubscriptionInfo> subscriptionList = new ArrayList<>();
  /**
   * List of listeners to be notified if the list of {@link SubscriptionInfo} changes. Managed by
   * {@link #addOnSubscriptionsChangedListener} and {@link removeOnSubscriptionsChangedListener}.
   */
  private List<OnSubscriptionsChangedListener> listeners = new ArrayList<>();
  /**
   * Cache of subscription ids used by {@link #isNetworkRoaming}. Managed by {@link
   * #setNetworkRoamingStatus} and {@link #clearNetworkRoamingStatus}.
   */
  private Set<Integer> roamingSimSubscriptionIds = new HashSet<>();

  /**
   * Returns the active list of {@link SubscriptionInfo} that were set via {@link
   * #setActiveSubscriptionInfoList}.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected List<SubscriptionInfo> getActiveSubscriptionInfoList() {
    return subscriptionList;
  }

  /**
   * Returns the size of the list of {@link SubscriptionInfo} that were set via {@link
   * #setActiveSubscriptionInfoList}. If no list was set, returns 0.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected int getActiveSubscriptionInfoCount() {
    return subscriptionList == null ? 0 : subscriptionList.size();
  }

  /**
   * Returns subscription that were set via {@link #setActiveSubscriptionInfoList} if it can find
   * one with the specified id or null if none found.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected SubscriptionInfo getActiveSubscriptionInfo(int subId) {
    if (subscriptionList == null) {
      return null;
    }
    for (SubscriptionInfo info : subscriptionList) {
      if (info.getSubscriptionId() == subId) {
        return info;
      }
    }
    return null;
  }

  /**
   * Returns subscription that were set via {@link #setActiveSubscriptionInfoList} if it can find
   * one with the specified slot index or null if none found.
   */
  @Implementation(minSdk = N)
  protected SubscriptionInfo getActiveSubscriptionInfoForSimSlotIndex(int slotIndex) {
    if (subscriptionList == null) {
      return null;
    }
    for (SubscriptionInfo info : subscriptionList) {
      if (info.getSimSlotIndex() == slotIndex) {
        return info;
      }
    }
    return null;
  }

  /**
   * Sets the active list of {@link SubscriptionInfo}. This call internally triggers {@link
   * OnSubscriptionsChangedListener#onSubscriptionsChanged()} to all the listeners.
   * @param list - The subscription info list, can be null.
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
    if (infos == null) {
      setActiveSubscriptionInfoList(Collections.emptyList());
    } else {
      setActiveSubscriptionInfoList(Arrays.asList(infos));
    }
  }

  /**
   * Adds a listener to a local list of listeners. Will be triggered by {@link
   * #setActiveSubscriptionInfoList} when the local list of {@link SubscriptionInfo} is updated.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void addOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener from a local list of listeners. Will be triggered by {@link
   * #setActiveSubscriptionInfoList} when the local list of {@link SubscriptionInfo} is updated.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void removeOnSubscriptionsChangedListener(OnSubscriptionsChangedListener listener) {
    listeners.remove(listener);
  }

  /** Returns subscription Ids that were set via {@link #setActiveSubscriptionInfoList}. */
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

  /**
   * Notifies {@link OnSubscriptionsChangedListener} listeners that the list of {@link
   * SubscriptionInfo} has been updated.
   */
  private void dispatchOnSubscriptionsChanged() {
    for (OnSubscriptionsChangedListener listener : listeners) {
      listener.onSubscriptionsChanged();
    }
  }

  /** Clears the local cache of roaming subscription Ids used by {@link #isNetworkRoaming}. */
  public void clearNetworkRoamingStatus(){
    roamingSimSubscriptionIds.clear();
  }

  /**
   * If isNetworkRoaming is set, it will mark the provided sim subscriptionId as roaming in a local
   * cache. If isNetworkRoaming is unset it will remove the subscriptionId from the local cache. The
   * local cache is used to provide roaming status returned by {@link #isNetworkRoaming}.
   */
  public void setNetworkRoamingStatus(int simSubscriptionId, boolean isNetworkRoaming) {
    if (isNetworkRoaming) {
      roamingSimSubscriptionIds.add(simSubscriptionId);
    } else {
      roamingSimSubscriptionIds.remove(simSubscriptionId);
    }
  }

  /**
   * Uses the local cache of roaming sim subscription Ids managed by {@link
   * #setNetworkRoamingStatus} to return subscription Ids marked as roaming. Otherwise subscription
   * Ids will be considered as non-roaming if they are not in the cache.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isNetworkRoaming(int simSubscriptionId) {
    return roamingSimSubscriptionIds.contains(simSubscriptionId);
  }

  @Resetter
  public static void reset() {
    defaultDataSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultSmsSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultVoiceSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    defaultSubscriptionId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
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
