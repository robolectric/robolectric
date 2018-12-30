package org.robolectric.android.internal;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

public class AndroidDevice {
  private static AndroidDevice INSTANCE;

  public static void register(AndroidDevice androidDevice) {
    if (INSTANCE != null) {
      throw new RuntimeException(AndroidDevice.class.getSimpleName() + " is already initialized");
    }

    INSTANCE = androidDevice;
  }

  public static AndroidDevice get() {
    return INSTANCE;
  }
  public final int apiLevel;

  public final boolean legacyResourceMode;

  private boolean initialized = false;

  public AndroidDevice(int apiLevel, boolean legacyResourceMode) {
    this.apiLevel = apiLevel;
    this.legacyResourceMode = legacyResourceMode;
  }

  public void reset(String sessionName) {
    RuntimeEnvironment.application = null;
    RuntimeEnvironment.setActivityThread(null);
    RuntimeEnvironment.setTempDirectory(new TempDirectory(sessionName));
    RuntimeEnvironment.setMasterScheduler(new Scheduler());
    RuntimeEnvironment.setMainThread(Thread.currentThread());

    if (!initialized) {
      lateInitialize();
      initialized = true;
    }


  }

  private void lateInitialize() {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    ShadowLog.setupLogging();
  }
}
