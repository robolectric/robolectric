package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = PackageInstaller.class, minSdk = LOLLIPOP)
public class ShadowPackageInstaller {

  private int nextSessionId;
  private Map<Integer, PackageInstaller.SessionInfo> sessionInfos = new HashMap<>();
  private Map<Integer, PackageInstaller.Session> sessions = new HashMap<>();
  private Set<CallbackInfo> callbackInfos = new HashSet<>();

  private static class CallbackInfo {
    PackageInstaller.SessionCallback callback;
    Handler handler;
  }

  @Implementation
  public List<PackageInstaller.SessionInfo> getAllSessions() {
    return ImmutableList.copyOf(sessionInfos.values());
  }

  @Implementation
  public void registerSessionCallback(@NonNull PackageInstaller.SessionCallback callback, @NonNull Handler handler) {
    CallbackInfo callbackInfo = new CallbackInfo();
    callbackInfo.callback = callback;
    callbackInfo.handler = handler;
    this.callbackInfos.add(callbackInfo);
  }

  @Implementation
  @Nullable
  public PackageInstaller.SessionInfo getSessionInfo(int sessionId) {
    return sessionInfos.get(sessionId);
  }

  @Implementation
  public int createSession(@NonNull PackageInstaller.SessionParams params) throws IOException {
    final PackageInstaller.SessionInfo sessionInfo = new PackageInstaller.SessionInfo();
    sessionInfo.sessionId = nextSessionId++;
    sessionInfo.active = true;
    sessionInfo.appPackageName = params.appPackageName;
    sessionInfos.put(sessionInfo.getSessionId(), sessionInfo);

    for (final CallbackInfo callbackInfo : callbackInfos) {
      callbackInfo.handler.post(new Runnable() {
        @Override
        public void run() {
          callbackInfo.callback.onCreated(sessionInfo.sessionId);
        }
      });
    }

    return sessionInfo.sessionId;
  }

  @Implementation
  public void abandonSession(int sessionId) {
    sessionInfos.remove(sessionId);
    sessions.remove(sessionId);

    for (final CallbackInfo callbackInfo : callbackInfos) {
      callbackInfo.handler.post(new Runnable() {
        @Override
        public void run() {
          callbackInfo.callback.onFinished(sessionId, false);
        }
      });
    }
  }

  @Implementation
  @NonNull
  public PackageInstaller.Session openSession(int sessionId) throws IOException {
    if (!sessionInfos.containsKey(sessionId)) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }

    PackageInstaller.Session session = new PackageInstaller.Session(null);
    shadowOf(session).setShadowPackageInstaller(sessionId, this);
    sessions.put(sessionId, session);
    return session;
  }

  public void setSessionProgress(final int sessionId, final float progress) {
    for (final CallbackInfo callbackInfo : callbackInfos) {
      callbackInfo.handler.post(new Runnable() {
        @Override
        public void run() {
          callbackInfo.callback.onProgressChanged(sessionId, progress);
        }
      });
    }
  }

  /**
   * Prefer instead to use the Android APIs to close the session
   * {@link android.content.pm.PackageInstaller.Session#commit(IntentSender)}
   */
  @Deprecated
  public void setSessionSucceeds(int sessionId) {
    setSessionFinishes(sessionId, true);
  }

  public void setSessionFails(int sessionId) {
    setSessionFinishes(sessionId, false);
  }

  private void setSessionFinishes(final int sessionId, final boolean success) {
    for (final CallbackInfo callbackInfo : callbackInfos) {
      callbackInfo.handler.post(new Runnable() {
        @Override
        public void run() {
          callbackInfo.callback.onFinished(sessionId, success);
        }
      });
    }

    PackageInstaller.Session session = sessions.get(sessionId);
    ShadowSession shadowSession = shadowOf(session);
    if (success) {
      try {
        shadowSession.statusReceiver
            .sendIntent(RuntimeEnvironment.application, 0, null, null, null, null);
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

    @Implementation
    public void __constructor__() {}

    @Implementation
    public @NonNull OutputStream openWrite(@NonNull String name, long offsetBytes, long lengthBytes) throws IOException {
      outputStream = new OutputStream() {
        @Override
        public void write(int aByte) throws IOException {

        }

        @Override
        public void close() throws IOException {
          outputStreamOpen = false;
        }
      };
      outputStreamOpen = true;
      return outputStream;
    }

    @Implementation
    public void fsync(@NonNull OutputStream out) throws IOException {

    }

    @Implementation
    public void commit(@NonNull IntentSender statusReceiver) {
      this.statusReceiver = statusReceiver;
      if (outputStreamOpen) {
        throw new SecurityException("OutputStream still open");
      }

      shadowPackageInstaller.setSessionSucceeds(sessionId);
    }

    @Implementation
    public void close() {

    }

    @Implementation
    public void abandon() {
      shadowPackageInstaller.abandonSession(sessionId);
    }

    private void setShadowPackageInstaller(int sessionId,
        ShadowPackageInstaller shadowPackageInstaller) {
      this.sessionId = sessionId;
      this.shadowPackageInstaller = shadowPackageInstaller;
    }
  }
}
