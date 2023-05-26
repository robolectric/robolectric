package org.robolectric.preinstrumented;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.bytecode.ClassDetails;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.ClassNodeProvider;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.NativeCallHandler;
import org.robolectric.util.inject.Injector;
import org.robolectric.versioning.AndroidVersionInitTools;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;

/** Runs Robolectric invokedynamic instrumentation on an android-all jar. */
public class JarInstrumentor {

  private static final int ONE_MB = 1024 * 1024;

  private static final Injector INJECTOR = new Injector.Builder().build();

  private final ClassInstrumentor classInstrumentor;
  private final InstrumentationConfiguration instrumentationConfiguration;

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    new JarInstrumentor().processCommandLine(args);
  }

  public JarInstrumentor() {
    AndroidConfigurer androidConfigurer = INJECTOR.getInstance(AndroidConfigurer.class);
    classInstrumentor = INJECTOR.getInstance(ClassInstrumentor.class);

    InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder();
    Interceptors interceptors = new Interceptors(AndroidInterceptors.all());
    androidConfigurer.configure(builder, interceptors);
    instrumentationConfiguration = builder.build();
  }

  @VisibleForTesting
  void processCommandLine(String[] args) throws IOException, ClassNotFoundException {
    if (args.length >= 2) {
      File sourceFile = new File(args[0]);
      File destJarFile = new File(args[1]);

      File destNativesFile = null;
      boolean throwOnNatives = false;
      boolean parseError = false;
      for (int i = 2; i < args.length; i++) {
        if (args[i].startsWith("--write-natives=")) {
          destNativesFile = new File(args[i].substring("--write-natives=".length()));
        } else if (args[i].equals("--throw-on-natives")) {
          throwOnNatives = true;
        } else {
          System.err.println("Unknown argument: " + args[i]);
          parseError = true;
          break;
        }
      }

      if (!parseError) {
        instrumentJar(sourceFile, destJarFile, destNativesFile, throwOnNatives);
        return;
      }
    }

    System.err.println(
        "Usage: JarInstrumentor <source jar> <dest jar> "
            + "[--write-natives=<file>] "
            + "[--throw-on-natives]");
    exit(1);
  }

  /** Calls {@link System#exit(int)}. Overridden during tests to avoid exiting during tests. */
  @VisibleForTesting
  protected void exit(int status) {
    System.exit(status);
  }

  /**
   * Performs the JAR instrumentation.
   *
   * @param sourceJarFile The source JAR to process.
   * @param destJarFile The destination JAR with the instrumented method calls.
   * @param destNativesFile Optional file to write native calls signature. Null to disable.
   * @param throwOnNatives Whether native calls should be instrumented as throwing a dedicated
   *     exception (true) or no-op (false).
   */
  @VisibleForTesting
  protected void instrumentJar(
      File sourceJarFile, File destJarFile, File destNativesFile, boolean throwOnNatives)
      throws IOException, ClassNotFoundException {
    long startNs = System.nanoTime();
    JarFile jarFile = new JarFile(sourceJarFile);
    ClassNodeProvider classNodeProvider =
        new ClassNodeProvider() {
          @Override
          protected byte[] getClassBytes(String className) throws ClassNotFoundException {
            return JarInstrumentor.getClassBytes(className, jarFile);
          }
        };

    NativeCallHandler nativeCallHandler;
    final boolean writeNativesFile = destNativesFile != null;

    if (destNativesFile == null) {
      destNativesFile =
          new File(
              sourceJarFile.getParentFile(),
              sourceJarFile.getName().replace(".jar", "-natives.txt"));
    }

    try {
      nativeCallHandler = new NativeCallHandler(destNativesFile, writeNativesFile, throwOnNatives);
      classInstrumentor.setNativeCallHandler(nativeCallHandler);
    } catch (IOException e) {
      throw new AssertionError("Unable to load native exemptions file", e);
    }

    int nonClassCount = 0;
    int classCount = 0;

    // get the jar's SDK version
    try {
      classInstrumentor.setAndroidJarSDKVersion(getJarAndroidSDKVersion(jarFile));
    } catch (Exception e) {
      throw new AssertionError("Unable to get Android SDK version from Jar file", e);
    }

    try (JarOutputStream jarOut =
        new JarOutputStream(new BufferedOutputStream(new FileOutputStream(destJarFile), ONE_MB))) {
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();

        String name = jarEntry.getName();
        if (name.endsWith("/")) {
          jarOut.putNextEntry(createJarEntry(jarEntry));
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          try {
            byte[] classBytes = getClassBytes(className, jarFile);
            ClassDetails classDetails = new ClassDetails(classBytes);
            byte[] outBytes = classBytes;
            if (instrumentationConfiguration.shouldInstrument(classDetails)) {
              outBytes =
                  classInstrumentor.instrument(
                      classDetails, instrumentationConfiguration, classNodeProvider);
            }
            jarOut.putNextEntry(createJarEntry(jarEntry));
            jarOut.write(outBytes);
            classCount++;
          } catch (NegativeArraySizeException e) {
            System.err.println(
                "Skipping instrumenting due to NegativeArraySizeException for class: " + className);
          }
        } else {
          // resources & stuff
          jarOut.putNextEntry(createJarEntry(jarEntry));
          ByteStreams.copy(jarFile.getInputStream(jarEntry), jarOut);
          nonClassCount++;
        }
      }
    }

    if (writeNativesFile) {
      nativeCallHandler.writeExemptionsList();
    }

    long elapsedNs = System.nanoTime() - startNs;
    System.out.println(
        String.format(
            Locale.getDefault(),
            "Wrote %d classes and %d resources in %1.2f seconds",
            classCount,
            nonClassCount,
            elapsedNs / 1000000000.0));
  }

  private static byte[] getClassBytes(String className, JarFile jarFile)
      throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    ZipEntry entry = jarFile.getEntry(classFilename);
    try {
      InputStream inputStream;
      if (entry == null) {
        inputStream = JarInstrumentor.class.getClassLoader().getResourceAsStream(classFilename);
      } else {
        inputStream = jarFile.getInputStream(entry);
      }
      if (inputStream == null) {
        throw new ClassNotFoundException("Couldn't find " + className.replace('/', '.'));
      }
      return ByteStreams.toByteArray(inputStream);
    } catch (IOException e) {
      throw new ClassNotFoundException("Couldn't load " + className.replace('/', '.'), e);
    }
  }

  private static JarEntry createJarEntry(JarEntry original) {
    JarEntry entry = new JarEntry(original.getName());
    // Setting the timestamp to the original is necessary for deterministic output.
    entry.setTime(original.getTime());
    return entry;
  }

  private int getJarAndroidSDKVersion(JarFile jarFile) throws IOException {
    AndroidRelease release = AndroidVersionInitTools.computeReleaseVersion(jarFile);
    return release.getSdkInt();
  }
}
