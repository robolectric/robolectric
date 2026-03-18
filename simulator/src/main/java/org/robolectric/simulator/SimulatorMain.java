package org.robolectric.simulator;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.internal.AndroidSandbox;

/** The main class for the Robolectric Simulator. */
public final class SimulatorMain {

  public static void main(String[] args) {
    Runtime.getRuntime().addShutdownHook(new Thread(SimulatorMain::logMemoryAndCpu));
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

    final AndroidSandbox androidSandbox =
        SandboxBuilder.newBuilder()
            .addClasspathEntries(extraClasspathEntries)
            .setSdkVersion(getSdkVersion())
            .build();

    try {
      androidSandbox.runOnMainThread(
          () -> {
            try {
              Class<?> appLoaderClass = androidSandbox.bootstrappedClass(AppLoader.class);
              Constructor<?> ctor = appLoaderClass.getConstructor(AndroidSandbox.class, Path.class);
              ((Runnable) ctor.newInstance(androidSandbox, apkFile.toPath())).run();
            } catch (ReflectiveOperationException e) {
              throw new RuntimeException(e);
            }
          });
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

  private static void logMemoryAndCpu() {
    System.err.println();
    System.err.printf(
        "Java heap info: totalMemory %dm, maxMemory %dm%n",
        (Runtime.getRuntime().totalMemory() / 1024 / 1024),
        (Runtime.getRuntime().maxMemory() / 1024 / 1024));
    ProcessHandle.current()
        .info()
        .totalCpuDuration()
        .ifPresent(
            duration -> {
              System.err.printf(
                  "Java process total CPU time: %.1fs%n", duration.toMillis() / 1000.0);
            });
  }

  private static int getSdkVersion() {
    return Integer.parseInt(System.getProperty("robolectric.deviceconfig.sdk", "35"));
  }

  private SimulatorMain() {}
}
