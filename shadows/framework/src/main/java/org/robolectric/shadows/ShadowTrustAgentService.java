package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.service.trust.GrantTrustResult;
import android.service.trust.TrustAgentService;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link TrustAgentService}. */
@Implements(value = TrustAgentService.class, minSdk = LOLLIPOP)
@SuppressWarnings("JavaApiBestPractices")
public class ShadowTrustAgentService extends ShadowService {
  /** Types of methods that can be invoked on {@link TrustAgentService}. */
  public enum CallType {
    SET_MANAGING_TRUST,
    GRANT_TRUST,
    REVOKE_TRUST,
    LOCK_USER,
    SHOW_KEYGUARD_ERROR_MESSAGE
  }

  /** Details of a call to {@link TrustAgentService}. */
  public static class TrustAgentCall {
    public final CallType type;
    public final Object[] args;

    public TrustAgentCall(CallType type, Object... args) {
      this.type = type;
      this.args = args;
    }
  }

  private boolean managingTrust = false;
  private final List<GrantTrustCall> grantTrustCalls = new ArrayList<>();
  private int revokeTrustCallCount = 0;
  private boolean lockUserCalled = false;
  private final List<CharSequence> keyguardErrorMessages = new ArrayList<>();
  private final List<TrustAgentCall> calls = new ArrayList<>();

  /** Details of a call to grantTrust. */
  public static class GrantTrustCall {
    public final CharSequence message;
    public final long durationMs;
    public final int flags;
    public final Consumer<GrantTrustResult> callback;

    public GrantTrustCall(
        CharSequence message, long durationMs, int flags, Consumer<GrantTrustResult> callback) {
      this.message = message;
      this.durationMs = durationMs;
      this.flags = flags;
      this.callback = callback;
    }
  }

  @Implementation
  protected void setManagingTrust(boolean managingTrust) {
    this.managingTrust = managingTrust;
    calls.add(new TrustAgentCall(CallType.SET_MANAGING_TRUST, managingTrust));
  }

  @Implementation
  protected void grantTrust(CharSequence message, long durationMs, int flags) {
    grantTrustCalls.add(new GrantTrustCall(message, durationMs, flags, null));
    calls.add(new TrustAgentCall(CallType.GRANT_TRUST, message, durationMs, flags, null));
  }

  @Implementation(minSdk = TIRAMISU)
  protected void grantTrust(
      CharSequence message, long durationMs, int flags, Consumer<GrantTrustResult> callback) {
    grantTrustCalls.add(new GrantTrustCall(message, durationMs, flags, callback));
    calls.add(new TrustAgentCall(CallType.GRANT_TRUST, message, durationMs, flags, callback));
  }

  @Implementation
  protected void revokeTrust() {
    revokeTrustCallCount++;
    calls.add(new TrustAgentCall(CallType.REVOKE_TRUST));
  }

  @Implementation(minSdk = TIRAMISU)
  protected void lockUser() {
    lockUserCalled = true;
    calls.add(new TrustAgentCall(CallType.LOCK_USER));
  }

  @Implementation(minSdk = P)
  protected void showKeyguardErrorMessage(CharSequence message) {
    keyguardErrorMessages.add(message);
    calls.add(new TrustAgentCall(CallType.SHOW_KEYGUARD_ERROR_MESSAGE, message));
  }

  public boolean isManagingTrust() {
    return managingTrust;
  }

  public List<GrantTrustCall> getGrantTrustCalls() {
    return new ArrayList<>(grantTrustCalls);
  }

  public int getRevokeTrustCallCount() {
    return revokeTrustCallCount;
  }

  public boolean isLockUserCalled() {
    return lockUserCalled;
  }

  public boolean isLockedUser() {
    return lockUserCalled;
  }

  public List<CharSequence> getKeyguardErrorMessages() {
    return new ArrayList<>(keyguardErrorMessages);
  }

  public CharSequence getLatestKeyguardErrorMessage() {
    return keyguardErrorMessages.isEmpty()
        ? null
        : keyguardErrorMessages.get(keyguardErrorMessages.size() - 1);
  }

  public List<TrustAgentCall> getCalls() {
    return new ArrayList<>(calls);
  }
}
