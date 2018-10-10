package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.PersistableBundle;
import android.telecom.PhoneAccountHandle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {

  private final Map<PhoneStateListener, Integer> phoneStateRegistrations = new HashMap<>();
  private final Map<Integer, String> slotIndexToDeviceId = new HashMap<>();
  private final Map<PhoneAccountHandle, Boolean> voicemailVibrationEnabledMap = new HashMap<>();
  private final Map<PhoneAccountHandle, Uri> voicemailRingtoneUriMap = new HashMap<>();
  private final Map<PhoneAccountHandle, TelephonyManager> phoneAccountToTelephonyManagers =
      new HashMap<>();

  private PhoneStateListener lastListener;
  private int lastEventFlags;

  private String deviceId;
  private String imei;
  private String meid;
  private String groupIdLevel1;
  private String networkOperatorName = "";
  private String networkCountryIso;
  private String networkOperator = "";
  private String simOperator;
  private String simOperatorName;
  private String simSerialNumber;
  private boolean readPhoneStatePermission = true;
  private int phoneType = TelephonyManager.PHONE_TYPE_GSM;
  private String line1Number;
  private int networkType;
  private int voiceNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
  private List<CellInfo> allCellInfo = Collections.emptyList();
  private CellLocation cellLocation = null;
  private int callState = CALL_STATE_IDLE;
  private String incomingPhoneNumber = null;
  private boolean isAnasEnabled;
  private boolean isSmsCapable = true;
  private String voiceMailNumber;
  private String voiceMailAlphaTag;
  private int phoneCount = 1;
  private Map<Integer, TelephonyManager> subscriptionIdsToTelephonyManagers = new HashMap<>();
  private PersistableBundle carrierConfig;
  private ServiceState serviceState;
  private boolean isNetworkRoaming;
  private final SparseIntArray simStates = new SparseIntArray();
  private final SparseIntArray currentPhoneTypes = new SparseIntArray();
  private final SparseArray<List<String>> carrierPackageNames = new SparseArray<>();
  private final Map<Integer, String> simCountryIsoMap = new HashMap<>();
  private int simCarrierId;
  private String subscriberId;

  {
    resetSimStates();
    resetSimCountryIsos();
  }

  @Implementation
  protected void listen(PhoneStateListener listener, int flags) {
    lastListener = listener;
    lastEventFlags = flags;

    if (flags == LISTEN_NONE) {
      phoneStateRegistrations.remove(listener);
    } else {
      initListener(listener, flags);
      phoneStateRegistrations.put(listener, flags);
    }
  }

  /**
   * Returns the most recent listener passed to #listen().
   *
   * @return Phone state listener.
   * @deprecated Avoid using.
   */
  @Deprecated
  public PhoneStateListener getListener() {
    return lastListener;
  }

  /**
   * Returns the most recent flags passed to #listen().
   *
   * @return Event flags.
   * @deprecated Avoid using.
   */
  @Deprecated
  public int getEventFlags() {
    return lastEventFlags;
  }

  /** Call state may be specified via {@link #setCallState(int)}. */
  @Implementation
  protected int getCallState() {
    return callState;
  }

  /** Sets the current call state to the desired state and updates any listeners. */
  public void setCallState(int callState) {
    setCallState(callState, null);
  }

  /**
   * Sets the current call state with the option to specify an incoming phone number for the
   * CALL_STATE_RINGING state. The incoming phone number will be ignored for all other cases.
   */
  public void setCallState(int callState, String incomingPhoneNumber) {
    if (callState != CALL_STATE_RINGING) {
      incomingPhoneNumber = null;
    }

    this.callState = callState;
    this.incomingPhoneNumber = incomingPhoneNumber;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CALL_STATE)) {
      listener.onCallStateChanged(callState, incomingPhoneNumber);
    }
  }

  @Implementation
  protected String getDeviceId() {
    checkReadPhoneStatePermission();
    return deviceId;
  }

  public void setDeviceId(String newDeviceId) {
    deviceId = newDeviceId;
  }

  public void setNetworkOperatorName(String networkOperatorName) {
    this.networkOperatorName = networkOperatorName;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected String getImei() {
    checkReadPhoneStatePermission();
    return imei;
  }

  /** Set the IMEI returned by getImei(). */
  public void setImei(String imei) {
    this.imei = imei;
  }

  @Implementation(minSdk = O)
  protected String getMeid() {
    checkReadPhoneStatePermission();
    return meid;
  }

  /** Set the MEID returned by getMeid(). */
  public void setMeid(String meid) {
    this.meid = meid;
  }

  @Implementation
  protected String getNetworkOperatorName() {
    return networkOperatorName;
  }

  public void setNetworkCountryIso(String networkCountryIso) {
    this.networkCountryIso = networkCountryIso;
  }

  @Implementation
  protected String getNetworkCountryIso() {
    return networkCountryIso;
  }

  public void setNetworkOperator(String networkOperator) {
    this.networkOperator = networkOperator;
  }

  @Implementation
  protected String getNetworkOperator() {
    return networkOperator;
  }

  @Implementation
  protected String getSimOperator() {
    return simOperator;
  }

  public void setSimOperator(String simOperator) {
    this.simOperator = simOperator;
  }

  @Implementation
  protected String getSimOperatorName() {
    return simOperatorName;
  }

  public void setSimOperatorName(String simOperatorName) {
    this.simOperatorName = simOperatorName;
  }

  @Implementation
  protected String getSimSerialNumber() {
    checkReadPhoneStatePermission();
    return this.simSerialNumber;
  }

  /** sets the serial number that will be returned by {@link #getSimSerialNumber}. */
  public void setSimSerialNumber(String simSerialNumber) {
    this.simSerialNumber = simSerialNumber;
  }

  @Implementation
  protected String getSimCountryIso() {
    return simCountryIsoMap.get(/* subId= */ 0);
  }

  @Implementation(minSdk = N)
  @HiddenApi
  protected String getSimCountryIso(int subId) {
    return simCountryIsoMap.get(subId);
  }

  public void setSimCountryIso(String simCountryIso) {
    setSimCountryIso(/* subId= */ 0, simCountryIso);
  }

  /** Sets the {@code simCountryIso} for the given {@code subId}. */
  public void setSimCountryIso(int subId, String simCountryIso) {
    simCountryIsoMap.put(subId, simCountryIso);
  }

  /** Clears {@code subId} to simCountryIso mapping and resets to default state. */
  public void resetSimCountryIsos() {
    simCountryIsoMap.clear();
    simCountryIsoMap.put(0, "");
  }

  @Implementation
  protected int getSimState() {
    return getSimState(/* slotIndex= */ 0);
  }

  /** Sets the sim state of slot 0. */
  public void setSimState(int simState) {
    setSimState(/* slotIndex= */ 0, simState);
  }

  /** Set the sim state for the given {@code slotIndex}. */
  public void setSimState(int slotIndex, int state) {
    simStates.put(slotIndex, state);
  }

  @Implementation(minSdk = O)
  protected int getSimState(int slotIndex) {
    return simStates.get(slotIndex, TelephonyManager.SIM_STATE_UNKNOWN);
  }

  /** Clears {@code slotIndex} to state mapping and resets to default state. */
  public void resetSimStates() {
    simStates.clear();
    simStates.put(0, TelephonyManager.SIM_STATE_READY);
  }

  public void setReadPhoneStatePermission(boolean readPhoneStatePermission) {
    this.readPhoneStatePermission = readPhoneStatePermission;
  }

  private void checkReadPhoneStatePermission() {
    if (!readPhoneStatePermission) {
      throw new SecurityException();
    }
  }

  @Implementation
  protected int getPhoneType() {
    return phoneType;
  }

  public void setPhoneType(int phoneType) {
    this.phoneType = phoneType;
  }

  @Implementation
  protected String getLine1Number() {
    return line1Number;
  }

  public void setLine1Number(String line1Number) {
    this.line1Number = line1Number;
  }

  @Implementation
  protected int getNetworkType() {
    return networkType;
  }

  public void setNetworkType(int networkType) {
    this.networkType = networkType;
  }

  /**
   * Returns whatever value was set by the last call to {@link #setVoiceNetworkType}, defaulting to
   * {@link TelephonyManager#NETWORK_TYPE_UNKNOWN} if it was never called.
   */
  @Implementation(minSdk = N)
  protected int getVoiceNetworkType() {
    return voiceNetworkType;
  }

  /**
   * Sets the value to be returned by calls to {@link getVoiceNetworkType}. This <b>should</b>
   * correspond to one of the {@code NETWORK_TYPE_*} constants defined on {@link TelephonyManager},
   * but this is not enforced.
   */
  public void setVoiceNetworkType(int voiceNetworkType) {
    this.voiceNetworkType = voiceNetworkType;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<CellInfo> getAllCellInfo() {
    return allCellInfo;
  }

  public void setAllCellInfo(List<CellInfo> allCellInfo) {
    this.allCellInfo = allCellInfo;

    if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_INFO)) {
        listener.onCellInfoChanged(allCellInfo);
      }
    }
  }

  @Implementation
  protected CellLocation getCellLocation() {
    return this.cellLocation;
  }

  public void setCellLocation(CellLocation cellLocation) {
    this.cellLocation = cellLocation;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_LOCATION)) {
      listener.onCellLocationChanged(cellLocation);
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected String getGroupIdLevel1() {
    return this.groupIdLevel1;
  }

  public void setGroupIdLevel1(String groupIdLevel1) {
    this.groupIdLevel1 = groupIdLevel1;
  }

  private void initListener(PhoneStateListener listener, int flags) {
    if ((flags & LISTEN_CALL_STATE) != 0) {
      listener.onCallStateChanged(callState, incomingPhoneNumber);
    }
    if ((flags & LISTEN_CELL_INFO) != 0) {
      if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
        listener.onCellInfoChanged(allCellInfo);
      }
    }
    if ((flags & LISTEN_CELL_LOCATION) != 0) {
      listener.onCellLocationChanged(cellLocation);
    }
  }

  private Iterable<PhoneStateListener> getListenersForFlags(int flags) {
    return Iterables.filter(
        phoneStateRegistrations.keySet(),
        new Predicate<PhoneStateListener>() {
          @Override
          public boolean apply(PhoneStateListener input) {
            // only select PhoneStateListeners with matching flags
            return (phoneStateRegistrations.get(input) & flags) != 0;
          }
        });
  }

  // BEGIN-INTERNAL
  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean setAlternativeNetworkState(boolean enable) {
    isAnasEnabled = enable;
    return true;
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isAlternativeNetworkEnabled() {
    return isAnasEnabled;
  }
  // END-INTERNAL

  /** @return `true` by default, or the value specified via {@link #setIsSmsCapable(boolean)} */
  @Implementation
  protected boolean isSmsCapable() {
    return isSmsCapable;
  }

  /** Sets the value returned by {@link TelephonyManager#isSmsCapable()}. */
  public void setIsSmsCapable(boolean isSmsCapable) {
    this.isSmsCapable = isSmsCapable;
  }

  /**
   * Returns a new empty {@link PersistableBundle} by default, or the value specified via {@link
   * #setCarrierConfig(PersistableBundle)}.
   */
  @Implementation(minSdk = O)
  protected PersistableBundle getCarrierConfig() {
    return carrierConfig != null ? carrierConfig : new PersistableBundle();
  }

  /**
   * Sets the value returned by {@link TelephonyManager#getCarrierConfig()}.
   *
   * @param carrierConfig
   */
  public void setCarrierConfig(PersistableBundle carrierConfig) {
    this.carrierConfig = carrierConfig;
  }

  /**
   * Returns {@code null} by default, or the value specified via {@link
   * #setVoiceMailNumber(String)}.
   */
  @Implementation
  protected String getVoiceMailNumber() {
    return voiceMailNumber;
  }

  /** Sets the value returned by {@link TelephonyManager#getVoiceMailNumber()}. */
  public void setVoiceMailNumber(String voiceMailNumber) {
    this.voiceMailNumber = voiceMailNumber;
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setVoiceMailAlphaTag(String)}.
   */
  @Implementation
  protected String getVoiceMailAlphaTag() {
    return voiceMailAlphaTag;
  }

  /** Sets the value returned by {@link TelephonyManager#getVoiceMailAlphaTag()}. */
  public void setVoiceMailAlphaTag(String voiceMailAlphaTag) {
    this.voiceMailAlphaTag = voiceMailAlphaTag;
  }

  /** Returns 1 by default or the value specified via {@link #setPhoneCount(int)}. */
  @Implementation(minSdk = M)
  protected int getPhoneCount() {
    return phoneCount;
  }

  /** Sets the value returned by {@link TelephonyManager#getPhoneCount()}. */
  public void setPhoneCount(int phoneCount) {
    this.phoneCount = phoneCount;
  }

  /**
   * Returns {@code null} by default or the value specified via {@link #setDeviceId(int, String)}.
   */
  @Implementation(minSdk = M)
  protected String getDeviceId(int slot) {
    return slotIndexToDeviceId.get(slot);
  }

  /** Sets the value returned by {@link TelephonyManager#getDeviceId(int)}. */
  public void setDeviceId(int slot, String deviceId) {
    slotIndexToDeviceId.put(slot, deviceId);
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setVoicemailVibrationEnabled(PhoneAccountHandle, boolean)}.
   */
  @Implementation(minSdk = N)
  protected boolean isVoicemailVibrationEnabled(PhoneAccountHandle handle) {
    Boolean result = voicemailVibrationEnabledMap.get(handle);
    return result != null ? result : false;
  }

  /**
   * Sets the value returned by {@link
   * TelephonyManager#isVoicemailVibrationEnabled(PhoneAccountHandle)}.
   */
  @Implementation(minSdk = O)
  protected void setVoicemailVibrationEnabled(PhoneAccountHandle handle, boolean isEnabled) {
    voicemailVibrationEnabledMap.put(handle, isEnabled);
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setVoicemailRingtoneUri(PhoneAccountHandle, Uri)}.
   */
  @Implementation(minSdk = N)
  protected Uri getVoicemailRingtoneUri(PhoneAccountHandle handle) {
    return voicemailRingtoneUriMap.get(handle);
  }

  /**
   * Sets the value returned by {@link
   * TelephonyManager#getVoicemailRingtoneUri(PhoneAccountHandle)}.
   */
  @Implementation(minSdk = O)
  protected void setVoicemailRingtoneUri(PhoneAccountHandle handle, Uri uri) {
    voicemailRingtoneUriMap.put(handle, uri);
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setTelephonyManagerForHandle(PhoneAccountHandle, TelephonyManager)}.
   */
  @Implementation(minSdk = O)
  protected TelephonyManager createForPhoneAccountHandle(PhoneAccountHandle handle) {
    return phoneAccountToTelephonyManagers.get(handle);
  }

  /**
   * Sets the value returned by {@link
   * TelephonyManager#createForPhoneAccountHandle(PhoneAccountHandle)}.
   */
  public void setTelephonyManagerForHandle(
      PhoneAccountHandle handle, TelephonyManager telephonyManager) {
    phoneAccountToTelephonyManagers.put(handle, telephonyManager);
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setTelephonyManagerForSubscriptionId(int, TelephonyManager)}
   */
  @Implementation(minSdk = N)
  protected TelephonyManager createForSubscriptionId(int subId) {
    return subscriptionIdsToTelephonyManagers.get(subId);
  }

  /** Sets the value returned by {@link TelephonyManager#createForSubscriptionId(int)}. */
  public void setTelephonyManagerForSubscriptionId(
      int subscriptionId, TelephonyManager telephonyManager) {
    subscriptionIdsToTelephonyManagers.put(subscriptionId, telephonyManager);
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setServiceState(ServiceState)}
   */
  @Implementation(minSdk = O)
  protected ServiceState getServiceState() {
    return serviceState;
  }

  /** Sets the value returned by {@link TelephonyManager#getServiceState()}. */
  public void setServiceState(ServiceState serviceState) {
    this.serviceState = serviceState;
  }

  /**
   * Returns {@code false} by default or the value specified via {@link
   * #setIsNetworkRoaming(boolean)}
   */
  @Implementation
  protected boolean isNetworkRoaming() {
    return isNetworkRoaming;
  }

  /** Sets the value returned by {@link TelephonyManager#isNetworkRoaming()}. */
  public void setIsNetworkRoaming(boolean isNetworkRoaming) {
    this.isNetworkRoaming = isNetworkRoaming;
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected int getCurrentPhoneType(int subId) {
    return currentPhoneTypes.get(subId, TelephonyManager.PHONE_TYPE_NONE);
  }

  /** Sets the phone type for the given {@code subId}. */
  public void setCurrentPhoneType(int subId, int phoneType) {
    currentPhoneTypes.put(subId, phoneType);
  }

  /** Removes all {@code subId} to {@code phoneType} mappings. */
  public void clearPhoneTypes() {
    currentPhoneTypes.clear();
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) {
    return carrierPackageNames.get(phoneId);
  }

  @Implementation(minSdk = LOLLIPOP)
  @HiddenApi
  protected List<String> getCarrierPackageNamesForIntent(Intent intent) {
    return carrierPackageNames.get(SubscriptionManager.DEFAULT_SUBSCRIPTION_ID);
  }

  /** Sets the {@code packages} for the given {@code phoneId}. */
  public void setCarrierPackageNamesForPhone(int phoneId, List<String> packages) {
    carrierPackageNames.put(phoneId, packages);
  }

  @Implementation(minSdk = P)
  protected int getSimCarrierId() {
    return simCarrierId;
  }

  /** Sets the value to be returned by {@link #getSimCarrierId()}. */
  public void setSimCarrierId(int simCarrierId) {
    this.simCarrierId = simCarrierId;
  }

  @Implementation
  protected String getSubscriberId() {
    return subscriberId;
  }

  /** Sets the value to be returned by {@link #getSubscriberId()}. */
  public void setSubscriberId(String subscriberId) {
    this.subscriberId = subscriberId;
  }
}
