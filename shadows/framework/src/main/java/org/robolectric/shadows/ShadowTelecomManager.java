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
  public PhoneAccountHandle getDefaultOutgoingPhoneAccount(String uriScheme) {
    return null;
  }

  @Implementation
  public PhoneAccountHandle getUserSelectedOutgoingPhoneAccount() {
    return null;
  }

  @Implementation
  public void setUserSelectedOutgoingPhoneAccount(PhoneAccountHandle accountHandle) {
  }

  @Implementation
  public PhoneAccountHandle getSimCallManager() {
    return simCallManager;
  }

  @Implementation
  public PhoneAccountHandle getSimCallManager(int userId) {
    return null;
  }

  @Implementation
  public PhoneAccountHandle getConnectionManager() {
    return this.getSimCallManager();
  }

  @Implementation
  public List<PhoneAccountHandle> getPhoneAccountsSupportingScheme(String uriScheme) {
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
  public List<PhoneAccountHandle> getCallCapablePhoneAccounts() {
    return this.getCallCapablePhoneAccounts(false);
  }

  @Implementation(minSdk = M)
  public List<PhoneAccountHandle> getCallCapablePhoneAccounts(boolean includeDisabledAccounts) {
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
  public List<PhoneAccountHandle> getPhoneAccountsForPackage() {
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
  public PhoneAccount getPhoneAccount(PhoneAccountHandle account) {
    return accounts.get(account);
  }

  @Implementation
  public int getAllPhoneAccountsCount() {
    return accounts.size();
  }

  @Implementation
  public List<PhoneAccount> getAllPhoneAccounts() {
    return ImmutableList.copyOf(accounts.values());
  }

  @Implementation
  public List<PhoneAccountHandle> getAllPhoneAccountHandles() {
    return ImmutableList.copyOf(accounts.keySet());
  }

  @Implementation
  public void registerPhoneAccount(PhoneAccount account) {
    accounts.put(account.getAccountHandle(), account);
  }

  @Implementation
  public void unregisterPhoneAccount(PhoneAccountHandle accountHandle) {
    accounts.remove(accountHandle);
  }

  /** @deprecated */
  @Deprecated
  @Implementation
  public void clearAccounts() {
    accounts.clear();
  }


  @Implementation(minSdk = LOLLIPOP_MR1)
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
  public ComponentName getDefaultPhoneApp() {
    return null;
  }

  @Implementation(minSdk = M)
  public String getDefaultDialerPackage() {
    return defaultDialerPackageName;
  }

  @Implementation(minSdk = M)
  public boolean setDefaultDialer(String packageName) {
    this.defaultDialerPackageName = packageName;
    return true;
  }

  @Implementation
  public String getSystemDialerPackage() {
    return null;
  }

  @Implementation
  public boolean isVoiceMailNumber(PhoneAccountHandle accountHandle, String number) {
    return false;
  }

  @Implementation
  public String getVoiceMailNumber(PhoneAccountHandle accountHandle) {
    return null;
  }

  @Implementation
  public String getLine1Number(PhoneAccountHandle accountHandle) {
    return null;
  }

  @Implementation
  public boolean isInCall() {
    return false;
  }

  @Implementation
  public int getCallState() {
    return 0;
  }

  @Implementation
  public boolean isRinging() {
    return false;
  }

  @Implementation
  public boolean endCall() {
    return false;
  }

  @Implementation
  public void acceptRingingCall() {
  }

  @Implementation
  public void silenceRinger() {
  }

  @Implementation
  public boolean isTtySupported() {
    return false;
  }

  @Implementation
  public int getCurrentTtyMode() {
    return 0;
  }

  @Implementation
  public void addNewIncomingCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    incomingCalls.add(new CallRecord(phoneAccount, extras));
  }

  public List<CallRecord> getAllIncomingCalls() {
    return incomingCalls;
  }

  @Implementation
  public void addNewUnknownCall(PhoneAccountHandle phoneAccount, Bundle extras) {
    unknownCalls.add(new CallRecord(phoneAccount, extras));
  }

  public List<CallRecord> getAllUnknownCalls() {
    return unknownCalls;
  }

  @Implementation
  public boolean handleMmi(String dialString) {
    return false;
  }

  @Implementation
  public boolean handleMmi(String dialString, PhoneAccountHandle accountHandle) {
    return false;
  }

  @Implementation
  public Uri getAdnUriForPhoneAccount(PhoneAccountHandle accountHandle) {
    return Uri.parse("content://icc/adn");
  }

  @Implementation
  public void cancelMissedCallsNotification() {
  }

  @Implementation
  public void showInCallScreen(boolean showDialpad) {
  }

  @Implementation
  public void placeCall(Uri address, Bundle extras) {
  }

  @Implementation
  public void enablePhoneAccount(PhoneAccountHandle handle, boolean isEnabled) {
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