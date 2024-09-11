package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

import android.Manifest.permission;
import android.annotation.CallSuper;
import android.app.ActivityThread;
import android.app.PendingIntent;
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
import android.telephony.CarrierRestrictionRules;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyCallback.CallStateListener;
import android.telephony.TelephonyCallback.CellInfoListener;
import android.telephony.TelephonyCallback.CellLocationListener;
import android.telephony.TelephonyCallback.DisplayInfoListener;
import android.telephony.TelephonyCallback.ServiceStateListener;
import android.telephony.TelephonyCallback.SignalStrengthsListener;
import android.telephony.TelephonyDisplayInfo;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.CellInfoCallback;
import android.telephony.VisualVoicemailSmsFilterSettings;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.versioning.AndroidVersions.U;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link TelephonyManager}. */
@Implements(value = TelephonyManager.class)
public class ShadowTelephonyManager {

  @RealObject protected TelephonyManager realTelephonyManager;

  private final Map<PhoneStateListener, Integer> phoneStateRegistrations =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private final /*List<TelephonyCallback>*/ List<Object> telephonyCallbackRegistrations =
      new ArrayList<>();
  private static final Map<Integer, String> slotIndexToDeviceId =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<Integer, String> slotIndexToImei =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<Integer, String> slotIndexToMeid =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<PhoneAccountHandle, Boolean> voicemailVibrationEnabledMap =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<PhoneAccountHandle, Uri> voicemailRingtoneUriMap =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<PhoneAccountHandle, TelephonyManager> phoneAccountToTelephonyManagers =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<PhoneAccountHandle, Integer> phoneAccountHandleSubscriptionId =
      Collections.synchronizedMap(new LinkedHashMap<>());

  private PhoneStateListener lastListener;
  private /*TelephonyCallback*/ Object lastTelephonyCallback;
  private int lastEventFlags;

  private String deviceId;
  private String deviceSoftwareVersion;
  private String imei;
  private String meid;
  private String groupIdLevel1;
  private String networkOperatorName = "";
  private String networkCountryIso;
  private String networkOperator = "";
  private String networkSpecifier = "";
  private Locale simLocale;
  private String simOperator = "";
  private String simOperatorName;
  private String simSerialNumber;
  private static volatile boolean readPhoneStatePermission = true;
  private int phoneType = TelephonyManager.PHONE_TYPE_GSM;
  private String line1Number;
  private int networkType;
  private int dataNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
  private int voiceNetworkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
  private static volatile List<CellInfo> allCellInfo = Collections.emptyList();
  private static volatile List<CellInfo> callbackCellInfos = null;
  private static volatile CellLocation cellLocation = null;
  private int callState = CALL_STATE_IDLE;
  private int dataState = TelephonyManager.DATA_DISCONNECTED;
  private int dataActivity = TelephonyManager.DATA_ACTIVITY_NONE;
  private String incomingPhoneNumber = null;
  private static volatile boolean isSmsCapable = true;
  private static volatile boolean voiceCapable = true;
  private String voiceMailNumber;
  private String voiceMailAlphaTag;
  private static volatile int phoneCount = 1;
  private static volatile int activeModemCount = 1;
  private static volatile Map<Integer, TelephonyManager> subscriptionIdsToTelephonyManagers =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private PersistableBundle carrierConfig;
  private ServiceState serviceState;
  private boolean isNetworkRoaming;
  private static final Map<Integer, Integer> simStates =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<Integer, Integer> currentPhoneTypes =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<Integer, List<String>> carrierPackageNames =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final Map<Integer, String> simCountryIsoMap =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private int simCarrierId;
  private int simSpecificCarrierId;
  private CharSequence simCarrierIdName;
  private int carrierIdFromSimMccMnc;
  private String subscriberId;
  private static volatile /*UiccSlotInfo[]*/ Object uiccSlotInfos;
  private static volatile /*List<UiccCardInfo>*/ Object uiccCardsInfo = new ArrayList<>();
  private String visualVoicemailPackageName = null;
  private SignalStrength signalStrength;
  private boolean dataEnabled = false;
  private final Set<Integer> dataDisabledReasons = new HashSet<>();
  private boolean isRttSupported;
  private static volatile boolean isTtyModeSupported;
  private static final Map<Integer, Boolean> subIdToHasCarrierPrivileges =
      Collections.synchronizedMap(new LinkedHashMap<>());
  private static final List<String> sentDialerSpecialCodes = new ArrayList<>();
  private static volatile boolean hearingAidCompatibilitySupported = false;
  private int requestCellInfoUpdateErrorCode = 0;
  private Throwable requestCellInfoUpdateDetail = null;
  private Object telephonyDisplayInfo;
  private boolean isDataConnectionAllowed;
  private static int callComposerStatus = 0;
  private VisualVoicemailSmsParams lastVisualVoicemailSmsParams;
  private VisualVoicemailSmsFilterSettings visualVoicemailSmsFilterSettings;
  private static volatile boolean emergencyCallbackMode;
  private static Map<Integer, List<EmergencyNumber>> emergencyNumbersList;
  private static volatile boolean isDataRoamingEnabled;
  private /*CarrierRestrictionRules*/ Object carrierRestrictionRules;
  private final AtomicInteger modemRebootCount = new AtomicInteger();

  /**
   * Should be {@link TelephonyManager.BootstrapAuthenticationCallback} but this object was
   * introduced in Android S, so we are using Object to avoid breaking other SDKs
   *
   * <p>XXX Look into using the real types if we're now compiling against S
   */
  private Object callback;

  private static volatile /*PhoneCapability*/ Object phoneCapability;

  static {
    resetAllSimStates();
    resetAllSimCountryIsos();
  }

  @Resetter
  public static void reset() {
    subscriptionIdsToTelephonyManagers.clear();
    resetAllSimStates();
    currentPhoneTypes.clear();
    carrierPackageNames.clear();
    resetAllSimCountryIsos();
    slotIndexToDeviceId.clear();
    slotIndexToImei.clear();
    slotIndexToMeid.clear();
    voicemailVibrationEnabledMap.clear();
    voicemailRingtoneUriMap.clear();
    phoneAccountToTelephonyManagers.clear();
    phoneAccountHandleSubscriptionId.clear();
    subIdToHasCarrierPrivileges.clear();
    allCellInfo = Collections.emptyList();
    cellLocation = null;
    callbackCellInfos = null;
    uiccSlotInfos = null;
    uiccCardsInfo = new ArrayList<>();
    callComposerStatus = 0;
    emergencyCallbackMode = false;
    emergencyNumbersList = null;
    phoneCapability = null;
    isTtyModeSupported = false;
    readPhoneStatePermission = true;
    isSmsCapable = true;
    voiceCapable = true;
    phoneCount = 1;
    activeModemCount = 1;
    sentDialerSpecialCodes.clear();
    hearingAidCompatibilitySupported = false;
  }

  @Implementation(minSdk = S)
  protected void setCallComposerStatus(int callComposerStatus) {
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
      int appType,
      Uri nafId,
      @ClassName("android.telephony.gba.UaSecurityProtocolIdentifier") Object securityProtocol,
      boolean forceBootStrapping,
      Executor e,
      @ClassName("android.telephony.TelephonyManager$BootstrapAuthenticationCallback")
          Object callback) {
    this.callback = callback;
  }

  public void setPhoneCapability(/*PhoneCapability*/ Object phoneCapability) {
    ShadowTelephonyManager.phoneCapability = phoneCapability;
  }

  @Implementation(minSdk = S)
  @HiddenApi
  public @ClassName("android.telephony.PhoneCapability") Object getPhoneCapability() {
    return phoneCapability;
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

  @Implementation(minSdk = S)
  public void registerTelephonyCallback(
      Executor executor, @ClassName("android.telephony.TelephonyCallback") Object callback) {
    Preconditions.checkArgument(callback instanceof TelephonyCallback);
    lastTelephonyCallback = callback;
    initTelephonyCallback(callback);
    telephonyCallbackRegistrations.add(callback);
  }

  @Implementation(minSdk = TIRAMISU)
  protected void registerTelephonyCallback(
      int includeLocationData,
      Executor executor,
      @ClassName("android.telephony.TelephonyCallback") Object callback) {
    registerTelephonyCallback(executor, callback);
  }

  @Implementation(minSdk = S)
  public void unregisterTelephonyCallback(
      @ClassName("android.telephony.TelephonyCallback") Object callback) {
    telephonyCallbackRegistrations.remove(callback);
  }

  /** Returns the most recent callback passed to #registerTelephonyCallback(). */
  public /*TelephonyCallback*/ Object getLastTelephonyCallback() {
    return lastTelephonyCallback;
  }

  /** Call state may be specified via {@link #setCallState(int)}. */
  @Implementation(minSdk = S)
  protected int getCallStateForSubscription() {
    checkReadPhoneStatePermission();
    return callState;
  }

  /** Call state may be specified via {@link #setCallState(int)}. */
  @Implementation
  protected int getCallState() {
    checkReadPhoneStatePermission();
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
    if (VERSION.SDK_INT >= S) {
      for (CallStateListener listener : getCallbackForListener(CallStateListener.class)) {
        listener.onCallStateChanged(callState);
      }
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

  /**
   * Data activity may be specified via {@link #setDataActivity(int)}. If no override is set, this
   * defaults to {@link TelephonyManager#DATA_ACTIVITY_NONE}.
   */
  @Implementation
  protected int getDataActivity() {
    return dataActivity;
  }

  /**
   * Sets the value to be returned by calls to {@link #getDataActivity()}. This <b>should</b>
   * correspond to one of the {@code DATA_ACTIVITY_*} constants defined on {@link TelephonyManager},
   * but this is not enforced.
   */
  public void setDataActivity(int dataActivity) {
    this.dataActivity = dataActivity;
  }

  @Implementation
  protected String getDeviceId() {
    checkReadPhoneStatePermission();
    return deviceId;
  }

  public void setDeviceId(String newDeviceId) {
    deviceId = newDeviceId;
  }

  @Implementation
  protected String getDeviceSoftwareVersion() {
    checkReadPhoneStatePermission();
    return deviceSoftwareVersion;
  }

  public void setDeviceSoftwareVersion(String newDeviceSoftwareVersion) {
    deviceSoftwareVersion = newDeviceSoftwareVersion;
  }

  @Implementation(minSdk = LOLLIPOP_MR1, maxSdk = U.SDK_INT)
  public void setNetworkOperatorName(String networkOperatorName) {
    this.networkOperatorName = networkOperatorName;
  }

  @Implementation(minSdk = V.SDK_INT)
  public void setNetworkOperatorNameForPhone(
      /* Ignored */ int phoneId, String networkOperatorName) {
    setNetworkOperatorName(networkOperatorName);
  }

  @Implementation
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

  @Implementation(minSdk = LOLLIPOP_MR1, maxSdk = P)
  public void setNetworkCountryIso(String networkCountryIso) {
    this.networkCountryIso = networkCountryIso;
  }

  /**
   * Returns the SIM country lowercase. This matches the API this shadows:
   * https://developer.android.com/reference/android/telephony/TelephonyManager#getNetworkCountryIso().
   */
  @Implementation
  protected String getNetworkCountryIso() {
    return networkCountryIso == null ? null : Ascii.toLowerCase(networkCountryIso);
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

  public void setNetworkSpecifier(String networkSpecifier) {
    this.networkSpecifier = networkSpecifier;
  }

  @Implementation(minSdk = O)
  protected String getNetworkSpecifier() {
    return networkSpecifier;
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

  @Implementation(minSdk = LOLLIPOP_MR1, maxSdk = U.SDK_INT)
  public void setSimOperatorName(String simOperatorName) {
    this.simOperatorName = simOperatorName;
  }

  @Implementation(minSdk = V.SDK_INT)
  public void setSimOperatorNameForPhone(/* Ignored */ int phoneId, String name) {
    setSimOperatorName(name);
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

  /**
   * Returns the SIM country lowercase. This matches the API it shadows:
   * https://developer.android.com/reference/android/telephony/TelephonyManager#getSimCountryIso().
   */
  @Implementation
  protected String getSimCountryIso() {
    String simCountryIso = simCountryIsoMap.get(/* subId= */ 0);
    return simCountryIso == null ? simCountryIso : Ascii.toLowerCase(simCountryIso);
  }

  @Implementation(minSdk = N, maxSdk = Q)
  @HiddenApi
  protected String getSimCountryIso(int subId) {
    return simCountryIsoMap.get(subId);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  public void setSimCountryIso(String simCountryIso) {
    setSimCountryIso(/* subId= */ 0, simCountryIso);
  }

  /** Sets the {@code simCountryIso} for the given {@code subId}. */
  public void setSimCountryIso(int subId, String simCountryIso) {
    simCountryIsoMap.put(subId, simCountryIso);
  }

  /** Clears {@code subId} to simCountryIso mapping and resets to default state. */
  public static void resetAllSimCountryIsos() {
    simCountryIsoMap.clear();
    simCountryIsoMap.put(0, "");
  }

  /**
   * Clears {@code subId} to simCountryIso mapping and resets to default state.
   *
   * @deprecated for resetAllSimCountryIsos
   */
  @Deprecated
  public void resetSimCountryIsos() {
    resetAllSimCountryIsos();
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
    return simStates.getOrDefault(slotIndex, TelephonyManager.SIM_STATE_UNKNOWN);
  }

  /** Sets the UICC slots information returned by {@link #getUiccSlotsInfo()}. */
  public void setUiccSlotsInfo(/*UiccSlotInfo[]*/ Object uiccSlotsInfos) {
    ShadowTelephonyManager.uiccSlotInfos = uiccSlotsInfos;
  }

  /** Returns the UICC slots information set by {@link #setUiccSlotsInfo}. */
  @Implementation(minSdk = P)
  @HiddenApi
  protected @ClassName("android.telephony.UiccSlotInfo[]") Object getUiccSlotsInfo() {
    return uiccSlotInfos;
  }

  /** Sets the UICC cards information returned by {@link #getUiccCardsInfo()}. */
  public void setUiccCardsInfo(/*List<UiccCardInfo>*/ Object uiccCardsInfo) {
    ShadowTelephonyManager.uiccCardsInfo = uiccCardsInfo;
  }

  /** Returns the UICC cards information set by {@link #setUiccCardsInfo}. */
  @Implementation(minSdk = Q)
  @HiddenApi
  protected @ClassName("java.util.List<android.telephony.UiccCardInfo>") Object getUiccCardsInfo() {
    return uiccCardsInfo;
  }

  /** Clears {@code slotIndex} to state mapping and resets to default state. */
  public static void resetAllSimStates() {
    simStates.clear();
    simStates.put(0, TelephonyManager.SIM_STATE_READY);
  }

  /**
   * Clears {@code slotIndex} to state mapping and resets to default state.
   *
   * @deprecated use resetAllSimStates()
   */
  @Deprecated
  public void resetSimStates() {
    resetAllSimStates();
  }

  public void setReadPhoneStatePermission(boolean readPhoneStatePermission) {
    ShadowTelephonyManager.readPhoneStatePermission = readPhoneStatePermission;
  }

  private void checkReadPhoneStatePermission() {
    if (!readPhoneStatePermission) {
      throw new SecurityException();
    }
  }

  private void checkReadPrivilegedPhoneStatePermission() {
    if (!checkPermission(permission.READ_PRIVILEGED_PHONE_STATE)) {
      throw new SecurityException();
    }
  }

  private void checkModifyPhoneStatePermission() {
    if (!checkPermission(permission.MODIFY_PHONE_STATE)) {
      throw new SecurityException();
    }
  }

  static ShadowInstrumentation getShadowInstrumentation() {
    ActivityThread activityThread = (ActivityThread) RuntimeEnvironment.getActivityThread();
    return Shadow.extract(activityThread.getInstrumentation());
  }

  static boolean checkPermission(String permission) {
    return getShadowInstrumentation()
            .checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid())
        == PERMISSION_GRANTED;
  }

  @Implementation
  protected int getPhoneType() {
    return phoneType;
  }

  @Implementation(minSdk = LOLLIPOP_MR1, maxSdk = U.SDK_INT)
  public void setPhoneType(int phoneType) {
    this.phoneType = phoneType;
  }

  @Implementation(minSdk = V.SDK_INT)
  public void setPhoneType(/* Ignored */ int phoneId, int type) {
    setPhoneType(type);
  }

  @Implementation
  protected String getLine1Number() {
    checkReadPhoneStatePermission();
    return line1Number;
  }

  public void setLine1Number(String line1Number) {
    this.line1Number = line1Number;
  }

  @Implementation
  protected int getNetworkType() {
    checkReadPhoneStatePermission();
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
    checkReadPhoneStatePermission();
    return dataNetworkType;
  }

  /**
   * Sets the value to be returned by calls to {@link #getDataNetworkType}. This <b>should</b>
   * correspond to one of the {@code NETWORK_TYPE_*} constants defined on {@link TelephonyManager},
   * but this is not enforced.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  public void setDataNetworkType(int dataNetworkType) {
    this.dataNetworkType = dataNetworkType;
  }

  /**
   * Returns whatever value was set by the last call to {@link #setVoiceNetworkType}, defaulting to
   * {@link TelephonyManager#NETWORK_TYPE_UNKNOWN} if it was never called.
   *
   * <p>An exception will be thrown if the READ_PHONE_STATE permission has not been granted.
   */
  @Implementation(minSdk = N)
  protected int getVoiceNetworkType() {
    checkReadPhoneStatePermission();
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

  @Implementation
  protected List<CellInfo> getAllCellInfo() {
    return allCellInfo;
  }

  public void setAllCellInfo(List<CellInfo> allCellInfo) {
    ShadowTelephonyManager.allCellInfo = allCellInfo;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_INFO)) {
      listener.onCellInfoChanged(allCellInfo);
    }
    if (VERSION.SDK_INT >= S) {
      for (CellInfoListener listener : getCallbackForListener(CellInfoListener.class)) {
        listener.onCellInfoChanged(allCellInfo);
      }
    }
  }

  /**
   * Returns the value set by {@link #setCallbackCellInfos}, defaulting to calling the real {@link
   * TelephonyManager#NETWORK_TYPE_UNKNOWN} if it was never called.
   */
  @Implementation(minSdk = Q)
  protected void requestCellInfoUpdate(
      Executor executor,
      @ClassName("android.telephony.TelephonyManager$CellInfoCallback") Object cellInfoCallback) {
    List<CellInfo> callbackCellInfos = ShadowTelephonyManager.callbackCellInfos;
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
    ShadowTelephonyManager.callbackCellInfos = callbackCellInfos;
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
    return ShadowTelephonyManager.cellLocation;
  }

  public void setCellLocation(CellLocation cellLocation) {
    ShadowTelephonyManager.cellLocation = cellLocation;
    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_LOCATION)) {
      listener.onCellLocationChanged(cellLocation);
    }
    if (VERSION.SDK_INT >= S) {
      for (CellLocationListener listener : getCallbackForListener(CellLocationListener.class)) {
        listener.onCellLocationChanged(cellLocation);
      }
    }
  }

  @Implementation
  protected String getGroupIdLevel1() {
    checkReadPhoneStatePermission();
    return this.groupIdLevel1;
  }

  public void setGroupIdLevel1(String groupIdLevel1) {
    this.groupIdLevel1 = groupIdLevel1;
  }

  @CallSuper
  protected void initListener(PhoneStateListener listener, int flags) {
    // grab the state "atomically" before doing callbacks, in case they modify the state
    String incomingPhoneNumber = this.incomingPhoneNumber;
    List<CellInfo> allCellInfo = ShadowTelephonyManager.allCellInfo;
    CellLocation cellLocation = ShadowTelephonyManager.cellLocation;
    Object telephonyDisplayInfo = this.telephonyDisplayInfo;
    ServiceState serviceState = this.serviceState;

    if ((flags & LISTEN_CALL_STATE) != 0) {
      listener.onCallStateChanged(callState, incomingPhoneNumber);
    }
    if ((flags & LISTEN_CELL_INFO) != 0) {
      listener.onCellInfoChanged(allCellInfo);
    }
    if ((flags & LISTEN_CELL_LOCATION) != 0) {
      listener.onCellLocationChanged(cellLocation);
    }

    if (telephonyDisplayInfo != null
        && ((flags & PhoneStateListener.LISTEN_DISPLAY_INFO_CHANGED) != 0)) {
      listener.onDisplayInfoChanged((TelephonyDisplayInfo) telephonyDisplayInfo);
    }

    if (serviceState != null && ((flags & PhoneStateListener.LISTEN_SERVICE_STATE) != 0)) {
      listener.onServiceStateChanged(serviceState);
    }
  }

  @CallSuper
  protected void initTelephonyCallback(Object callback) {
    if (VERSION.SDK_INT < S) {
      return;
    }
    // grab the state "atomically" before doing callbacks, in case they modify the state
    int callState = this.callState;
    List<CellInfo> allCellInfo = ShadowTelephonyManager.allCellInfo;
    CellLocation cellLocation = ShadowTelephonyManager.cellLocation;
    Object telephonyDisplayInfo = this.telephonyDisplayInfo;
    ServiceState serviceState = this.serviceState;

    if (callback instanceof CallStateListener) {
      ((CallStateListener) callback).onCallStateChanged(callState);
    }
    if (callback instanceof CellInfoListener) {
      ((CellInfoListener) callback).onCellInfoChanged(allCellInfo);
    }
    if (callback instanceof CellLocationListener) {
      ((CellLocationListener) callback).onCellLocationChanged(cellLocation);
    }
    if (telephonyDisplayInfo != null && callback instanceof DisplayInfoListener) {
      ((DisplayInfoListener) callback)
          .onDisplayInfoChanged((TelephonyDisplayInfo) telephonyDisplayInfo);
    }
    if (serviceState != null && callback instanceof ServiceStateListener) {
      ((ServiceStateListener) callback).onServiceStateChanged(serviceState);
    }
  }

  protected Iterable<PhoneStateListener> getListenersForFlags(int flags) {
    return Iterables.filter(
        ImmutableSet.copyOf(phoneStateRegistrations.keySet()),
        new Predicate<PhoneStateListener>() {
          @Override
          public boolean apply(PhoneStateListener input) {
            // only select PhoneStateListeners with matching flags
            return (phoneStateRegistrations.get(input) & flags) != 0;
          }
        });
  }

  /**
   * Returns a view of {@code telephonyCallbackRegistrations} containing all elements that are of
   * the type {@code clazz}.
   */
  protected <T> Iterable<T> getCallbackForListener(Class<T> clazz) {
    // Only selects TelephonyCallback with matching class.
    return Iterables.filter(ImmutableList.copyOf(telephonyCallbackRegistrations), clazz);
  }

  /**
   * @return true by default, or the value specified via {@link #setIsSmsCapable(boolean)}
   */
  @Implementation
  protected boolean isSmsCapable() {
    return isSmsCapable;
  }

  /** Sets the value returned by {@link TelephonyManager#isSmsCapable()}. */
  public void setIsSmsCapable(boolean isSmsCapable) {
    ShadowTelephonyManager.isSmsCapable = isSmsCapable;
  }

  /**
   * Returns a new empty {@link PersistableBundle} by default, or the value specified via {@link
   * #setCarrierConfig(PersistableBundle)}.
   */
  @Implementation(minSdk = O)
  protected PersistableBundle getCarrierConfig() {
    checkReadPhoneStatePermission();
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
    checkReadPhoneStatePermission();
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
    checkReadPhoneStatePermission();
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
    ShadowTelephonyManager.phoneCount = phoneCount;
  }

  /** Returns 1 by default or the value specified via {@link #setActiveModemCount(int)}. */
  @Implementation(minSdk = R)
  protected int getActiveModemCount() {
    return activeModemCount;
  }

  /** Sets the value returned by {@link TelephonyManager#getActiveModemCount()}. */
  public void setActiveModemCount(int activeModemCount) {
    ShadowTelephonyManager.activeModemCount = activeModemCount;
  }

  /**
   * Returns {@code null} by default or the value specified via {@link #setDeviceId(int, String)}.
   */
  @Implementation(minSdk = M)
  protected String getDeviceId(int slot) {
    checkReadPhoneStatePermission();
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
    ShadowTelephonyManager.voiceCapable = voiceCapable;
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
    checkReadPhoneStatePermission();
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
    checkReadPhoneStatePermission();
    return serviceState;
  }

  /**
   * Returns {@code null} by default or the value specified via {@link
   * #setServiceState(ServiceState)}
   */
  @Implementation(minSdk = TIRAMISU)
  protected ServiceState getServiceState(int includeLocationData) {
    return getServiceState();
  }

  /** Sets the value returned by {@link TelephonyManager#getServiceState()}. */
  public void setServiceState(ServiceState serviceState) {
    this.serviceState = serviceState;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_SERVICE_STATE)) {
      listener.onServiceStateChanged(serviceState);
    }
    if (VERSION.SDK_INT >= S) {
      for (ServiceStateListener listener : getCallbackForListener(ServiceStateListener.class)) {
        listener.onServiceStateChanged(serviceState);
      }
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
    return currentPhoneTypes.getOrDefault(subId, TelephonyManager.PHONE_TYPE_NONE);
  }

  /** Sets the phone type for the given {@code subId}. */
  public void setCurrentPhoneType(int subId, int phoneType) {
    currentPhoneTypes.put(subId, phoneType);
  }

  /** Removes all {@code subId} to {@code phoneType} mappings. */
  public static void clearPhoneTypes() {
    currentPhoneTypes.clear();
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected List<String> getCarrierPackageNamesForIntentAndPhone(Intent intent, int phoneId) {
    return carrierPackageNames.get(phoneId);
  }

  @Implementation
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

  @Implementation(minSdk = Q)
  protected int getSimSpecificCarrierId() {
    return simSpecificCarrierId;
  }

  /** Sets the value to be returned by {@link #getSimSpecificCarrierId()}. */
  public void setSimSpecificCarrierId(int simSpecificCarrierId) {
    this.simSpecificCarrierId = simSpecificCarrierId;
  }

  @Implementation(minSdk = P)
  protected CharSequence getSimCarrierIdName() {
    return simCarrierIdName;
  }

  /** Sets the value to be returned by {@link #getSimCarrierIdName()}. */
  public void setSimCarrierIdName(CharSequence simCarrierIdName) {
    this.simCarrierIdName = simCarrierIdName;
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

  @Implementation(minSdk = R)
  protected int getSubscriptionId(PhoneAccountHandle handle) {
    checkReadPhoneStatePermission();
    return phoneAccountHandleSubscriptionId.get(handle);
  }

  public void setPhoneAccountHandleSubscriptionId(PhoneAccountHandle handle, int subscriptionId) {
    phoneAccountHandleSubscriptionId.put(handle, subscriptionId);
  }

  /** Returns the value set by {@link #setVisualVoicemailPackageName(String)}. */
  @Implementation(minSdk = O)
  protected String getVisualVoicemailPackageName() {
    checkReadPhoneStatePermission();
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
    if (VERSION.SDK_INT >= S) {
      for (SignalStrengthsListener listener :
          getCallbackForListener(SignalStrengthsListener.class)) {
        listener.onSignalStrengthsChanged(signalStrength);
      }
    }
  }

  /**
   * Cribbed from {@link android.telephony.PhoneNumberUtils#isEmergencyNumberInternal}.
   *
   * <p>TODO: need better implementation
   */
  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected boolean isEmergencyNumber(String number) {
    if (ShadowServiceManager.getService(Context.TELEPHONY_SERVICE) == null) {
      throw new IllegalStateException("telephony service is null.");
    }

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

  /**
   * Emergency Callback Mode (ECBM) is typically set by the carrier, for a time window of 5 minutes
   * after the last outgoing emergency call. The user can exit ECBM via a system notification.
   *
   * @param emergencyCallbackMode whether the device is in ECBM or not.
   */
  public void setEmergencyCallbackMode(boolean emergencyCallbackMode) {
    ShadowTelephonyManager.emergencyCallbackMode = emergencyCallbackMode;
  }

  @Implementation(minSdk = Build.VERSION_CODES.O)
  protected boolean getEmergencyCallbackMode() {
    checkReadPrivilegedPhoneStatePermission();
    return emergencyCallbackMode;
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
    checkReadPhoneStatePermission();
    return dataEnabled;
  }

  /**
   * Implementation for {@link TelephonyManager#isDataEnabledForReason}.
   *
   * @return True by default, unless reason is set to false with {@link
   *     TelephonyManager#setDataEnabledForReason}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.S)
  protected boolean isDataEnabledForReason(@TelephonyManager.DataEnabledReason int reason) {
    checkReadPhoneStatePermission();
    return !dataDisabledReasons.contains(reason);
  }

  /**
   * Implementation for {@link TelephonyManager#setDataEnabled}. Marked as public in order to allow
   * it to be used as a test API.
   */
  @Implementation(minSdk = Build.VERSION_CODES.O)
  public void setDataEnabled(boolean enabled) {
    setDataEnabledForReason(TelephonyManager.DATA_ENABLED_REASON_USER, enabled);
  }

  /**
   * Implementation for {@link TelephonyManager#setDataEnabledForReason}. Marked as public in order
   * to allow it to be used as a test API.
   */
  @Implementation(minSdk = Build.VERSION_CODES.S)
  public void setDataEnabledForReason(
      @TelephonyManager.DataEnabledReason int reason, boolean enabled) {
    if (enabled) {
      dataDisabledReasons.remove(reason);
    } else {
      dataDisabledReasons.add(reason);
    }
    dataEnabled = dataDisabledReasons.isEmpty();
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
   * Implementation for {@link TelephonyManager#isTtyModeSupported}.
   *
   * @return False by default, unless set with {@link #setTtyModeSupported(boolean)}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.M)
  protected boolean isTtyModeSupported() {
    checkReadPhoneStatePermission();
    return isTtyModeSupported;
  }

  /** Sets the value to be returned by {@link #isTtyModeSupported()} */
  public void setTtyModeSupported(boolean isTtyModeSupported) {
    ShadowTelephonyManager.isTtyModeSupported = isTtyModeSupported;
  }

  /**
   * @return False by default, unless set with {@link #setHasCarrierPrivileges(int, boolean)}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.N)
  @HiddenApi
  protected boolean hasCarrierPrivileges(int subId) {
    return subIdToHasCarrierPrivileges.getOrDefault(subId, false);
  }

  public void setHasCarrierPrivileges(boolean hasCarrierPrivileges) {
    int subId = ReflectionHelpers.callInstanceMethod(realTelephonyManager, "getSubId");
    setHasCarrierPrivileges(subId, hasCarrierPrivileges);
  }

  /** Sets the {@code hasCarrierPrivileges} for the given {@code subId}. */
  public void setHasCarrierPrivileges(int subId, boolean hasCarrierPrivileges) {
    subIdToHasCarrierPrivileges.put(subId, hasCarrierPrivileges);
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
    if (VERSION.SDK_INT >= S) {
      for (DisplayInfoListener listener : getCallbackForListener(DisplayInfoListener.class)) {
        listener.onDisplayInfoChanged((TelephonyDisplayInfo) telephonyDisplayInfo);
      }
    }
  }

  @Implementation(minSdk = R)
  @HiddenApi
  protected boolean isDataConnectionAllowed() {
    checkReadPhoneStatePermission();
    return isDataConnectionAllowed;
  }

  public void setIsDataConnectionAllowed(boolean isDataConnectionAllowed) {
    this.isDataConnectionAllowed = isDataConnectionAllowed;
  }

  @Implementation(minSdk = O)
  public void sendVisualVoicemailSms(
      String number, int port, String text, PendingIntent sentIntent) {
    lastVisualVoicemailSmsParams = new VisualVoicemailSmsParams(number, port, text, sentIntent);
  }

  public VisualVoicemailSmsParams getLastSentVisualVoicemailSmsParams() {
    return lastVisualVoicemailSmsParams;
  }

  /**
   * Implementation for {@link
   * TelephonyManager#setVisualVoicemailSmsFilterSettings(VisualVoicemailSmsFilterSettings)}.
   *
   * @param settings The settings for the filter, or null to disable the filter.
   */
  @Implementation(minSdk = O)
  public void setVisualVoicemailSmsFilterSettings(VisualVoicemailSmsFilterSettings settings) {
    visualVoicemailSmsFilterSettings = settings;
  }

  /** Returns the last set {@link VisualVoicemailSmsFilterSettings}. */
  public VisualVoicemailSmsFilterSettings getVisualVoicemailSmsFilterSettings() {
    return visualVoicemailSmsFilterSettings;
  }

  /** Testable parameters from calls to {@link #sendVisualVoicemailSms}. */
  public static class VisualVoicemailSmsParams {
    private final String destinationAddress;
    private final int port;
    private final String text;
    private final PendingIntent sentIntent;

    public VisualVoicemailSmsParams(
        String destinationAddress, int port, String text, PendingIntent sentIntent) {
      this.destinationAddress = destinationAddress;
      this.port = port;
      this.text = text;
      this.sentIntent = sentIntent;
    }

    public String getDestinationAddress() {
      return destinationAddress;
    }

    public int getPort() {
      return port;
    }

    public String getText() {
      return text;
    }

    public PendingIntent getSentIntent() {
      return sentIntent;
    }
  }

  /**
   * Sets the emergency numbers list returned by {@link TelephonyManager#getEmergencyNumberList}.
   */
  public static void setEmergencyNumberList(
      Map<Integer, List<EmergencyNumber>> emergencyNumbersList) {
    ShadowTelephonyManager.emergencyNumbersList = emergencyNumbersList;
  }

  /**
   * Implementation for {@link TelephonyManager#getEmergencyNumberList}.
   *
   * @return an immutable map by default, unless set with {@link #setEmergencyNumberList}.
   */
  @Implementation(minSdk = R)
  protected Map<Integer, List<EmergencyNumber>> getEmergencyNumberList() {
    if (ShadowTelephonyManager.emergencyNumbersList != null) {
      return ShadowTelephonyManager.emergencyNumbersList;
    }
    return ImmutableMap.of();
  }

  /**
   * Implementation for {@link TelephonyManager#isDataRoamingEnabled}.
   *
   * @return False by default, unless set with {@link #setDataRoamingEnabled(boolean)}.
   */
  @Implementation(minSdk = Q)
  protected boolean isDataRoamingEnabled() {
    checkReadPhoneStatePermission();
    return isDataRoamingEnabled;
  }

  /** Sets the value to be returned by {@link #isDataRoamingEnabled()} */
  @Implementation(minSdk = Q)
  protected void setDataRoamingEnabled(boolean isDataRoamingEnabled) {
    ShadowTelephonyManager.isDataRoamingEnabled = isDataRoamingEnabled;
  }

  /**
   * Sets the value to be returned by {@link #getCarrierRestrictionRules()}. Marked as public in
   * order to allow it to be used as a test API.
   *
   * @param carrierRestrictionRules An object of type {@link CarrierRestrictionRules}
   */
  public void setCarrierRestrictionRules(Object carrierRestrictionRules) {
    Preconditions.checkState(carrierRestrictionRules instanceof CarrierRestrictionRules);
    this.carrierRestrictionRules = carrierRestrictionRules;
  }

  /**
   * Implementation for {@link TelephonyManager#getCarrierRestrictionRules} that is set for tests by
   * {@link TelephonyManager#setCarrierRestrictionRules}.
   */
  @Implementation(minSdk = Q)
  protected @ClassName("android.telephony.CarrierRestrictionRules") Object
      getCarrierRestrictionRules() {
    return carrierRestrictionRules;
  }

  /** Implementation for {@link TelephonyManager#rebootModem} */
  @Implementation(minSdk = Build.VERSION_CODES.TIRAMISU)
  protected void rebootModem() {
    checkModifyPhoneStatePermission();
    modemRebootCount.incrementAndGet();
  }

  public int getModemRebootCount() {
    return modemRebootCount.get();
  }
}
