package org.robolectric.preinstrumented;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
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
import org.robolectric.util.inject.Injector;
import org.robolectric.versioning.AndroidVersionInitTools;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;

/** Runs Robolectric invokedynamic instrumentation on an android-all jar. */
public class JarInstrumentor {

  private static final int ONE_MB = 1024 * 1024;

  private static final Injector INJECTOR = new Injector.Builder().build();

  private final ClassInstrumentor classInstrumentor;
  private final InstrumentationConfiguration instrumentationConfiguration;

  private boolean hasPackagesToKeepFile;
  private ImmutableSet<String> packagesToKeep = ImmutableSet.of();

  private boolean hasResourcesToKeepFile;
  private ImmutableSet<String> resourceFilesToKeep = ImmutableSet.of();
  private ImmutableSet<String> resourceDirsToKeep = ImmutableSet.of();

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
    if (args.length < 2) {
      System.err.println(
          "Usage: JarInstrumentor"
              + " [--packages_to_keep=file path containing package list]"
              + " [--resources_to_keep=file path containing resource list]"
              + " <source jar> <dest jar> ");
      exit(1);
    }
    File sourceFile = null;
    File destFile = null;

    for (String arg : args) {
      if (arg.startsWith("--packages_to_keep=")) {
        File packagesToKeepFile = new File(arg.substring(arg.indexOf('=') + 1));
        if (!packagesToKeepFile.exists()) {
          System.err.println("Packages file does not exist: " + packagesToKeepFile);
          exit(1);
          return;
        }
        hasPackagesToKeepFile = true;
        packagesToKeep = ImmutableSet.copyOf(Files.readLines(packagesToKeepFile, UTF_8));
        Preconditions.checkState(!packagesToKeep.isEmpty(), "Package files must be non-empty.");
      } else if (arg.startsWith("--resources_to_keep=")) {
        File resourcesToKeepFile = new File(arg.substring(arg.indexOf('=') + 1));
        if (!resourcesToKeepFile.exists()) {
          System.err.println("Resources file does not exist: " + resourcesToKeepFile);
          exit(1);
          return;
        }
        List<String> resourceFiles = Files.readLines(resourcesToKeepFile, UTF_8);
        resourceFilesToKeep =
            ImmutableSet.copyOf(Iterables.filter(resourceFiles, s -> !s.endsWith("/")));
        resourceDirsToKeep =
            ImmutableSet.copyOf(Iterables.filter(resourceFiles, s -> s.endsWith("/")));
        Preconditions.checkState(
            !resourceFilesToKeep.isEmpty() && !resourceDirsToKeep.isEmpty(),
            "Resource files and directories must be specified.");
        hasResourcesToKeepFile = true;
      } else if (arg.startsWith("--")) {
        System.err.println("Unknown flag: " + arg);
        exit(1);
        return;
      } else if (sourceFile == null) {
        sourceFile = new File(arg);
      } else if (destFile == null) {
        destFile = new File(arg);
      }
    }
    instrumentJar(sourceFile, destFile);
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
   */
  @VisibleForTesting
  protected void instrumentJar(File sourceJarFile, File destJarFile)
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
        Path normalizedPath = new File(destJarFile.getParentFile(), name).toPath().normalize();
        if (!normalizedPath.startsWith(destJarFile.getParentFile().toPath())) {
          throw new IOException("Bad zip entry: " + name);
        }
        if (name.endsWith("/")) {
          // Copy directories
          jarOut.putNextEntry(createJarEntry(jarEntry));
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          int lastDotIndex = className.lastIndexOf('.');
          if (lastDotIndex != -1) {
            String packageName = className.substring(0, lastDotIndex);
            if (hasPackagesToKeepFile && !packagesToKeep.contains(packageName)) {
              continue;
            }
          }

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
          boolean shouldKeep = true;
          if (hasResourcesToKeepFile) {
            shouldKeep = false;
            if (resourceFilesToKeep.contains(name)) {
              shouldKeep = true;
            }
            for (String dir : resourceDirsToKeep) {
              if (name.startsWith(dir)) {
                shouldKeep = true;
              }
            }
          }
          if (shouldKeep) {
            jarOut.putNextEntry(createJarEntry(jarEntry));
            ByteStreams.copy(jarFile.getInputStream(jarEntry), jarOut);
            nonClassCount++;
          }
        }
      }
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
