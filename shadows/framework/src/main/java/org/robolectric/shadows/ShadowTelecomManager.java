package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = TelecomManager.class, minSdk = LOLLIPOP)
public class ShadowTelecomManager {

  @RealObject
  private TelecomManager realObject;

  private final LinkedHashMap<PhoneAccountHandle, PhoneAccount> accounts = new LinkedHashMap<>();
  private final List<IncomingCallRecord> incomingCalls = new ArrayList<>();
  private final List<OutgoingCallRecord> outgoingCalls = new ArrayList<>();
  private final List<UnknownCallRecord> unknownCalls = new ArrayList<>();

  private PhoneAccountHandle simCallManager;
  private String defaultDialerPackageName;
  private String systemDefaultDialerPackageName;
  private boolean isInCall;

  @Implementation
  protected PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme) {
    return null;
  }

  @Implementation
  @HiddenApi
  public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() {
    return null;
  }

  @Implementation
  @HiddenApi
  public void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle accountHandle) {
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
      if(phoneAccount.getSupportedUriSchemes().contains(uriScheme)) {
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
      if(!phoneAccount.isEnabled() && !includeDisabledAccounts) {
        continue;
      }
      result.add(handle);
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

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number) {
    return false;
  }

  @Implementation(minSdk = M)
  protected String getVoiceMailNumber(PhoneAccountHandle accountHandle) {
    return null;
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
    return false;
  }

  @Implementation
  @HiddenApi
  public int getCurrentTtyMode() {
    return 0;
  }

  @Implementation
  protected void addNewIncomingCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    incomingCalls.add(new IncomingCallRecord(phoneAccount, extras));
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

  @Implementation(minSdk = M)
  protected void placeCall(Uri address, Bundle extras) {
    outgoingCalls.add(new OutgoingCallRecord(address, extras));
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

  public void setSimCallManager(PhoneAccountHandle simCallManager) {
    this.simCallManager = simCallManager;
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
    public IncomingCallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      super(phoneAccount, extras);
    }
  }

  /** Details about an outgoing call request made via {@link TelecomManager#placeCall}. */
  public static class OutgoingCallRecord {
    public final Uri address;
    public final Bundle extras;

    public OutgoingCallRecord(Uri address, Bundle extras) {
      this.address = address;
      this.extras = extras == null ? null : new Bundle(extras);
    }
  }

  /** Details about an unknown call request made via {@link TelecomManager#addNewUnknownCall}. */
  public static class UnknownCallRecord extends CallRecord {
    public UnknownCallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      super(phoneAccount, extras);
    }
  }
}
