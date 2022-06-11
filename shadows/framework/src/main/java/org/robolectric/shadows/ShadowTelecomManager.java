package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.base.Verify.verifyNotNull;

import android.annotation.SystemApi;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.ConnectionRequest;
import android.telecom.ConnectionService;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.text.TextUtils;
import android.util.ArrayMap;
import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = TelecomManager.class, minSdk = LOLLIPOP)
public class ShadowTelecomManager {

  /**
   * Mode describing how the shadow handles incoming ({@link TelecomManager#addNewIncomingCall}) and
   * outgoing ({@link TelecomManager#placeCall}) call requests.
   */
  public enum CallRequestMode {
    /** Automatically allows all call requests. */
    ALLOW_ALL,

    /** Automatically denies all call requests. */
    DENY_ALL,

    /**
     * Do not automatically allow or deny any call requests. Instead, call requests should be
     * allowed or denied manually by calling the following methods:
     *
     * <ul>
     *   <li>{@link #allowIncomingCall(IncomingCallRecord)}
     *   <li>{@link #denyIncomingCall(IncomingCallRecord)}
     *   <li>{@link #allowOutgoingCall(OutgoingCallRecord)}
     *   <li>{@link #denyOutgoingCall(OutgoingCallRecord)}
     * </ul>
     */
    MANUAL,
  }

  @RealObject
  private TelecomManager realObject;

  private final LinkedHashMap<PhoneAccountHandle, PhoneAccount> accounts = new LinkedHashMap<>();
  private final LinkedHashMap<PhoneAccountHandle, String> voicemailNumbers = new LinkedHashMap<>();

  private final List<IncomingCallRecord> incomingCalls = new ArrayList<>();
  private final List<OutgoingCallRecord> outgoingCalls = new ArrayList<>();
  private final List<UnknownCallRecord> unknownCalls = new ArrayList<>();
  private final Map<String, PhoneAccountHandle> defaultOutgoingPhoneAccounts = new ArrayMap<>();
  private Intent manageBlockNumbersIntent;
  private CallRequestMode callRequestMode = CallRequestMode.MANUAL;
  private PhoneAccountHandle simCallManager;
  private String defaultDialerPackageName;
  private String systemDefaultDialerPackageName;
  private boolean isInCall;
  private boolean ttySupported;
  private PhoneAccountHandle userSelectedOutgoingPhoneAccount;

  public CallRequestMode getCallRequestMode() {
    return callRequestMode;
  }

  public void setCallRequestMode(CallRequestMode callRequestMode) {
    this.callRequestMode = callRequestMode;
  }

  /**
   * Set default outgoing phone account to be returned from {@link
   * #getDefaultOutgoingPhoneAccount(String)} for corresponding {@code uriScheme}.
   */
  public void setDefaultOutgoingPhoneAccount(String uriScheme, PhoneAccountHandle handle) {
    defaultOutgoingPhoneAccounts.put(uriScheme, handle);
  }

  /** Remove default outgoing phone account for corresponding {@code uriScheme}. */
  public void removeDefaultOutgoingPhoneAccount(String uriScheme) {
    defaultOutgoingPhoneAccounts.remove(uriScheme);
  }

  /**
   * Returns default outgoing phone account set through {@link
   * #setDefaultOutgoingPhoneAccount(String, PhoneAccountHandle)} for corresponding {@code
   * uriScheme}.
   */
  @Implementation
  protected PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme) {
    return defaultOutgoingPhoneAccounts.get(uriScheme);
  }

  @Implementation
  @HiddenApi
  public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() {
    return userSelectedOutgoingPhoneAccount;
  }

  @Implementation
  @HiddenApi
  public void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle accountHandle) {
    userSelectedOutgoingPhoneAccount = accountHandle;
  }

  @Implementation
  protected PhoneAccountHandle getSimCallManager() {
    return simCallManager;
  }

  @Implementation(minSdk = M)
  @HiddenApi
  public PhoneAccountHandle getSimCallManager(int userId) {
    return null;
  }

  @Implementation
  @HiddenApi
  public PhoneAccountHandle getConnectionManager() {
    return this.getSimCallManager();
  }

  @Implementation
  @HiddenApi
  public List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String uriScheme) {
    List<PhoneAccountHandle> result = new ArrayList<>();

    for (PhoneAccountHandle handle : accounts.keySet()) {
      PhoneAccount phoneAccount = accounts.get(handle);
      if (phoneAccount.getSupportedUriSchemes().contains(uriScheme)) {
        result.add(handle);
      }
    }
    return result;
  }

  @Implementation(minSdk = M)
  protected List<PhoneAccountHandle> getCallCapablePhoneAccounts() {
    return this.getCallCapablePhoneAccounts(false);
  }

  @Implementation(minSdk = M)
  @HiddenApi
  public List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean includeDisabledAccounts) {
    List<PhoneAccountHandle> result = new ArrayList<>();

    for (PhoneAccountHandle handle : accounts.keySet()) {
      PhoneAccount phoneAccount = accounts.get(handle);
      if (!phoneAccount.isEnabled() && !includeDisabledAccounts) {
        continue;
      }
      result.add(handle);
    }
    return result;
  }

  @Implementation(minSdk = O)
  public List<PhoneAccountHandle> getSelfManagedPhoneAccounts() {
    List<PhoneAccountHandle> result = new ArrayList<>();

    for (PhoneAccountHandle handle : accounts.keySet()) {
      PhoneAccount phoneAccount = accounts.get(handle);
      if ((phoneAccount.getCapabilities() & PhoneAccount.CAPABILITY_SELF_MANAGED)
          == PhoneAccount.CAPABILITY_SELF_MANAGED) {
        result.add(handle);
      }
    }
    return result;
  }

  @Implementation
  @HiddenApi
  public List<PhoneAccountHandle> getPhoneAccountsForPackage() {
    Context context = ReflectionHelpers.getField(realObject, "mContext");

    List<PhoneAccountHandle> results = new ArrayList<>();
    for (PhoneAccountHandle handle : accounts.keySet()) {
      if (handle.getComponentName().getPackageName().equals(context.getPackageName())) {
        results.add(handle);
      }
    }
    return results;
  }

  @Implementation
  protected PhoneAccount getPhoneAccount(PhoneAccountHandle account) {
    return accounts.get(account);
  }

  @Implementation
  @HiddenApi
  public int getAllPhoneAccountsCount() {
    return accounts.size();
  }

  @Implementation
  @HiddenApi
  public List<PhoneAccount> getAllPhoneAccounts() {
    return ImmutableList.copyOf(accounts.values());
  }

  @Implementation
  @HiddenApi
  public List<PhoneAccountHandle> getAllPhoneAccountHandles() {
    return ImmutableList.copyOf(accounts.keySet());
  }

  @Implementation
  protected void registerPhoneAccount(PhoneAccount account) {
    accounts.put(account.getAccountHandle(), account);
  }

  @Implementation
  protected void unregisterPhoneAccount(PhoneAccountHandle accountHandle) {
    accounts.remove(accountHandle);
  }

  /** @deprecated */
  @Deprecated
  @Implementation
  @HiddenApi
  public void clearAccounts() {
    accounts.clear();
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  @HiddenApi
  public void clearAccountsForPackage(String packageName) {
    Set<PhoneAccountHandle> phoneAccountHandlesInPackage = new HashSet<>();

    for (PhoneAccountHandle handle : accounts.keySet()) {
      if (handle.getComponentName().getPackageName().equals(packageName)) {
        phoneAccountHandlesInPackage.add(handle);
      }
    }

    for (PhoneAccountHandle handle : phoneAccountHandlesInPackage) {
      accounts.remove(handle);
    }
  }

  /** @deprecated */
  @Deprecated
  @Implementation
  @HiddenApi
  public ComponentName getDefaultPhoneApp() {
    return null;
  }

  @Implementation(minSdk = M)
  protected String getDefaultDialerPackage() {
    return defaultDialerPackageName;
  }

  /** @deprecated API deprecated since Q, for testing, use setDefaultDialerPackage instead */
  @Deprecated
  @Implementation(minSdk = M)
  @HiddenApi
  public boolean setDefaultDialer(String packageName) {
    this.defaultDialerPackageName = packageName;
    return true;
  }

  /** Set returned value of {@link #getDefaultDialerPackage()}. */
  public void setDefaultDialerPackage(String packageName) {
    this.defaultDialerPackageName = packageName;
  }

  @Implementation(minSdk = M)
  @HiddenApi // API goes public in Q
  protected String getSystemDialerPackage() {
    return systemDefaultDialerPackageName;
  }

  /** Set returned value of {@link #getSystemDialerPackage()}. */
  public void setSystemDialerPackage(String packageName) {
    this.systemDefaultDialerPackageName = packageName;
  }

  public void setVoicemailNumber(PhoneAccountHandle accountHandle, String number) {
    voicemailNumbers.put(accountHandle, number);
  }

  @Implementation(minSdk = M)
  protected boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number) {
    return TextUtils.equals(number, voicemailNumbers.get(accountHandle));
  }

  @Implementation(minSdk = M)
  protected String getVoiceMailNumber(PhoneAccountHandle accountHandle) {
    return voicemailNumbers.get(accountHandle);
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected String getLine1Number(PhoneAccountHandle accountHandle) {
    return null;
  }

  /** Sets the return value for {@link TelecomManager#isInCall}. */
  public void setIsInCall(boolean isInCall) {
    this.isInCall = isInCall;
  }

  /**
   * Overrides behavior of {@link TelecomManager#isInCall} to return pre-set result.
   *
   * @return Value set by calling {@link ShadowTelecomManager#setIsInCall}. If setIsInCall has not
   *     previously been called, will return false.
   */
  @Implementation
  protected boolean isInCall() {
    return isInCall;
  }

  @Implementation
  @HiddenApi
  public int getCallState() {
    return 0;
  }

  @Implementation
  @HiddenApi
  public boolean isRinging() {
    for (IncomingCallRecord callRecord : incomingCalls) {
      if (callRecord.isRinging) {
        return true;
      }
    }
    for (UnknownCallRecord callRecord : unknownCalls) {
      if (callRecord.isRinging) {
        return true;
      }
    }
    return false;
  }

  @Implementation
  @HiddenApi
  public boolean endCall() {
    return false;
  }

  @Implementation
  protected void acceptRingingCall() {}

  @Implementation
  protected void silenceRinger() {
    for (IncomingCallRecord callRecord : incomingCalls) {
      callRecord.isRinging = false;
    }
    for (UnknownCallRecord callRecord : unknownCalls) {
      callRecord.isRinging = false;
    }
  }

  @Implementation
  protected boolean isTtySupported() {
    return ttySupported;
  }

  /** Sets the value to be returned by {@link #isTtySupported()}. */
  public void setTtySupported(boolean isSupported) {
    ttySupported = isSupported;
  }

  @Implementation
  @HiddenApi
  public int getCurrentTtyMode() {
    return 0;
  }

  @Implementation
  protected void addNewIncomingCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    IncomingCallRecord call = new IncomingCallRecord(phoneAccount, extras);
    incomingCalls.add(call);

    switch (callRequestMode) {
      case ALLOW_ALL:
        allowIncomingCall(call);
        break;
      case DENY_ALL:
        denyIncomingCall(call);
        break;
      default:
        // Do nothing.
    }
  }

  public List<IncomingCallRecord> getAllIncomingCalls() {
    return ImmutableList.copyOf(incomingCalls);
  }

  public IncomingCallRecord getLastIncomingCall() {
    return Iterables.getLast(incomingCalls);
  }

  public IncomingCallRecord getOnlyIncomingCall() {
    return Iterables.getOnlyElement(incomingCalls);
  }

  /**
   * Allows an {@link IncomingCallRecord} created via {@link TelecomManager#addNewIncomingCall}.
   *
   * <p>Specifically, this method sets up the relevant {@link ConnectionService} and returns the
   * result of {@link ConnectionService#onCreateIncomingConnection}.
   */
  @TargetApi(M)
  @Nullable
  public Connection allowIncomingCall(IncomingCallRecord call) {
    if (call.isHandled) {
      throw new IllegalStateException("Call has already been allowed or denied.");
    }
    call.isHandled = true;

    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    ConnectionRequest request = buildConnectionRequestForIncomingCall(call);
    ConnectionService service = setupConnectionService(phoneAccount);
    return service.onCreateIncomingConnection(phoneAccount, request);
  }

  /**
   * Denies an {@link IncomingCallRecord} created via {@link TelecomManager#addNewIncomingCall}.
   *
   * <p>Specifically, this method sets up the relevant {@link ConnectionService} and calls {@link
   * ConnectionService#onCreateIncomingConnectionFailed}.
   */
  @TargetApi(O)
  public void denyIncomingCall(IncomingCallRecord call) {
    if (call.isHandled) {
      throw new IllegalStateException("Call has already been allowed or denied.");
    }
    call.isHandled = true;

    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    ConnectionRequest request = buildConnectionRequestForIncomingCall(call);
    ConnectionService service = setupConnectionService(phoneAccount);
    service.onCreateIncomingConnectionFailed(phoneAccount, request);
  }

  private static ConnectionRequest buildConnectionRequestForIncomingCall(IncomingCallRecord call) {
    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    Bundle extras = verifyNotNull(call.extras);
    Uri address = extras.getParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS);
    int videoState =
        extras.getInt(
            TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
    return new ConnectionRequest(phoneAccount, address, new Bundle(extras), videoState);
  }

  @Implementation(minSdk = M)
  protected void placeCall(Uri address, Bundle extras) {
    OutgoingCallRecord call = new OutgoingCallRecord(address, extras);
    outgoingCalls.add(call);

    switch (callRequestMode) {
      case ALLOW_ALL:
        allowOutgoingCall(call);
        break;
      case DENY_ALL:
        denyOutgoingCall(call);
        break;
      default:
        // Do nothing.
    }
  }

  public List<OutgoingCallRecord> getAllOutgoingCalls() {
    return ImmutableList.copyOf(outgoingCalls);
  }

  public OutgoingCallRecord getLastOutgoingCall() {
    return Iterables.getLast(outgoingCalls);
  }

  public OutgoingCallRecord getOnlyOutgoingCall() {
    return Iterables.getOnlyElement(outgoingCalls);
  }

  /**
   * Allows an {@link OutgoingCallRecord} created via {@link TelecomManager#placeCall}.
   *
   * <p>Specifically, this method sets up the relevant {@link ConnectionService} and returns the
   * result of {@link ConnectionService#onCreateOutgoingConnection}.
   */
  @TargetApi(M)
  @Nullable
  public Connection allowOutgoingCall(OutgoingCallRecord call) {
    if (call.isHandled) {
      throw new IllegalStateException("Call has already been allowed or denied.");
    }
    call.isHandled = true;

    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    ConnectionRequest request = buildConnectionRequestForOutgoingCall(call);
    ConnectionService service = setupConnectionService(phoneAccount);
    return service.onCreateOutgoingConnection(phoneAccount, request);
  }

  /**
   * Denies an {@link OutgoingCallRecord} created via {@link TelecomManager#placeCall}.
   *
   * <p>Specifically, this method sets up the relevant {@link ConnectionService} and calls {@link
   * ConnectionService#onCreateOutgoingConnectionFailed}.
   */
  @TargetApi(O)
  public void denyOutgoingCall(OutgoingCallRecord call) {
    if (call.isHandled) {
      throw new IllegalStateException("Call has already been allowed or denied.");
    }
    call.isHandled = true;

    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    ConnectionRequest request = buildConnectionRequestForOutgoingCall(call);
    ConnectionService service = setupConnectionService(phoneAccount);
    service.onCreateOutgoingConnectionFailed(phoneAccount, request);
  }

  private static ConnectionRequest buildConnectionRequestForOutgoingCall(OutgoingCallRecord call) {
    PhoneAccountHandle phoneAccount = verifyNotNull(call.phoneAccount);
    Uri address = verifyNotNull(call.address);
    Bundle extras = verifyNotNull(call.extras);
    Bundle outgoingCallExtras = extras.getBundle(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS);
    int videoState =
        extras.getInt(
            TelecomManager.EXTRA_START_CALL_WITH_VIDEO_STATE, VideoProfile.STATE_AUDIO_ONLY);
    return new ConnectionRequest(
        phoneAccount,
        address,
        outgoingCallExtras == null ? null : new Bundle(outgoingCallExtras),
        videoState);
  }

  @Implementation
  @HiddenApi
  public void addNewUnknownCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    unknownCalls.add(new UnknownCallRecord(phoneAccount, extras));
  }

  public List<UnknownCallRecord> getAllUnknownCalls() {
    return ImmutableList.copyOf(unknownCalls);
  }

  public UnknownCallRecord getLastUnknownCall() {
    return Iterables.getLast(unknownCalls);
  }

  public UnknownCallRecord getOnlyUnknownCall() {
    return Iterables.getOnlyElement(unknownCalls);
  }

  private static ConnectionService setupConnectionService(PhoneAccountHandle phoneAccount) {
    ComponentName service = phoneAccount.getComponentName();
    Class<? extends ConnectionService> clazz;
    try {
      clazz = Class.forName(service.getClassName()).asSubclass(ConnectionService.class);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    return verifyNotNull(
        ServiceController.of(ReflectionHelpers.callConstructor(clazz), null).create().get());
  }

  @Implementation
  protected boolean handleMmi(String dialString) {
    return false;
  }

  @Implementation(minSdk = M)
  protected boolean handleMmi(String dialString, PhoneAccountHandle accountHandle) {
    return false;
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle) {
    return Uri.parse("content://icc/adn");
  }

  @Implementation
  protected void cancelMissedCallsNotification() {}

  @Implementation
  protected void showInCallScreen(boolean showDialpad) {}

  @Implementation(minSdk = M)
  @HiddenApi
  public void enablePhoneAccount(PhoneAccountHandle handle, boolean isEnabled) {
  }

  /**
   * Returns the intent set by {@link ShadowTelecomManager#setManageBlockNumbersIntent(Intent)} ()}
   */
  @Implementation(minSdk = N)
  protected Intent createManageBlockedNumbersIntent() {
    return this.manageBlockNumbersIntent;
  }

  /**
   * Sets the BlockNumbersIntent to be returned by {@link
   * ShadowTelecomManager#createManageBlockedNumbersIntent()}
   */
  public void setManageBlockNumbersIntent(Intent intent) {
    this.manageBlockNumbersIntent = intent;
  }

  @Implementation(maxSdk = LOLLIPOP_MR1)
  public void setSimCallManager(PhoneAccountHandle simCallManager) {
    this.simCallManager = simCallManager;
  }

  /**
   * Creates a new {@link CallAudioState}. The real constructor of {@link CallAudioState} is hidden.
   */
  public CallAudioState newCallAudioState(
      boolean muted,
      int route,
      int supportedRouteMask,
      BluetoothDevice activeBluetoothDevice,
      Collection<BluetoothDevice> supportedBluetoothDevices) {
    return new CallAudioState(
        muted, route, supportedRouteMask, activeBluetoothDevice, supportedBluetoothDevices);
  }

  @Implementation(minSdk = R)
  @SystemApi
  protected Intent createLaunchEmergencyDialerIntent(String number) {
    // copy of logic from TelecomManager service
    Context context = ReflectionHelpers.getField(realObject, "mContext");
    // use reflection to get resource id since it can vary based on SDK version, and compiler will
    // inline the value if used explicitly
    int configEmergencyDialerPackageId =
        ReflectionHelpers.getStaticField(
            com.android.internal.R.string.class, "config_emergency_dialer_package");
    String packageName = context.getString(configEmergencyDialerPackageId);
    Intent intent = new Intent(Intent.ACTION_DIAL_EMERGENCY).setPackage(packageName);
    ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
    if (resolveInfo == null) {
      // No matching activity from config, fallback to default platform implementation
      intent.setPackage(null);
    }
    if (!TextUtils.isEmpty(number) && TextUtils.isDigitsOnly(number)) {
      intent.setData(Uri.parse("tel:" + number));
    }
    return intent;
  }

  /**
   * Details about a call request made via {@link TelecomManager#addNewIncomingCall} or {@link
   * TelecomManager#addNewUnknownCall}.
   *
   * @deprecated Use {@link IncomingCallRecord} or {@link UnknownCallRecord} instead.
   */
  @Deprecated
  public static class CallRecord {
    public final PhoneAccountHandle phoneAccount;
    public final Bundle extras;
    protected boolean isRinging = true;

    /** @deprecated Use {@link extras} instead. */
    @Deprecated public final Bundle bundle;

    public CallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      this.phoneAccount = phoneAccount;
      this.extras = extras == null ? null : new Bundle(extras);

      // Keep the deprecated "bundle" name around for a while.
      this.bundle = this.extras;
    }
  }

  /** Details about an incoming call request made via {@link TelecomManager#addNewIncomingCall}. */
  public static class IncomingCallRecord extends CallRecord {
    private boolean isHandled = false;

    public IncomingCallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      super(phoneAccount, extras);
    }
  }

  /** Details about an outgoing call request made via {@link TelecomManager#placeCall}. */
  public static class OutgoingCallRecord {
    public final PhoneAccountHandle phoneAccount;
    public final Uri address;
    public final Bundle extras;

    private boolean isHandled = false;

    public OutgoingCallRecord(Uri address, Bundle extras) {
      this.address = address;
      if (extras != null) {
        this.extras = new Bundle(extras);
        this.phoneAccount = extras.getParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE);
      } else {
        this.extras = null;
        this.phoneAccount = null;
      }
    }
  }

  /** Details about an unknown call request made via {@link TelecomManager#addNewUnknownCall}. */
  public static class UnknownCallRecord extends CallRecord {
    public UnknownCallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      super(phoneAccount, extras);
    }
  }
}
