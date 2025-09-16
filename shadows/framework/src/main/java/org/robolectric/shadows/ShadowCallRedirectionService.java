package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.telecom.CallRedirectionService;
import android.telecom.PhoneAccountHandle;
import com.android.internal.telecom.ICallRedirectionAdapter;
import com.android.internal.telecom.ICallRedirectionService;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Robolectric shadow to support {@link CallRedirectionService}. */
@Implements(value = CallRedirectionService.class, minSdk = Q)
public class ShadowCallRedirectionService {
  @RealObject CallRedirectionService callRedirectionService;
  @ReflectorObject protected CallRedirectionServiceReflector callRedirectionServiceReflector;

  private static Optional<RedirectionResult> lastRedirectionResult = Optional.empty();

  /**
   * Place a call to be processed by the {@link CallRedirectionService}. This will trigger the
   * {@link CallRedirectionService#onPlaceCall} method with the provided arguments.
   */
  public void placeCall(
      Uri uri, PhoneAccountHandle phoneAccountHandle, boolean allowInteractiveResponse) {
    ICallRedirectionService binder =
        (ICallRedirectionService) callRedirectionService.onBind(new Intent());
    ICallRedirectionServiceReflector binderReflector =
        reflector(ICallRedirectionServiceReflector.class, binder);
    try {
      if (getApiLevel() <= BAKLAVA) {
        binderReflector.placeCall(
            ReflectionHelpers.createNullProxy(ICallRedirectionAdapter.class),
            uri,
            phoneAccountHandle,
            allowInteractiveResponse);
      } else {
        binderReflector.placeCall(
            ReflectionHelpers.createNullProxy(ICallRedirectionAdapter.class),
            uri,
            uri,
            phoneAccountHandle,
            allowInteractiveResponse);
      }
    } catch (RemoteException e) {
      throw new AssertionError(e);
    }
  }

  /** Returns the last captured {@link RedirectionResult}. */
  public Optional<RedirectionResult> getRedirectionResult() {
    return lastRedirectionResult;
  }

  private void setRedirectionResult(RedirectionResult redirectionResult) {
    lastRedirectionResult = Optional.of(redirectionResult);
  }

  @Resetter
  public static void reset() {
    lastRedirectionResult = Optional.empty();
  }

  @Implementation
  protected void redirectCall(
      Uri handle, PhoneAccountHandle initialPhoneAccount, boolean isInteractiveResponseAllowed) {
    setRedirectionResult(
        new RedirectionResult(
            new RedirectionResult.RedirectCallArgs(
                handle, initialPhoneAccount, isInteractiveResponseAllowed)));
    callRedirectionServiceReflector.redirectCall(
        handle, initialPhoneAccount, isInteractiveResponseAllowed);
  }

  @Implementation
  protected void placeCallUnmodified() {
    setRedirectionResult(
        new RedirectionResult(RedirectionResult.RedirectionResultType.PLACE_CALL_UNMODIFIED));
    callRedirectionServiceReflector.placeCallUnmodified();
  }

  @Implementation
  protected void cancelCall() {
    setRedirectionResult(
        new RedirectionResult(RedirectionResult.RedirectionResultType.CANCEL_CALL));
    callRedirectionServiceReflector.cancelCall();
  }

  @ForType(CallRedirectionService.class)
  private interface CallRedirectionServiceReflector {
    @Direct
    void redirectCall(
        Uri handle, PhoneAccountHandle initialPhoneAccount, boolean isInteractiveResponseAllowed);

    @Direct
    void placeCallUnmodified();

    @Direct
    void cancelCall();
  }

  @ForType(ICallRedirectionService.class)
  private interface ICallRedirectionServiceReflector {

    // For <= BAKLAVA
    void placeCall(
        ICallRedirectionAdapter nullProxy,
        Uri uri,
        PhoneAccountHandle phoneAccountHandle,
        boolean allowInteractiveResponse)
        throws RemoteException;

    // For > BAKLAVA
    void placeCall(
        ICallRedirectionAdapter nullProxy,
        Uri uri,
        Uri uri2,
        PhoneAccountHandle phoneAccountHandle,
        boolean allowInteractiveResponse)
        throws RemoteException;
  }

  /** The result of the redirection attempt. */
  public static class RedirectionResult {
    private final RedirectionResultType redirectionResultType;
    private final Optional<RedirectCallArgs> redirectCallArgsOptional;

    private RedirectionResult(RedirectionResultType redirectionResultType) {
      this.redirectionResultType = redirectionResultType;
      this.redirectCallArgsOptional = Optional.empty();
    }

    private RedirectionResult(RedirectCallArgs redirectCallArgs) {
      this.redirectionResultType = RedirectionResultType.REDIRECT_CALL;
      this.redirectCallArgsOptional = Optional.of(redirectCallArgs);
    }

    public RedirectionResultType getRedirectionResultType() {
      return redirectionResultType;
    }

    public Optional<RedirectCallArgs> getRedirectCallArgs() {
      return redirectCallArgsOptional;
    }

    /** The type of the redirection result. */
    public enum RedirectionResultType {
      /** The call is placed unmodified. */
      PLACE_CALL_UNMODIFIED,
      /** The call is cancelled. */
      CANCEL_CALL,
      /** The call is requested to be redirected. */
      REDIRECT_CALL,
    }

    /** The captured arguments for a call that is requested to be redirected. */
    public static class RedirectCallArgs {
      private final Uri handle;
      private final PhoneAccountHandle targetPhoneAccount;
      private final boolean confirmFirst;

      private RedirectCallArgs(
          Uri handle, PhoneAccountHandle targetPhoneAccount, boolean confirmFirst) {
        this.handle = handle;
        this.targetPhoneAccount = targetPhoneAccount;
        this.confirmFirst = confirmFirst;
      }

      public Uri getHandle() {
        return handle;
      }

      public PhoneAccountHandle getTargetPhoneAccount() {
        return targetPhoneAccount;
      }

      public boolean getConfirmFirst() {
        return confirmFirst;
      }
    }
  }
}
