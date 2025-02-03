package org.robolectric.simulator;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.internal.AndroidSandbox;

/** The main class for the Robolectric Simulator. */
public final class SimulatorMain {

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("Command-line usage: SimulatorLauncher <apk> <deploy_jar> [extra jars]");
      System.exit(1);
    }

    File apkFile = new File(args[0]);
    File deployJar = new File(args[1]);

    List<Path> extraJars = new ArrayList<>();
    for (int i = 2; i < args.length; i++) {
      extraJars.add(Path.of(args[i]));
    }

    if (!apkFile.exists()) {
      System.err.println("Missing APK file " + args[0]);
      System.exit(1);
    }

    if (!deployJar.exists()) {
      System.err.println("Missing deploy jar " + args[1]);
      System.exit(1);
    }

    final AndroidSandbox androidSandbox =
        SandboxBuilder.newBuilder()
            .addExtraJar(apkFile.toPath())
            .addExtraJar(deployJar.toPath())
            .addExtraJars(extraJars)
            .build();

    androidSandbox.runOnMainThread(
        () -> {
          try {
            Class<?> appLoaderClass = androidSandbox.bootstrappedClass(AppLoader.class);
            Constructor<?> ctor = appLoaderClass.getConstructor(AndroidSandbox.class, Path.class);
            ((Runnable) ctor.newInstance(androidSandbox, apkFile.toPath())).run();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  private SimulatorMain() {}
}
