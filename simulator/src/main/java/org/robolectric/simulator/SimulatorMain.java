package org.robolectric.simulator;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.runner.common.RobolectricEnvironment;
import org.robolectric.runner.common.RobolectricSession;

/** The main class for the Robolectric Simulator. */
public final class SimulatorMain {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Command-line usage: SimulatorLauncher <apk> [extra_jars]");
      System.exit(1);
    }

    File apkFile = new File(args[0]);
    if (!apkFile.exists()) {
      System.err.println("Missing APK file " + args[0]);
      System.exit(1);
    }

    List<Path> extraClasspathEntries = new ArrayList<>();
    extraClasspathEntries.add(
        apkFile.toPath()); // Include on classpath to open arsc file as a resource.
    for (int i = 1; i < args.length; i++) {
      extraClasspathEntries.add(Path.of(args[i]));
    }

    try {
      RobolectricSession session =
          RobolectricSession.builder()
              .apk(apkFile.toPath())
              .classpath(extraClasspathEntries.toArray(new Path[0]))
              // Code outside of the sandbox reads the screen from the registry.
              .sharePackage(SimulatorPanelRegistry.class.getName())
              .build();
      Config config = new Config.Builder().setSdk(getSdkVersion()).build();
      RobolectricEnvironment android =
          session.open(
              session
                  .plan(
                      config,
                      ConscryptMode.Mode.OFF,
                      LooperMode.Mode.PAUSED,
                      LazyApplication.LazyLoad.OFF,
                      GraphicsMode.Mode.NATIVE,
                      SQLiteMode.Mode.NATIVE)
                  .get(0));
      android.run(
          () -> {
            Class<?> appLoaderClass = android.loadClass(AppLoader.class);
            ((Runnable) appLoaderClass.getConstructor().newInstance()).run();
            return null;
          });
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static int getSdkVersion() {
    return Integer.parseInt(System.getProperty("robolectric.deviceconfig.sdk", "35"));
  }

  private SimulatorMain() {}
}
