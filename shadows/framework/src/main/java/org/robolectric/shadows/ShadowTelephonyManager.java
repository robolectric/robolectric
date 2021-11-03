package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

import android.annotation.CallSuper;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.telecom.PhoneAccountHandle;
import android.telephony.Annotation.NetworkType;
import android.telephony.Annotation.OverrideNetworkType;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.CellInfoCallback;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = TelephonyManager.class, looseSignatures = true)
public class ShadowTelephonyManager {

  @RealObject protected TelephonyManager realTelephonyManager;

  private final Map<PhoneStateListener, Integer> phoneStateRegistrations = new HashMap<>();
  private final Map<Integer, String> slotIndexToDeviceId = new HashMap<>();
  private final Map<Integer, String> slotIndexToImei = new HashMap<>();
  private final Map<Integer, String> slotIndexToMeid = new HashMap<>();
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
  private Locale simLocale;
  private String simOperator;
  private String simOperatorName;
  private String simSerialNumber;
  private boolean readPhoneStatePermission = true;
  private int phoneType = TelephonyManager.PHONE_TYPE_GSM;
  private String line1Number;
  private int networkType;
  private int dataNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
  private int voiceNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
  private List<CellInfo> allCellInfo = Collections.emptyList();
  private List<CellInfo> callbackCellInfos = null;
  private CellLocation cellLocation = null;
  private int callState = CALL_STATE_IDLE;
  private int dataState = TelephonyManager.DATA_DISCONNECTED;
  private String incomingPhoneNumber = null;
  private boolean isSmsCapable = true;
  private boolean voiceCapable = true;
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
  private int carrierIdFromSimMccMnc;
  private String subscriberId;
  private /*UiccSlotInfo[]*/ Object uiccSlotInfos;
  private String visualVoicemailPackageName = null;
  private SignalStrength signalStrength;
  private boolean dataEnabled = false;
  private boolean isRttSupported;
  private final List<String> sentDialerSpecialCodes = new ArrayList<>();
  private boolean hearingAidCompatibilitySupported = false;
  private int requestCellInfoUpdateErrorCode = 0;
  private Throwable requestCellInfoUpdateDetail = null;
  private Object telephonyDisplayInfo;
  private boolean isDataConnectionAllowed;
  private static int callComposerStatus = 0;
  /**
   * Should be {@link TelephonyManager.BootstrapAuthenticationCallback} but this object was
   * introduced in Android S, so we are using Object to avoid breaking other SDKs
   *
   * <p>XXX Look into using the real types if we're now compiling against S
   */
  private Object callback;

  {
    resetSimStates();
    resetSimCountryIsos();
  }

  @Resetter
  public static void reset() {
    callComposerStatus = 0;
  }

  public static void setCallComposerStatus(int callComposerStatus) {
    ShadowTelephonyManager.callComposerStatus = callComposerStatus;
  }

  @Implementation(minSdk = S)
  @HiddenApi
  protected int getCallComposerStatus() {
    return callComposerStatus;
  }

  public Object getBootstrapAuthenticationCallback() {
    return callback;
  }

  @Implementation(minSdk = S)
  @HiddenApi
  public void bootstrapAuthenticationRequest(
      Object appType,
      Object nafId,
      Object securityProtocol,
      Object forceBootStrapping,
      Object e,
      Object callback) {
    this.callback = callback;
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

  /**
   * Data state may be specified via {@link #setDataState(int)}. If no override is set, this
   * defaults to {@link TelephonyManager#DATA_DISCONNECTED}.
   */
  @Implementation
  protected int getDataState() {
    return dataState;
  }

  /** Sets the data state returned by {@link #getDataState()}. */
  public void setDataState(int dataState) {
    this.dataState = dataState;
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

  @Implementation(minSdk = O)
  protected String getImei(int slotIndex) {
    checkReadPhoneStatePermission();
    return slotIndexToImei.get(slotIndex);
  }

  /** Set the IMEI returned by getImei(). */
  public void setImei(String imei) {
    this.imei = imei;
  }

  /** Set the IMEI returned by {@link #getImei(int)}. */
  public void setImei(int slotIndex, String imei) {
    slotIndexToImei.put(slotIndex, imei);
  }

  @Implementation(minSdk = O)
  protected String getMeid() {
    checkReadPhoneStatePermission();
    return meid;
  }

  @Implementation(minSdk = O)
  protected String getMeid(int slotIndex) {
    checkReadPhoneStatePermission();
    return slotIndexToMeid.get(slotIndex);
  }

  /** Set the MEID returned by getMeid(). */
  public void setMeid(String meid) {
    this.meid = meid;
  }

  /** Set the MEID returned by {@link #getMeid(int)}. */
  public void setMeid(int slotIndex, String meid) {
    slotIndexToMeid.put(slotIndex, meid);
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

  /** Sets the sim locale returned by {@link #getSimLocale()}. */
  public void setSimLocale(Locale simLocale) {
    this.simLocale = simLocale;
  }

  /** Returns sim locale set by {@link #setSimLocale}. */
  @Implementation(minSdk = Q)
  protected Locale getSimLocale() {
    return simLocale;
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

  @Implementation(minSdk = N, maxSdk = Q)
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

  /** Sets the UICC slots information returned by {@link #getUiccSlotsInfo()}. */
  public void setUiccSlotsInfo(/*UiccSlotInfo[]*/ Object uiccSlotsInfos) {
    this.uiccSlotInfos = uiccSlotsInfos;
  }

  /** Returns the UICC slots information set by {@link #setUiccSlotsInfo}. */
  @Implementation(minSdk = P)
  @HiddenApi
  protected /*UiccSlotInfo[]*/ Object getUiccSlotsInfo() {
    return uiccSlotInfos;
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

  /**
   * @deprecated {@link TelephonyManager#getNetworkType()} was replaced with {@link
   *     TelephonyManager#getDataNetworkType()} in Android N, and has been deprecated in Android R.
   *     Use {@link #setDataNetworkType instead}.
   */
  @Deprecated
  public void setNetworkType(int networkType) {
    this.networkType = networkType;
  }

  /**
   * Returns whatever value was set by the last call to {@link #setDataNetworkType}, defaulting to
   * {@link TelephonyManager#NETWORK_TYPE_UNKNOWN} if it was never called.
   */
  @Implementation(minSdk = N)
  protected int getDataNetworkType() {
    return dataNetworkType;
  }

  /**
   * Sets the value to be returned by calls to {@link #getDataNetworkType}. This <b>should</b>
   * correspond to one of the {@code NETWORK_TYPE_*} constants defined on {@link TelephonyManager},
   * but this is not enforced.
   */
  public void setDataNetworkType(int dataNetworkType) {
    this.dataNetworkType = dataNetworkType;
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

  /**
   * Returns the value set by {@link #setCallbackCellInfos}, defaulting to calling the real {@link
   * TelephonyManager#NETWORK_TYPE_UNKNOWN} if it was never called.
   */
  @Implementation(minSdk = Q)
  protected void requestCellInfoUpdate(Object cellInfoExecutor, Object cellInfoCallback) {
    Executor executor = (Executor) cellInfoExecutor;
    if (callbackCellInfos == null) {
      // ignore
    } else if (requestCellInfoUpdateErrorCode != 0 || requestCellInfoUpdateDetail != null) {
      // perform the "failure" callback operation via the specified executor
      executor.execute(
          () -> {
            // Must cast 'callback' inside the anonymous class to avoid NoClassDefFoundError when
            // referring to 'CellInfoCallback'.
            CellInfoCallback callback = (CellInfoCallback) cellInfoCallback;
            callback.onError(requestCellInfoUpdateErrorCode, requestCellInfoUpdateDetail);
          });
    } else {
      // perform the "success" callback operation via the specified executor
      executor.execute(
          () -> {
            // Must cast 'callback' inside the anonymous class to avoid NoClassDefFoundError when
            // referring to 'CellInfoCallback'.
            CellInfoCallback callback = (CellInfoCallback) cellInfoCallback;
            callback.onCellInfo(callbackCellInfos);
          });
    }
  }

  /**
   * Sets the value to be returned by calls to {@link requestCellInfoUpdate}. Note that it does not
   * set the value to be returned by calls to {@link getAllCellInfo}; for that, see {@link
   * setAllCellInfo}.
   */
  public void setCallbackCellInfos(List<CellInfo> callbackCellInfos) {
    this.callbackCellInfos = callbackCellInfos;
  }

  /**
   * Sets the values to be returned by a presumed error condition in {@link requestCellInfoUpdate}.
   * These values will persist until cleared: to clear, set (0, null) using this method.
   */
  public void setRequestCellInfoUpdateErrorValues(int errorCode, Throwable detail) {
    requestCellInfoUpdateErrorCode = errorCode;
    requestCellInfoUpdateDetail = detail;
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

  @CallSuper
  protected void initListener(PhoneStateListener listener, int flags) {
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

    if (telephonyDisplayInfo != null
        && ((flags & PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED) != 0)) {
      listener.onDisplayInfoChanged((TelephonyDisplayInfo) telephonyDisplayInfo);
    }
  }

  protected Iterable<PhoneStateListener> getListenersForFlags(int flags) {
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

  /** @return true by default, or the value specified via {@link #setIsSmsCapable(boolean)} */
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
   * Returns {@code true} by default or the value specified via {@link #setVoiceCapable(boolean)}.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isVoiceCapable() {
    return voiceCapable;
  }

  /** Sets the value returned by {@link #isVoiceCapable()}. */
  public void setVoiceCapable(boolean voiceCapable) {
    this.voiceCapable = voiceCapable;
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

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_SERVICE_STATE)) {
      listener.onServiceStateChanged(serviceState);
    }
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

  @Implementation(minSdk = Q)
  protected int getCarrierIdFromSimMccMnc() {
    return carrierIdFromSimMccMnc;
  }

  /** Sets the value to be returned by {@link #getCarrierIdFromSimMccMnc()}. */
  public void setCarrierIdFromSimMccMnc(int carrierIdFromSimMccMnc) {
    this.carrierIdFromSimMccMnc = carrierIdFromSimMccMnc;
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
    checkReadPhoneStatePermission();
    return subscriberId;
  }

  /** Sets the value to be returned by {@link #getSubscriberId()}. */
  public void setSubscriberId(String subscriberId) {
    this.subscriberId = subscriberId;
  }

  /** Returns the value set by {@link #setVisualVoicemailPackageName(String)}. */
  @Implementation(minSdk = O)
  protected String getVisualVoicemailPackageName() {
    return visualVoicemailPackageName;
  }

  /** Sets the value to be returned by {@link #getVisualVoicemailPackageName()}. */
  public void setVisualVoicemailPackageName(String visualVoicemailPackageName) {
    this.visualVoicemailPackageName = visualVoicemailPackageName;
  }

  @Implementation(minSdk = P)
  protected SignalStrength getSignalStrength() {
    return signalStrength;
  }

  /** Sets the value to be returned by {@link #getSignalStrength()} */
  public void setSignalStrength(SignalStrength signalStrength) {
    this.signalStrength = signalStrength;
    for (PhoneStateListener listener :
        getListenersForFlags(PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)) {
      listener.onSignalStrengthsChanged(signalStrength);
    }
  }

  /**
   * Cribbed from {@link android.telephony.PhoneNumberUtils#isEmergencyNumberInternal}.
   *
   * <p>TODO(b/122324733) need better implementation
   */
  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isEmergencyNumber(String number) {

    if (number == null) {
      return false;
    }

    Context context = ReflectionHelpers.getField(realTelephonyManager, "mContext");
    Locale locale = context == null ? null : context.getResources().getConfiguration().locale;
    String defaultCountryIso = locale == null ? null : locale.getCountry();

    int slotId = -1;
    boolean useExactMatch = true;

    // retrieve the list of emergency numbers
    // check read-write ecclist property first
    String ecclist = (slotId <= 0) ? "ril.ecclist" : ("ril.ecclist" + slotId);

    String emergencyNumbers = SystemProperties.get(ecclist, "");

    if (TextUtils.isEmpty(emergencyNumbers)) {
      // then read-only ecclist property since old RIL only uses this
      emergencyNumbers = SystemProperties.get("ro.ril.ecclist");
    }

    if (!TextUtils.isEmpty(emergencyNumbers)) {
      // searches through the comma-separated list for a match,
      // return true if one is found.
      for (String emergencyNum : emergencyNumbers.split(",")) {
        // It is not possible to append additional digits to an emergency number to dial
        // the number in Brazil - it won't connect.
        if (useExactMatch || "BR".equalsIgnoreCase(defaultCountryIso)) {
          if (number.equals(emergencyNum)) {
            return true;
          }
        } else {
          if (number.startsWith(emergencyNum)) {
            return true;
          }
        }
      }
      // no matches found against the list!
      return false;
    }

    emergencyNumbers = ((slotId < 0) ? "112,911,000,08,110,118,119,999" : "112,911");
    for (String emergencyNum : emergencyNumbers.split(",")) {
      if (useExactMatch) {
        if (number.equals(emergencyNum)) {
          return true;
        }
      } else {
        if (number.startsWith(emergencyNum)) {
          return true;
        }
      }
    }

    return false;
  }

  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isPotentialEmergencyNumber(String number) {
    return isEmergencyNumber(number);
  }

  /**
   * Implementation for {@link TelephonyManager#isDataEnabled}.
   *
   * @return False by default, unless set with {@link TelephonyManager#setDataEnabled}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected boolean isDataEnabled() {
    return dataEnabled;
  }

  /**
   * Implementation for {@link TelephonyManager#setDataEnabled}. Marked as public in order to allow
   * it to be used as a test API.
   */
  @Implementation(minSdk = Build.VERSION_CODES.O)
  public void setDataEnabled(boolean enabled) {
    dataEnabled = enabled;
  }

  /**
   * Implementation for {@link TelephonyManager#isRttSupported}.
   *
   * @return False by default, unless set with {@link #setRttSupported(boolean)}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isRttSupported() {
    return isRttSupported;
  }

  /** Sets the value to be returned by {@link #isRttSupported()} */
  public void setRttSupported(boolean isRttSupported) {
    this.isRttSupported = isRttSupported;
  }

  /**
   * Implementation for {@link TelephonyManager#sendDialerSpecialCode(String)}.
   *
   * @param inputCode special code to be sent.
   */
  @Implementation(minSdk = O)
  public void sendDialerSpecialCode(String inputCode) {
    sentDialerSpecialCodes.add(inputCode);
  }

  /**
   * Returns immutable list of special codes sent using {@link
   * TelephonyManager#sendDialerSpecialCode(String)}. Special codes contained in the list are in the
   * order they were sent.
   */
  public List<String> getSentDialerSpecialCodes() {
    return ImmutableList.copyOf(sentDialerSpecialCodes);
  }

  /** Sets the value to be returned by {@link #isHearingAidCompatibilitySupported()}. */
  public void setHearingAidCompatibilitySupported(boolean isSupported) {
    hearingAidCompatibilitySupported = isSupported;
  }

  /**
   * Implementation for {@link TelephonyManager#isHearingAidCompatibilitySupported()}.
   *
   * @return False by default, unless set with {@link
   *     #setHearingAidCompatibilitySupported(boolean)}.
   */
  @Implementation(minSdk = M)
  protected boolean isHearingAidCompatibilitySupported() {
    return hearingAidCompatibilitySupported;
  }

  /**
   * Creates a {@link TelephonyDisplayInfo}.
   *
   * @param networkType The packet-switching cellular network type (see {@link NetworkType})
   * @param overrideNetworkType The override network type (see {@link OverrideNetworkType})
   */
  public static Object createTelephonyDisplayInfo(
      @NetworkType int networkType, @OverrideNetworkType int overrideNetworkType) {
    return new TelephonyDisplayInfo(networkType, overrideNetworkType);
  }

  /**
   * Sets the current {@link TelephonyDisplayInfo}, and notifies all the {@link PhoneStateListener}s
   * that were registered with the {@link PhoneStateListener#LISTEN_DISPLAY_INFO_CHANGED} flag.
   *
   * @param telephonyDisplayInfo The {@link TelephonyDisplayInfo} to set. May not be null.
   * @throws NullPointerException if telephonyDisplayInfo is null.
   */
  public void setTelephonyDisplayInfo(Object telephonyDisplayInfo) {
    Preconditions.checkNotNull(telephonyDisplayInfo);
    this.telephonyDisplayInfo = telephonyDisplayInfo;

    for (PhoneStateListener listener :
        getListenersForFlags(PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED)) {
      listener.onDisplayInfoChanged((TelephonyDisplayInfo) telephonyDisplayInfo);
    }
  }

  @Implementation(minSdk = R)
  @HiddenApi
  protected boolean isDataConnectionAllowed() {
    return isDataConnectionAllowed;
  }

  public void setIsDataConnectionAllowed(boolean isDataConnectionAllowed) {
    this.isDataConnectionAllowed = isDataConnectionAllowed;
  }
}
