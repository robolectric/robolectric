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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = TelecomManager.class, minSdk = LOLLIPOP)
public class ShadowTelecomManager {

  @RealObject
  private TelecomManager realObject;

  private PhoneAccountHandle simCallManager;
  private LinkedHashMap<PhoneAccountHandle, PhoneAccount> accounts = new LinkedHashMap();
  private List<CallRecord> incomingCalls = new LinkedList<>();
  private List<CallRecord> unknownCalls = new LinkedList<>();
  private String defaultDialerPackageName;

  @Implementation
  protected PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme) {
    return null;
  }

  @Implementation
  protected PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() {
    return null;
  }

  @Implementation
  protected void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle accountHandle) {
  }

  @Implementation
  protected PhoneAccountHandle getSimCallManager() {
    return simCallManager;
  }

  @Implementation
  protected PhoneAccountHandle getSimCallManager(int userId) {
    return null;
  }

  @Implementation
  protected PhoneAccountHandle getConnectionManager() {
    return this.getSimCallManager();
  }

  @Implementation
  protected List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String uriScheme) {
    List<PhoneAccountHandle> result = new LinkedList<>();

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
  protected List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean includeDisabledAccounts) {
    List<PhoneAccountHandle> result = new LinkedList<>();

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
  protected List<PhoneAccountHandle> getPhoneAccountsForPackage() {
    Context context = ReflectionHelpers.getField(realObject, "mContext");

    List<PhoneAccountHandle> results = new LinkedList<>();
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
  protected int getAllPhoneAccountsCount() {
    return accounts.size();
  }

  @Implementation
  protected List<PhoneAccount> getAllPhoneAccounts() {
    return ImmutableList.copyOf(accounts.values());
  }

  @Implementation
  protected List<PhoneAccountHandle> getAllPhoneAccountHandles() {
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
  protected void clearAccounts() {
    accounts.clear();
  }


  @Implementation(minSdk = LOLLIPOP_MR1)
  protected void clearAccountsForPackage(String packageName) {
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
  protected ComponentName getDefaultPhoneApp() {
    return null;
  }

  @Implementation(minSdk = M)
  protected String getDefaultDialerPackage() {
    return defaultDialerPackageName;
  }

  @Implementation(minSdk = M)
  protected boolean setDefaultDialer(String packageName) {
    this.defaultDialerPackageName = packageName;
    return true;
  }

  @Implementation
  protected String getSystemDialerPackage() {
    return null;
  }

  @Implementation
  protected boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number) {
    return false;
  }

  @Implementation
  protected String getVoiceMailNumber(PhoneAccountHandle accountHandle) {
    return null;
  }

  @Implementation
  protected String getLine1Number(PhoneAccountHandle accountHandle) {
    return null;
  }

  @Implementation
  protected boolean isInCall() {
    return false;
  }

  @Implementation
  protected int getCallState() {
    return 0;
  }

  @Implementation
  protected boolean isRinging() {
    return false;
  }

  @Implementation
  protected boolean endCall() {
    return false;
  }

  @Implementation
  protected void acceptRingingCall() {
  }

  @Implementation
  protected void silenceRinger() {
  }

  @Implementation
  protected boolean isTtySupported() {
    return false;
  }

  @Implementation
  protected int getCurrentTtyMode() {
    return 0;
  }

  @Implementation
  protected void addNewIncomingCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    incomingCalls.add(new CallRecord(phoneAccount, extras));
  }

  public List<CallRecord> getAllIncomingCalls() {
    return incomingCalls;
  }

  @Implementation
  protected void addNewUnknownCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    unknownCalls.add(new CallRecord(phoneAccount, extras));
  }

  public List<CallRecord> getAllUnknownCalls() {
    return unknownCalls;
  }

  @Implementation
  protected boolean handleMmi(String dialString) {
    return false;
  }

  @Implementation
  protected boolean handleMmi(String dialString, PhoneAccountHandle accountHandle) {
    return false;
  }

  @Implementation
  protected Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle) {
    return Uri.parse("content://icc/adn");
  }

  @Implementation
  protected void cancelMissedCallsNotification() {
  }

  @Implementation
  protected void showInCallScreen(boolean showDialpad) {
  }

  @Implementation
  protected void placeCall(Uri address, Bundle extras) {
  }

  @Implementation
  protected void enablePhoneAccount(PhoneAccountHandle handle, boolean isEnabled) {
  }

  public void setSimCallManager(PhoneAccountHandle simCallManager) {
    this.simCallManager = simCallManager;
  }

  public static class CallRecord {
    public final PhoneAccountHandle phoneAccount;
    public final Bundle bundle;

    public CallRecord(PhoneAccountHandle phoneAccount, Bundle extras) {
      this.phoneAccount = phoneAccount;
      this.bundle = extras;
    }
  }

}