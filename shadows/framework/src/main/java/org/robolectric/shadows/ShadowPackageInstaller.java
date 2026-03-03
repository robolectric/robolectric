package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.PersistableBundle;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Shadow for PackageInstaller. */
@Implements(PackageInstaller.class)
@SuppressLint("NewApi")
public class ShadowPackageInstaller {
  /** Shadow for PackageInstaller.SessionInfo. */
  @Implements(PackageInstaller.SessionInfo.class)
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
  private final Map<Integer, PackageInstaller.SessionInfo> sessionInfos = new HashMap<>();
  private final Map<Integer, PackageInstaller.Session> sessions = new HashMap<>();
  private final Set<CallbackInfo> callbackInfos = Collections.synchronizedSet(new HashSet<>());
  private final Map<String, UninstalledPackage> uninstalledPackages = new HashMap<>();

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
      @Nonnull PackageInstaller.SessionCallback callback, @Nonnull Handler handler) {
    CallbackInfo callbackInfo = new CallbackInfo();
    callbackInfo.callback = callback;
    callbackInfo.handler = handler;
    this.callbackInfos.add(callbackInfo);
  }

  @Implementation
  protected void unregisterSessionCallback(@Nonnull PackageInstaller.SessionCallback callback) {
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
  protected int createSession(@Nonnull PackageInstaller.SessionParams params) throws IOException {
    final PackageInstaller.SessionInfo sessionInfo = new PackageInstaller.SessionInfo();
    sessionInfo.sessionId = nextSessionId++;
    sessionInfo.active = true;
    sessionInfo.appPackageName = params.appPackageName;
    sessionInfo.appLabel = params.appLabel;
    sessionInfo.appIcon = params.appIcon;
    if (VERSION.SDK_INT >= P) {
      sessionInfo.installerPackageName = params.installerPackageName;
    }

    if (VERSION.SDK_INT >= TIRAMISU) {
      sessionInfo.packageSource = params.packageSource;
    }

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
  @Nonnull
  protected PackageInstaller.Session openSession(int sessionId) throws IOException {
    if (!sessionInfos.containsKey(sessionId)) {
      throw new SecurityException("Invalid session Id: " + sessionId);
    }

    if (sessions.containsKey(sessionId) && sessions.get(sessionId) != null) {
      return sessions.get(sessionId);
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
      callbackInfo.handler.post(() -> callbackInfo.callback.onBadgingChanged(sessionId));
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
      callbackInfo.handler.post(() -> callbackInfo.callback.onBadgingChanged(sessionId));
    }
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected void uninstall(
      VersionedPackage versionedPackage, int flags, IntentSender statusReceiver) {
    uninstalledPackages.put(
        versionedPackage.getPackageName(),
        new UninstalledPackage(versionedPackage.getLongVersionCode(), statusReceiver));
  }

  @Implementation(minSdk = O)
  protected void uninstall(VersionedPackage versionedPackage, IntentSender statusReceiver) {
    if (VERSION.SDK_INT < P) {
      uninstalledPackages.put(
          versionedPackage.getPackageName(),
          new UninstalledPackage((long) versionedPackage.getVersionCode(), statusReceiver));
    } else {
      uninstalledPackages.put(
          versionedPackage.getPackageName(),
          new UninstalledPackage(versionedPackage.getLongVersionCode(), statusReceiver));
    }
  }

  @Implementation
  protected void uninstall(String packageName, IntentSender statusReceiver) {
    uninstalledPackages.put(
        packageName,
        new UninstalledPackage((long) PackageManager.VERSION_CODE_HIGHEST, statusReceiver));
  }

  @Implementation(minSdk = S)
  protected void uninstallExistingPackage(String packageName, IntentSender statusReceiver) {
    uninstalledPackages.put(
        packageName,
        new UninstalledPackage((long) PackageManager.VERSION_CODE_HIGHEST, statusReceiver));
  }

  public Long getLastUninstalledVersion(String packageName) {
    if (uninstalledPackages.get(packageName) == null) {
      return null;
    }
    return uninstalledPackages.get(packageName).version;
  }

  public IntentSender getLastUninstalledStatusReceiver(String packageName) {
    if (uninstalledPackages.get(packageName) == null) {
      return null;
    }
    return uninstalledPackages.get(packageName).intentSender;
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

  /** Approve the preapproval dialog. */
  public void setPreapprovalDialogApproved(int sessionId) throws IntentSender.SendIntentException {
    sendPreapprovalUpdate(sessionId, PackageInstaller.STATUS_SUCCESS);
  }

  /** Deny the preapproval dialog. */
  public void setPreapprovalDialogDenied(int sessionId) throws IntentSender.SendIntentException {
    sendPreapprovalUpdate(sessionId, PackageInstaller.STATUS_FAILURE);
  }

  /** Close the preapproval dialog. */
  public void setPreapprovalDialogDismissed(int sessionId) throws IntentSender.SendIntentException {
    sendPreapprovalUpdate(sessionId, PackageInstaller.STATUS_FAILURE_ABORTED);
  }

  /**
   * Sends an update to the preapproval status receiver.
   *
   * @param status refers to the Session status. See
   *     https://developer.android.com/reference/android/content/pm/PackageInstaller for possible
   *     values.
   */
  private void sendPreapprovalUpdate(int sessionId, int status)
      throws IntentSender.SendIntentException {
    ShadowSession shadowSession = shadowOf(sessions.get(sessionId));
    Intent fillIn = new Intent();
    fillIn.putExtra(PackageInstaller.EXTRA_SESSION_ID, sessionId);
    fillIn.putExtra(PackageInstaller.EXTRA_STATUS, status);
    fillIn.putExtra(PackageInstaller.EXTRA_PRE_APPROVAL, true);
    shadowSession.preapprovalStatusReceiver.sendIntent(
        RuntimeEnvironment.getApplication(),
        0,
        fillIn,
        null /* onFinished */,
        null /* handler */,
        null /* requiredPermission */);
  }

  private void setSessionFinishes(final int sessionId, final boolean success) {
    for (final CallbackInfo callbackInfo : new ArrayList<>(callbackInfos)) {
      callbackInfo.handler.post(() -> callbackInfo.callback.onFinished(sessionId, success));
    }

    PackageInstaller.Session session = sessions.get(sessionId);
    ShadowSession shadowSession = Shadow.extract(session);
    if (success) {
      installCommittedSession(sessionId, shadowSession);
      try {
        shadowSession.statusReceiver.sendIntent(
            RuntimeEnvironment.getApplication(), 0, null, null, null, null);
      } catch (SendIntentException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * When a session succeeds, automatically installs the written APKs into {@link
   * ShadowPackageManager}. The base APK (base.apk / base-master.apk) is used to establish the
   * package; all other written APKs are registered as split names (with the ".apk" suffix
   * stripped).
   */
  private void installCommittedSession(int sessionId, ShadowSession shadowSession) {
    PackageInstaller.SessionInfo info = sessionInfos.get(sessionId);
    if (info == null || info.appPackageName == null) {
      return;
    }

    List<String> writtenApkNames = shadowSession.getWrittenSplitNames();
    if (writtenApkNames.isEmpty()) {
      return;
    }

    String packageName = info.appPackageName;

    List<String> splitNameList = new ArrayList<>();
    for (String apkName : writtenApkNames) {
      if (apkName.equals("base.apk") || apkName.equals("base-master.apk")) {
        continue;
      }
      splitNameList.add(
          apkName.endsWith(".apk") ? apkName.substring(0, apkName.length() - 4) : apkName);
    }
    String[] splitNames = splitNameList.toArray(new String[0]);

    android.content.pm.PackageManager pm = RuntimeEnvironment.getApplication().getPackageManager();
    ShadowPackageManager shadowPM = (ShadowPackageManager) Shadow.extract(pm);
    PackageInfo existing = shadowPM.getInternalMutablePackageInfo(packageName);

    if (existing == null) {
      PackageInfo newPkg = new PackageInfo();
      newPkg.packageName = packageName;
      newPkg.applicationInfo = new ApplicationInfo();
      newPkg.applicationInfo.packageName = packageName;
      if (splitNames.length > 0 && RuntimeEnvironment.getApiLevel() >= O) {
        shadowPM.installPackageWithSplits(newPkg, splitNames);
      } else {
        shadowPM.installPackage(newPkg);
      }
    } else if (splitNames.length > 0 && RuntimeEnvironment.getApiLevel() >= O) {
      for (String splitName : splitNames) {
        String splitApkPath =
            ShadowPackageManager.createSplitApkWithAssets(splitName, new HashMap<>());
        shadowPM.addSplitToInstalledPackage(packageName, splitName, splitApkPath);
      }
    }
  }

  /** Shadow for PackageInstaller.Session. */
  @Implements(PackageInstaller.Session.class)
  public static class ShadowSession {

    private OutputStream outputStream;
    private boolean outputStreamOpen;
    private boolean committed = false;
    private IntentSender statusReceiver;
    private IntentSender preapprovalStatusReceiver;
    private int sessionId;
    private ShadowPackageInstaller shadowPackageInstaller;
    private PersistableBundle appMetadata = new PersistableBundle();
    private final List<String> writtenSplitNames = new ArrayList<>();

    @Implementation(minSdk = UPSIDE_DOWN_CAKE)
    protected void requestUserPreapproval(
        @Nonnull @ClassName("android.content.pm.PackageInstaller$PreapprovalDetails")
            Object details,
        @Nonnull IntentSender statusReceiver) {
      preapprovalStatusReceiver = statusReceiver;
    }

    @Implementation(minSdk = UPSIDE_DOWN_CAKE)
    protected void setAppMetadata(@Nullable PersistableBundle data) throws IOException {
      appMetadata = data;
    }

    @Implementation(minSdk = UPSIDE_DOWN_CAKE)
    @Nonnull
    protected PersistableBundle getAppMetadata() {
      return appMetadata;
    }

    @Implementation
    @Nonnull
    protected OutputStream openWrite(@Nonnull String name, long offsetBytes, long lengthBytes)
        throws IOException {
      // Track the name of the written APK.
      if (name != null && !name.isEmpty()) {
        writtenSplitNames.add(name);
      }
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
    protected void fsync(@Nonnull OutputStream out) throws IOException {}

    @Implementation
    protected void commit(@Nonnull IntentSender statusReceiver) {
      this.statusReceiver = statusReceiver;
      if (outputStreamOpen) {
        throw new SecurityException("OutputStream still open");
      }
      this.committed = true;
      shadowPackageInstaller.setSessionSucceeds(sessionId);
    }

    /** Returns {@code true} if {@link #commit} has been called on this session. */
    public boolean isCommitted() {
      return committed;
    }

    @Implementation
    protected void close() {}

    @Implementation
    protected void abandon() {
      shadowPackageInstaller.abandonSession(sessionId);
    }

    /**
     * Returns the list of split names that were written to this session via {@link
     * #openWrite(String, long, long)}.
     */
    public List<String> getWrittenSplitNames() {
      return ImmutableList.copyOf(writtenSplitNames);
    }

    private void setShadowPackageInstaller(
        int sessionId, ShadowPackageInstaller shadowPackageInstaller) {
      this.sessionId = sessionId;
      this.shadowPackageInstaller = shadowPackageInstaller;
    }
  }

  private static class UninstalledPackage {
    Long version;
    IntentSender intentSender;

    public UninstalledPackage(Long version, IntentSender intentSender) {
      this.version = version;
      this.intentSender = intentSender;
    }
  }
}
