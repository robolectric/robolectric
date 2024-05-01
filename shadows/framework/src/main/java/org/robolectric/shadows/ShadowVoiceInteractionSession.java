package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.callConstructor;

import android.app.Dialog;
import android.app.VoiceInteractor;
import android.app.VoiceInteractor.PickOptionRequest.Option;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSession.CommandRequest;
import android.service.voice.VoiceInteractionSession.Request;
import com.android.internal.app.IVoiceInteractorCallback;
import com.android.internal.app.IVoiceInteractorRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow implementation of {@link android.service.voice.VoiceInteractionSession}. */
@Implements(value = VoiceInteractionSession.class)
public class ShadowVoiceInteractionSession {

  private final List<Intent> assistantActivityIntents = new ArrayList<>();
  private final List<Intent> voiceActivityIntents = new ArrayList<>();

  private boolean isFinishing;
  @Nullable private RuntimeException startVoiceActivityException;
  @Nullable private RuntimeException startAssistantActivityException;
  @RealObject private VoiceInteractionSession realSession;

  /**
   * Simulates the creation of the {@link VoiceInteractionSession}, as if it was being created by
   * the framework.
   *
   * <p>This method must be called before state changing methods of {@link VoiceInteractionSession}.
   */
  public void create() {
    try {
      Class<?> serviceClass =
          Class.forName("com.android.internal.app.IVoiceInteractionManagerService");
      Object service =
          ReflectionHelpers.createDelegatingProxy(
              serviceClass, new FakeVoiceInteractionManagerService());

      Binder token = new Binder();

      ReflectionHelpers.callInstanceMethod(
          realSession,
          "doCreate",
          ClassParameter.from(serviceClass, service),
          ClassParameter.from(IBinder.class, token));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the last {@link Intent} passed into {@link
   * VoiceInteractionSession#startAssistantActivity(Intent)} or {@code null} if there wasn't any.
   */
  @Nullable
  public Intent getLastAssistantActivityIntent() {
    return Iterables.getLast(assistantActivityIntents, /* defaultValue= */ null);
  }

  /**
   * Returns the list of {@link Intent} instances passed into {@link
   * VoiceInteractionSession#startAssistantActivity(Intent)} in invocation order.
   */
  public ImmutableList<Intent> getAssistantActivityIntents() {
    return ImmutableList.copyOf(assistantActivityIntents);
  }

  /**
   * Returns the last {@link Intent} passed into {@link
   * VoiceInteractionSession#startVoiceActivity(Intent)} or {@code null} if there wasn't any.
   */
  @Nullable
  public Intent getLastVoiceActivityIntent() {
    return Iterables.getLast(voiceActivityIntents, /* defaultValue= */ null);
  }

  /**
   * Returns the list of {@link Intent} instances passed into {@link
   * VoiceInteractionSession#startVoiceActivity(Intent)} in invocation order.
   */
  public ImmutableList<Intent> getVoiceActivityIntents() {
    return ImmutableList.copyOf(voiceActivityIntents);
  }

  /**
   * Returns whether the window from {@link VoiceInteractionSession} is currently visible. Although
   * window is visible this method does not check whether UI content of window is also showed.
   */
  public boolean isWindowVisible() {
    return ReflectionHelpers.getField(realSession, "mWindowVisible");
  }

  /** Returns whether the UI window from {@link VoiceInteractionSession} is currently showing. */
  public boolean isWindowShowing() {
    Dialog window = ReflectionHelpers.getField(realSession, "mWindow");
    return isWindowVisible() && window != null && window.isShowing();
  }

  /**
   * Returns whether the UI is set to be enabled through {@link
   * VoiceInteractionSession#setUiEnabled(boolean)}.
   */
  public boolean isUiEnabled() {
    return ReflectionHelpers.getField(realSession, "mUiEnabled");
  }

  /**
   * Returns whether the {@link VoiceInteractionSession} is in the process of being destroyed and
   * finishing.
   */
  public boolean isFinishing() {
    return isFinishing;
  }

  /**
   * Sets a {@link RuntimeException} that should be thrown when {@link
   * VoiceInteractionSession#startVoiceActivity(Intent)} is invoked.
   *
   * @see <a
   *     href="https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/Instrumentation.java;drc=5f8f8ab44b4ac659804a13fb75b6516f86f977e7;l=2225">Instrumentation.checkStartActivityResult</a>
   *     for possible runtime exceptions that can be thrown.
   */
  public void setStartVoiceActivityException(RuntimeException exception) {
    startVoiceActivityException = exception;
  }

  /**
   * Sets a {@link RuntimeException} that should be thrown when {@link
   * VoiceInteractionSession#startAssistantActivity(Intent)} is invoked.
   *
   * @see <a
   *     href="https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/app/Instrumentation.java;drc=5f8f8ab44b4ac659804a13fb75b6516f86f977e7;l=2225">Instrumentation.checkStartActivityResult</a>
   *     for possible runtime exceptions that can be thrown.
   */
  public void setStartAssistantActivityException(RuntimeException exception) {
    startAssistantActivityException = exception;
  }

  /**
   * Simulates the creation of the {@link VoiceInteractionSession.CommandRequest} related to the
   * provided {@link VoiceInteractor.CommandRequest}, as if it was being created by the framework.
   * The method calls {@link VoiceInteractionSession#onRequestCommand(CommandRequest)} with newly
   * created {@link VoiceInteractionSession.CommandRequest}.
   *
   * @param commandRequest: Command request sent by a third-party application.
   * @param packageName: Package name of the application that initiated the request.
   * @param uid: User ID of the application that initiated the request.
   * @return newly created {@link VoiceInteractionSession.CommandRequest}
   */
  public CommandRequest sendCommandRequest(
      @Nonnull VoiceInteractor.CommandRequest commandRequest,
      @Nonnull String packageName,
      int uid) {
    String command = ReflectionHelpers.getField(commandRequest, "mCommand");
    Bundle extras = ReflectionHelpers.getField(commandRequest, "mArgs");

    IVoiceInteractorCallback callback = new ShadowVoiceInteractorCallback(commandRequest);

    CommandRequest internalCommandRequest =
        createCommandRequest(packageName, uid, callback, command, extras);
    realSession.onRequestCommand(internalCommandRequest);
    return internalCommandRequest;
  }

  /**
   * Creates the {@link VoiceInteractionSession.CommandRequest}.
   *
   * @param packageName: Package name of the application that initiated the request.
   * @param uid: User ID of the application that initiated the request.
   * @param callback: IVoiceInteractorCallback.
   * @param command: RequestCommand command.
   * @param extras: Additional extra information that was supplied as part of the request.
   * @return created {@link VoiceInteractionSession.CommandRequest}.
   */
  private CommandRequest createCommandRequest(
      @Nonnull String packageName,
      int uid,
      @Nonnull IVoiceInteractorCallback callback,
      @Nonnull String command,
      @Nonnull Bundle extras) {
    CommandRequest commandRequest =
        callConstructor(
            CommandRequest.class,
            ClassParameter.from(String.class, packageName),
            ClassParameter.from(int.class, uid),
            ClassParameter.from(IVoiceInteractorCallback.class, callback),
            ClassParameter.from(VoiceInteractionSession.class, realSession),
            ClassParameter.from(String.class, command),
            ClassParameter.from(Bundle.class, extras));
    ReflectionHelpers.callInstanceMethod(
        realSession, "addRequest", ClassParameter.from(Request.class, commandRequest));
    return commandRequest;
  }

  // Extends com.android.internal.app.IVoiceInteractionManagerService.Stub
  private class FakeVoiceInteractionManagerService {

    // Removed in Android U
    // @Override
    public boolean showSessionFromSession(IBinder token, Bundle args, int flags) {
      return showSessionFromSessionImpl(args, flags);
    }

    // Added in Android U
    // @Override
    public boolean showSessionFromSession(
        IBinder token, Bundle args, int flags, String attributionTag) {
      return showSessionFromSessionImpl(args, flags);
    }

    private boolean showSessionFromSessionImpl(Bundle args, int flags) {
      try {
        Class<?> callbackClass =
            Class.forName("com.android.internal.app.IVoiceInteractionSessionShowCallback");
        Object callback = ReflectionHelpers.createDeepProxy(callbackClass);

        ReflectionHelpers.callInstanceMethod(
            realSession,
            "doShow",
            ClassParameter.from(Bundle.class, args),
            ClassParameter.from(int.class, flags),
            ClassParameter.from(callbackClass, callback));
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      return true;
    }

    // @Override
    public boolean hideSessionFromSession(IBinder token) {
      ReflectionHelpers.callInstanceMethod(realSession, "doHide");
      return true;
    }

    // Removed in Android U
    // @Override
    public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) {
      return startVoiceActivityImpl(intent);
    }

    // Added in Android U
    // @Override
    public int startVoiceActivity(
        IBinder token, Intent intent, String resolvedType, String callingFeatureId) {
      return startVoiceActivityImpl(intent);
    }

    private int startVoiceActivityImpl(Intent intent) {
      RuntimeException exception = startVoiceActivityException;
      if (exception != null) {
        throw exception;
      }
      voiceActivityIntents.add(intent);
      return 0;
    }

    // Removed in Android R
    // @Override
    public int startAssistantActivity(IBinder token, Intent intent, String resolvedType) {
      return startAssistantActivityImpl(intent);
    }

    // Added in Android R
    // Removed in Android U
    // @Override
    public int startAssistantActivity(
        IBinder token, Intent intent, String resolvedType, String callingFeatureId) {
      return startAssistantActivityImpl(intent);
    }

    // Added in Android U
    // @Override
    public int startAssistantActivity(
        IBinder token, Intent intent, String resolvedType, String callingFeatureId, Bundle bundle) {
      return startAssistantActivityImpl(intent);
    }

    private int startAssistantActivityImpl(Intent intent) {
      RuntimeException exception = startAssistantActivityException;
      if (exception != null) {
        throw exception;
      }
      assistantActivityIntents.add(intent);
      return 0;
    }

    // @Override
    public void finish(IBinder token) {
      ReflectionHelpers.callInstanceMethod(realSession, "doDestroy");
      isFinishing = true;
    }
  }

  private static class ShadowVoiceInteractorCallback implements IVoiceInteractorCallback {
    private final VoiceInteractor.CommandRequest commandRequest;

    ShadowVoiceInteractorCallback(VoiceInteractor.CommandRequest commandRequest) {
      this.commandRequest = commandRequest;
    }

    @Override
    public void deliverConfirmationResult(
        IVoiceInteractorRequest request, boolean confirmed, Bundle result) throws RemoteException {}

    @Override
    public void deliverPickOptionResult(
        IVoiceInteractorRequest request, boolean finished, Option[] selections, Bundle result)
        throws RemoteException {}

    @Override
    public void deliverCompleteVoiceResult(IVoiceInteractorRequest request, Bundle result)
        throws RemoteException {}

    @Override
    public void deliverAbortVoiceResult(IVoiceInteractorRequest request, Bundle result)
        throws RemoteException {}

    @Override
    public void deliverCommandResult(
        IVoiceInteractorRequest request, boolean finished, Bundle result) throws RemoteException {
      commandRequest.onCommandResult(finished, result);
    }

    @Override
    public void deliverCancel(IVoiceInteractorRequest request) throws RemoteException {
      commandRequest.onCancel();
    }

    @Override
    public void destroy() throws RemoteException {}

    @Override
    public IBinder asBinder() {
      return null;
    }
  }
}
