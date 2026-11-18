package org.robolectric.preinstrumented;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.deleteIfExists;
import static java.util.concurrent.TimeUnit.MINUTES;

import com.android.zipflinger.Entry;
import com.android.zipflinger.Source;
import com.android.zipflinger.ZipArchive;
import com.android.zipflinger.ZipSource;
import com.android.zipflinger.ZipWriter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.bytecode.ClassDetails;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.ClassNodeProvider;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.inject.Injector;
import org.robolectric.versioning.VersionCalculator;

/** Runs Robolectric invokedynamic instrumentation on an android-all jar. */
public class JarInstrumentor {

  private static final int DEFLATE_BUFFER_SIZE = 8192;
  private static final int MAX_WORKER_THREADS = 16;
  private static final int MAX_PENDING_ENTRIES = 1024;

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
    // PerfStatsCollector is enabled by default, but JarInstrumentor does not report metrics.
    // Disable it to avoid per-class Event allocations and shared Metric contention across workers.
    PerfStatsCollector.getInstance().setEnabled(false);

    Path sourcePath = sourceJarFile.toPath();
    Path destPath = destJarFile.toPath();
    Path destParent = destPath.toAbsolutePath().normalize().getParent();

    int nonClassCount = 0;
    int classCount = 0;

    deleteIfExists(destPath);

    int numThreads =
        Math.max(1, Math.min(Runtime.getRuntime().availableProcessors(), MAX_WORKER_THREADS));
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    ConcurrentHashMap<Thread, DeflaterContext> deflaterContexts = new ConcurrentHashMap<>();
    Deque<PendingEntry> pendingEntries = new ArrayDeque<>();

    try (JarFile jarFile = new JarFile(sourceJarFile);
        ZipArchive zipArchive = new ZipArchive(destPath)) {
      ClassNodeProvider classNodeProvider =
          new ClassNodeProvider() {
            @Override
            protected byte[] getClassBytes(String className) throws ClassNotFoundException {
              return JarInstrumentor.getClassBytes(className, jarFile);
            }
          };

      // get the jar's SDK version
      try {
        classInstrumentor.setAndroidJarSDKVersion(getJarAndroidSDKVersion(jarFile));
      } catch (Exception e) {
        throw new AssertionError("Unable to get Android SDK version from Jar file", e);
      }

      ZipSource zipSource = new ZipSource(sourcePath);
      for (Map.Entry<String, Entry> mapEntry : zipSource.entries().entrySet()) {
        String name = mapEntry.getKey();
        Entry entry = mapEntry.getValue();
        Path normalizedPath = destParent.resolve(name).normalize();
        if (!normalizedPath.startsWith(destParent)) {
          throw new IOException("Bad zip entry: " + name);
        }
        if (name.endsWith("/")) {
          // Zero-copy directories
          zipSource.select(name, name);
        } else if (name.endsWith(".class")) {
          String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');

          int lastDotIndex = className.lastIndexOf('.');
          if (lastDotIndex != -1) {
            String packageName = className.substring(0, lastDotIndex);
            if (hasPackagesToKeepFile && !packagesToKeep.contains(packageName)) {
              continue;
            }
          }

          Future<DeflatedBytesSource> instrumentedClass =
              executor.submit(() ->
                  instrumentAndDeflateClass(
                      name, className, jarFile, classNodeProvider, deflaterContexts));
          pendingEntries.add(new PendingEntry(name, instrumentedClass));
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
            if (!entry.isCompressed() && name.endsWith(".arsc")) {
              // Compress uncompressed .arsc resource tables in parallel to avoid bloating the jar.
              Future<DeflatedBytesSource> deflatedArsc =
                  executor.submit(() -> deflateResource(name, jarFile, deflaterContexts));
              pendingEntries.add(new PendingEntry(name, deflatedArsc));
            } else {
              // Zero-copy non-class resources without decompressing or recompressing.
              zipSource.select(name, name);
            }
            nonClassCount++;
          }
        }

        if (pendingEntries.size() > MAX_PENDING_ENTRIES) {
          if (writePendingEntry(pendingEntries.remove(), zipArchive, zipSource)) {
            classCount++;
          }
        }
      }

      while (!pendingEntries.isEmpty()) {
        if (writePendingEntry(pendingEntries.remove(), zipArchive, zipSource)) {
          classCount++;
        }
      }
      zipArchive.add(zipSource);
    } finally {
      executor.shutdownNow();
      try {
        executor.awaitTermination(1, MINUTES);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      for (DeflaterContext context : deflaterContexts.values()) {
        context.deflater.end();
      }
    }

    long elapsedNs = System.nanoTime() - startNs;
    System.out.printf(
        Locale.getDefault(),
        "Wrote %d classes and %d resources in %1.2f seconds%n",
        classCount,
        nonClassCount,
        elapsedNs / 1000000000.0);
  }

  private DeflatedBytesSource instrumentAndDeflateClass(
      String entryName,
      String className,
      JarFile jarFile,
      ClassNodeProvider classNodeProvider,
      ConcurrentHashMap<Thread, DeflaterContext> deflaterContexts)
      throws ClassNotFoundException {
    byte[] classBytes = getClassBytes(className, jarFile);
    ClassDetails classDetails = new ClassDetails(classBytes);
    if (!instrumentationConfiguration.shouldInstrument(classDetails)) {
      // Signal caller to zero-copy the uninstrumented class from the source archive.
      return null;
    }
    byte[] outBytes =
        classInstrumentor.instrument(classDetails, instrumentationConfiguration, classNodeProvider);
    DeflaterContext context =
        deflaterContexts.computeIfAbsent(Thread.currentThread(), t -> new DeflaterContext());
    return DeflatedBytesSource.create(entryName, outBytes, context);
  }

  private static DeflatedBytesSource deflateResource(
      String entryName,
      JarFile jarFile,
      ConcurrentHashMap<Thread, DeflaterContext> deflaterContexts)
      throws IOException {
    ZipEntry entry = jarFile.getEntry(entryName);
    byte[] bytes;
    try (InputStream in = jarFile.getInputStream(entry)) {
      bytes = in.readAllBytes();
    }
    DeflaterContext context =
        deflaterContexts.computeIfAbsent(Thread.currentThread(), t -> new DeflaterContext());
    return DeflatedBytesSource.create(entryName, bytes, context);
  }

  private static boolean writePendingEntry(
      PendingEntry pendingEntry, ZipArchive zipArchive, ZipSource zipSource)
      throws IOException, ClassNotFoundException {
    boolean isClass = pendingEntry.name.endsWith(".class");
    DeflatedBytesSource source;
    try {
      source = pendingEntry.future.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while instrumenting " + pendingEntry.name, e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (isClass && cause instanceof NegativeArraySizeException) {
        String className =
            pendingEntry
                .name
                .substring(0, pendingEntry.name.length() - ".class".length())
                .replace('/', '.');
        System.err.println(
            "Skipping instrumenting due to NegativeArraySizeException for class: " + className);
        return false;
      }
      Throwables.throwIfInstanceOf(cause, IOException.class);
      Throwables.throwIfInstanceOf(cause, ClassNotFoundException.class);
      Throwables.throwIfUnchecked(cause);
      throw new IOException("Failed to instrument " + pendingEntry.name, cause);
    }

    if (source != null) {
      zipArchive.add(source);
    } else {
      zipSource.select(pendingEntry.name, pendingEntry.name);
    }
    return isClass;
  }

  private static final class PendingEntry {
    final String name;
    final Future<DeflatedBytesSource> future;

    PendingEntry(String name, Future<DeflatedBytesSource> future) {
      this.name = name;
      this.future = future;
    }
  }

  private static final class DeflaterContext {
    final Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, /* nowrap= */ true);
    final CRC32 crc32 = new CRC32();
    final byte[] buffer = new byte[DEFLATE_BUFFER_SIZE];
  }

  private static final class DeflatedBytesSource extends Source {
    private final ByteBuffer payload;

    private DeflatedBytesSource(
        String name, ByteBuffer payload, int crc, long uncompressedSize, long compressedSize) {
      super(name);
      this.payload = payload;
      this.crc = crc;
      this.uncompressedSize = uncompressedSize;
      this.compressedSize = compressedSize;
      this.compressionFlag = (short) ZipEntry.DEFLATED;
    }

    static DeflatedBytesSource create(String name, byte[] bytes, DeflaterContext context) {
      context.crc32.reset();
      context.crc32.update(bytes, 0, bytes.length);
      int crc = (int) context.crc32.getValue();

      context.deflater.reset();
      context.deflater.setInput(bytes, 0, bytes.length);
      context.deflater.finish();

      ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(256, bytes.length / 2));
      while (!context.deflater.finished()) {
        int count = context.deflater.deflate(context.buffer);
        if (count == 0) {
          break;
        }
        out.write(context.buffer, 0, count);
      }
      byte[] compressed = out.toByteArray();
      return new DeflatedBytesSource(
          name, ByteBuffer.wrap(compressed), crc, bytes.length, compressed.length);
    }

    @Override
    public void prepare() {}

    @Override
    public long writeTo(ZipWriter writer) throws IOException {
      return writer.write(payload);
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

  private int getJarAndroidSDKVersion(JarFile jarFile) throws IOException {
    return new VersionCalculator().calculateSdkInfo(jarFile).apiLevel;
  }
}
