package org.robolectric.preinstrumented;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import org.robolectric.versioning.VersionCalculator;

/** Runs Robolectric invokedynamic instrumentation on an android-all jar. */
public class JarInstrumentor {

  private static final int ONE_MB = 1024 * 1024;

  /** The maximum number of entries that are read ahead of the one being written. */
  private static final int MAX_PENDING_ENTRIES = 1024;

  private static final Injector INJECTOR = new Injector.Builder().build();

  private final ClassInstrumentor classInstrumentor;
  private final InstrumentationConfiguration instrumentationConfiguration;

  private boolean hasPackagesToKeepFile;
  private ImmutableSet<String> packagesToKeep = ImmutableSet.of();

  private boolean hasResourcesToKeepFile;
  private ImmutableSet<String> resourceFilesToKeep = ImmutableSet.of();
  private ImmutableSet<String> resourceDirsToKeep = ImmutableSet.of();

  private int classCount;
  private int nonClassCount;

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

    classCount = 0;
    nonClassCount = 0;

    // get the jar's SDK version
    try {
      classInstrumentor.setAndroidJarSDKVersion(getJarAndroidSDKVersion(jarFile));
    } catch (Exception e) {
      throw new AssertionError("Unable to get Android SDK version from Jar file", e);
    }

    // Classes are instrumented on a thread pool while this thread writes the entries in their
    // original order, so the output does not depend on scheduling.
    ExecutorService executor =
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    Deque<PendingEntry> pendingEntries = new ArrayDeque<>();
    try (JarOutputStream jarOut =
        new JarOutputStream(
            new BufferedOutputStream(newOutputStream(destJarFile.toPath()), ONE_MB))) {
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
          pendingEntries.add(new PendingEntry(jarEntry, null));
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          int lastDotIndex = className.lastIndexOf('.');
          if (lastDotIndex != -1) {
            String packageName = className.substring(0, lastDotIndex);
            if (hasPackagesToKeepFile && !packagesToKeep.contains(packageName)) {
              continue;
            }
          }

          Future<byte[]> instrumentedClass =
              executor.submit(() -> instrumentClass(className, jarFile, classNodeProvider));
          pendingEntries.add(new PendingEntry(jarEntry, instrumentedClass));
        } else {
          boolean shouldKeep = true;
          if (hasResourcesToKeepFile) {
            shouldKeep = resourceFilesToKeep.contains(name);
            for (String dir : resourceDirsToKeep) {
              if (name.startsWith(dir)) {
                shouldKeep = true;
                break;
              }
            }
          }
          if (shouldKeep) {
            pendingEntries.add(new PendingEntry(jarEntry, null));
          }
        }

        if (pendingEntries.size() > MAX_PENDING_ENTRIES) {
          writeEntry(pendingEntries.remove(), jarFile, jarOut);
        }
      }
      while (!pendingEntries.isEmpty()) {
        writeEntry(pendingEntries.remove(), jarFile, jarOut);
      }
    } finally {
      executor.shutdownNow();
    }

    long elapsedNs = System.nanoTime() - startNs;
    System.out.printf(
        Locale.getDefault(),
        "Wrote %d classes and %d resources in %1.2f seconds%n",
        classCount,
        nonClassCount,
        elapsedNs / 1000000000.0);
  }

  private byte[] instrumentClass(
      String className, JarFile jarFile, ClassNodeProvider classNodeProvider)
      throws ClassNotFoundException {
    byte[] classBytes = getClassBytes(className, jarFile);
    ClassDetails classDetails = new ClassDetails(classBytes);
    if (instrumentationConfiguration.shouldInstrument(classDetails)) {
      return classInstrumentor.instrument(
          classDetails, instrumentationConfiguration, classNodeProvider);
    }
    return classBytes;
  }

  private void writeEntry(PendingEntry pendingEntry, JarFile jarFile, JarOutputStream jarOut)
      throws IOException, ClassNotFoundException {
    JarEntry jarEntry = pendingEntry.jarEntry;
    String name = jarEntry.getName();
    if (name.endsWith("/")) {
      jarOut.putNextEntry(createJarEntry(jarEntry));
    } else if (pendingEntry.instrumentedClass != null) {
      byte[] outBytes;
      try {
        outBytes = pendingEntry.instrumentedClass.get();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Interrupted while instrumenting " + name, e);
      } catch (ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof NegativeArraySizeException) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
          System.err.println(
              "Skipping instrumenting due to NegativeArraySizeException for class: " + className);
          return;
        }
        Throwables.throwIfInstanceOf(cause, ClassNotFoundException.class);
        Throwables.throwIfUnchecked(cause);
        throw new AssertionError(cause);
      }
      jarOut.putNextEntry(createJarEntry(jarEntry));
      jarOut.write(outBytes);
      classCount++;
    } else {
      jarOut.putNextEntry(createJarEntry(jarEntry));
      ByteStreams.copy(jarFile.getInputStream(jarEntry), jarOut);
      nonClassCount++;
    }
  }

  /** An entry to write, with its instrumented class bytes if it is a class. */
  private static final class PendingEntry {
    final JarEntry jarEntry;
    final Future<byte[]> instrumentedClass;

    PendingEntry(JarEntry jarEntry, Future<byte[]> instrumentedClass) {
      this.jarEntry = jarEntry;
      this.instrumentedClass = instrumentedClass;
    }
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
    return new VersionCalculator().calculateSdkInfo(jarFile).apiLevel;
  }
}
