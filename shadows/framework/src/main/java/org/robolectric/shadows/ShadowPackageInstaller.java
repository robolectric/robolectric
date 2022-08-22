package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionInfo;
import android.graphics.Bitmap;
import android.os.Handler;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Shadow for PackageInstaller. */
@Implements(value = PackageInstaller.class, minSdk = LOLLIPOP)
@SuppressLint("NewApi")
public class ShadowPackageInstaller {
  /** Shadow for PackageInstaller.SessionInfo. */
  @Implements(value = PackageInstaller.SessionInfo.class, minSdk = LOLLIPOP)
  public static class ShadowSessionInfo {
    @RealObject private SessionInfo sessionInfo;

    /** Real method makes a system call not available in tests. */
    @Implementation
    protected Bitmap getAppIcon() {
      return sessionInfo.appIcon;
    }
  }

  // According to the documentation, the session ID is always non-zero:
  // https://developer.android.com/reference/android/content/pm/PackageInstaller#createSession(android.content.pm.PackageInstaller.SessionParams)
  private int nextSessionId = 1;
  private Map<Integer, PackageInstaller.SessionInfo> sessionInfos = new HashMap<>();
  private Map<Integer, PackageInstaller.Session> sessions = new HashMap<>();
  private Set<CallbackInfo> callbackInfos = Collections.synchronizedSet(new HashSet<>());

  private static class CallbackInfo {
    PackageInstaller.SessionCallback callback;
    Handler handler;
  }

  @Implementation
  protected List<PackageInstaller.SessionInfo> getAllSessions() {
    return ImmutableList.copyOf(sessionInfos.values());
  }

  @Implementation
  protected List<PackageInstaller.SessionInfo> getMySessions() {
    return getAllSessions();
  }

  @Implementation
  protected void registerSessionCallback(
      @NonNull PackageInstaller.SessionCallback callback, @NonNull Handler handler) {
    CallbackInfo callbackInfo = new CallbackInfo();
    callbackInfo.callback = callback;
    callbackInfo.handler = handler;
    this.callbackInfos.add(callbackInfo);
  }

  @Implementation
  protected void unregisterSessionCallback(@NonNull PackageInstaller.SessionCallback callback) {
    for (Iterator<CallbackInfo> i = callbackInfos.iterator(); i.hasNext(); ) {
      final CallbackInfo callbackInfo = i.next();
      if (callbackInfo.callback == callback) {
        i.remove();
        return;
      }
    }
  }

  @Implementation
  @Nullable
  protected PackageInstaller.SessionInfo getSessionInfo(int sessionId) {
    return sessionInfos.get(sessionId);
  }

  @Implementation
  protected int createSession(@NonNull PackageInstaller.SessionParams params) throws IOException {
    final PackageInstaller.SessionInfo sessionInfo = new PackageInstaller.SessionInfo();
    sessionInfo.sessionId = nextSessionId++;
    sessionInfo.active = true;
    sessionInfo.appPackageName = params.appPackageName;
    sessionInfo.appLabel = params.appLabel;
    sessionInfo.appIcon = params.appIcon;

    sessionInfos.put(sessionInfo.getSessionId(), sessionInfo);

    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onCreated(sessionInfo.sessionId));
    }

    return sessionInfo.sessionId;
  }

  @Implementation
  protected void abandonSession(int sessionId) {
    sessionInfos.remove(sessionId);
    sessions.remove(sessionId);

    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onFinished(sessionId, false));
    }
  }

  @Implementation
  @NonNull
  protected PackageInstaller.Session openSession(int sessionId) throws IOException {
    if (!sessionInfos.containsKey(sessionId)) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }

    PackageInstaller.Session session = new PackageInstaller.Session(null);
    ShadowSession shadowSession = Shadow.extract(session);
    shadowSession.setShadowPackageInstaller(sessionId, this);
    sessions.put(sessionId, session);
    return session;
  }

  @Implementation
  protected void updateSessionAppIcon(int sessionId, Bitmap appIcon) {
    SessionInfo sessionInfo = sessionInfos.get(sessionId);
    if (sessionInfo == null) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }
    sessionInfo.appIcon = appIcon;

    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(
          new Runnable() {
            @Override
            public void run() {
              callbackInfo.callback.onBadgingChanged(sessionId);
            }
          });
    }
  }

  @Implementation
  protected void updateSessionAppLabel(int sessionId, CharSequence appLabel) {
    SessionInfo sessionInfo = sessionInfos.get(sessionId);
    if (sessionInfo == null) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }
    sessionInfo.appLabel = appLabel;

    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(
          new Runnable() {
            @Override
            public void run() {
              callbackInfo.callback.onBadgingChanged(sessionId);
            }
          });
    }
  }

  public List<PackageInstaller.SessionCallback> getAllSessionCallbacks() {
    return ImmutableList.copyOf(callbackInfos.stream().map(info -> info.callback).iterator());
  }

  public void setSessionProgress(final int sessionId, final float progress) {
    SessionInfo sessionInfo = sessionInfos.get(sessionId);
    if (sessionInfo == null) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }
    sessionInfo.progress = progress;

    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onProgressChanged(sessionId, progress));
    }
  }

  public void setSessionActiveState(final int sessionId, final boolean active) {
    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onActiveChanged(sessionId, active));
    }
  }

  /**
   * Prefer instead to use the Android APIs to close the session {@link
   * android.content.pm.PackageInstaller.Session#commit(IntentSender)}
   */
  @Deprecated
  public void setSessionSucceeds(int sessionId) {
    setSessionFinishes(sessionId, true);
  }

  public void setSessionFails(int sessionId) {
    setSessionFinishes(sessionId, false);
  }

  private void setSessionFinishes(final int sessionId, final boolean success) {
    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onFinished(sessionId, success));
    }

    PackageInstaller.Session session = sessions.get(sessionId);
    ShadowSession shadowSession = Shadow.extract(session);
    if (success) {
      try {
        shadowSession.statusReceiver.sendIntent(
            RuntimeEnvironment.getApplication(), 0, null, null, null, null);
      } catch (SendIntentException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Implements(value = PackageInstaller.Session.class, minSdk = LOLLIPOP)
  public static class ShadowSession {

    private OutputStream outputStream;
    private boolean outputStreamOpen;
    private IntentSender statusReceiver;
    private int sessionId;
    private ShadowPackageInstaller shadowPackageInstaller;

    @Implementation(maxSdk = KITKAT_WATCH)
    protected void __constructor__() {}

    @Implementation
    @NonNull
    protected OutputStream openWrite(@NonNull String name, long offsetBytes, long lengthBytes)
        throws IOException {
      outputStream =
          new OutputStream() {
            @Override
            public void write(int aByte) throws IOException {}

            @Override
            public void close() throws IOException {
              outputStreamOpen = false;
            }
          };
      outputStreamOpen = true;
      return outputStream;
    }

    @Implementation
    protected void fsync(@NonNull OutputStream out) throws IOException {}

    @Implementation
    protected void commit(@NonNull IntentSender statusReceiver) {
      this.statusReceiver = statusReceiver;
      if (outputStreamOpen) {
        throw new SecurityException("OutputStream still open");
      }

      shadowPackageInstaller.setSessionSucceeds(sessionId);
    }

    @Implementation
    protected void close() {}

    @Implementation
    protected void abandon() {
      shadowPackageInstaller.abandonSession(sessionId);
    }

    private void setShadowPackageInstaller(
        int sessionId, ShadowPackageInstaller shadowPackageInstaller) {
      this.sessionId = sessionId;
      this.shadowPackageInstaller = shadowPackageInstaller;
    }
  }
}
