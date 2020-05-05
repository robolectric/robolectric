package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.Dialog;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.service.voice.VoiceInteractionSession;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow implementation of {@link android.service.voice.VoiceInteractionSession}. */
@Implements(value = VoiceInteractionSession.class, minSdk = LOLLIPOP)
public class ShadowVoiceInteractionSession {

  private final List<Intent> assistantActivityIntents = new ArrayList<>();
  private final List<Intent> voiceActivityIntents = new ArrayList<>();

  private boolean isFinishing;
  @Nullable private RuntimeException startVoiceActivityException;
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

  /** Returns whether the UI window from {@link VoiceInteractionSession} is currently showing. */
  public boolean isWindowShowing() {
    boolean windowVisible = ReflectionHelpers.getField(realSession, "mWindowVisible");
    Dialog window = ReflectionHelpers.getField(realSession, "mWindow");
    return windowVisible && window != null && window.isShowing();
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
   */
  public void setStartVoiceActivityException(RuntimeException exception) {
    startVoiceActivityException = exception;
  }

  // Extends com.android.internal.app.IVoiceInteractionManagerService.Stub
  private class FakeVoiceInteractionManagerService {

    // @Override
    public boolean showSessionFromSession(IBinder token, Bundle args, int flags) {
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

    // @Override
    public int startVoiceActivity(IBinder token, Intent intent, String resolvedType) {
      RuntimeException exception = startVoiceActivityException;
      if (exception != null) {
        throw exception;
      }
      voiceActivityIntents.add(intent);
      return 0;
    }

    // @Override
    public int startAssistantActivity(IBinder token, Intent intent, String resolvedType) {
      assistantActivityIntents.add(intent);
      return 0;
    }

    // @Override
    public void finish(IBinder token) {
      ReflectionHelpers.callInstanceMethod(realSession, "doDestroy");
      isFinishing = true;
    }
  }
}
