package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import android.database.CursorWindow;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.Hyphenator;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import javax.annotation.Priority;
import org.robolectric.pluginapi.NativeRuntimeLoader;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.Logger;
import org.robolectric.util.OsUtil;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.TempDirectory;
import org.robolectric.util.inject.Injector;

/** Loads the Robolectric native runtime. */
@AutoService(NativeRuntimeLoader.class)
@Priority(Integer.MIN_VALUE)
public class DefaultNativeRuntimeLoader implements NativeRuntimeLoader {
  protected static final AtomicBoolean loaded = new AtomicBoolean(false);

  private static final AtomicReference<NativeRuntimeLoader> nativeRuntimeLoader =
      new AtomicReference<>();

  /**
   * Set to {@code false} to extract the native runtime's data assets into a fresh temporary
   * directory on every load, which is the behavior from before these assets were cached.
   */
  private static final String CACHE_ASSETS_PROPERTY = "robolectric.nativeruntime.cacheAssets";

  private static final String ASSET_CACHE_DIR_NAME = "robolectric-nativeruntime-assets";

  /** Written once a cache directory is fully populated, so partial extractions are never used. */
  private static final String ASSET_CACHE_MARKER = ".complete";

  protected static final String METHOD_BINDING_FORMAT = "$$robo$$${method}$nativeBinding";
  private static final String HYPHEN_DATA_DIR = "hyphen-data";
  private static final String FONTS_DIR = "fonts";
  private static final String ICU_DIR = "icu";

  // These system properties are used to configure JNI registration for RNG (libandroid_runtime)
  // when it is being loaded. They are also used by Paparazzi, which loads a different version of
  // libandroid_runtime that is packaged in Android Studio's LayoutLib. To ensure that RNG does not
  // inadvertently override the LayoutLib configuration, these properties are saved before RNG is
  // loaded and restored afterwards.
  private static final ImmutableList<String> PROPERTIES_TO_RESTORE =
      ImmutableList.of(
          "use_base_native_hostruntime", // Whether to use base HostRuntime or LayoutlibLoader.
          "core_native_classes", // Classes in base/core/jni to register.
          "graphics_native_classes", // Classes in libs/hwui/jni to register.
          "method_binding_format" // Format for method binding. Used to support shadows in RNG.
          );

  // Core classes for which native methods are to be registered for Android V and above.
  protected static final ImmutableList<String> CORE_CLASS_NATIVES =
      ImmutableList.copyOf(
          new String[] {
            "android.animation.PropertyValuesHolder",
            "android.database.CursorWindow",
            "android.database.sqlite.SQLiteConnection",
            "android.database.sqlite.SQLiteRawStatement",
            "android.media.ImageReader",
            "android.os.SystemProperties",
            "android.text.Hyphenator",
            "android.view.Surface",
            "com.android.internal.util.VirtualRefBasePtr",
            "libcore.util.NativeAllocationRegistry",
          });

  // Graphics classes for which native methods are to be registered.
  protected static final ImmutableList<String> GRAPHICS_CLASS_NATIVES =
      ImmutableList.copyOf(
          new String[] {
            "android.graphics.Bitmap",
            "android.graphics.BitmapFactory",
            "android.graphics.BitmapRegionDecoder",
            "android.graphics.ByteBufferStreamAdaptor",
            "android.graphics.Camera",
            "android.graphics.Canvas",
            "android.graphics.CanvasProperty",
            "android.graphics.Color",
            "android.graphics.ColorFilter",
            "android.graphics.ColorSpace",
            "android.graphics.CreateJavaOutputStreamAdaptor",
            "android.graphics.DrawFilter",
            "android.graphics.FontFamily",
            "android.graphics.Gainmap",
            "android.graphics.Graphics",
            "android.graphics.HardwareRenderer",
            "android.graphics.HardwareRendererObserver",
            "android.graphics.ImageDecoder",
            "android.graphics.Interpolator",
            "android.graphics.MaskFilter",
            "android.graphics.Matrix",
            "android.graphics.NinePatch",
            "android.graphics.Paint",
            "android.graphics.Path",
            "android.graphics.PathEffect",
            "android.graphics.PathIterator",
            "android.graphics.PathMeasure",
            "android.graphics.Picture",
            "android.graphics.RecordingCanvas",
            "android.graphics.Region",
            "android.graphics.RenderEffect",
            "android.graphics.RenderNode",
            "android.graphics.Shader",
            "android.graphics.Typeface",
            "android.graphics.YuvImage",
            "android.graphics.animation.NativeInterpolatorFactory",
            "android.graphics.animation.RenderNodeAnimator",
            "android.graphics.drawable.AnimatedVectorDrawable",
            "android.graphics.drawable.AnimatedImageDrawable",
            "android.graphics.drawable.VectorDrawable",
            "android.graphics.fonts.Font",
            "android.graphics.fonts.FontFamily",
            "android.graphics.text.LineBreaker",
            "android.graphics.text.MeasuredText",
            "android.graphics.text.TextRunShaper",
            "android.util.PathParser",
          });

  /**
   * {@code DEFERRED_STATIC_INITIALIZERS} that invoke their own native methods in static
   * initializers. Unlike libcore, registering JNI on the JVM causes static initialization to be
   * performed on the class. Because of this, static initializers cannot invoke the native methods
   * of the class under registration. Executing these static initializers must be deferred until
   * after JNI has been registered.
   */
  protected static final ImmutableList<String> DEFERRED_STATIC_INITIALIZERS =
      ImmutableList.copyOf(
          new String[] {
            "android.graphics.FontFamily",
            "android.graphics.Path",
            "android.graphics.PathIterator",
            "android.graphics.Typeface",
            "android.graphics.text.MeasuredText$Builder",
            "android.media.ImageReader",
          });

  private TempDirectory extractDirectory;

  /**
   * Where the hyphen data was made available. This is a shared directory when it is shared, and
   * {@link #extractDirectory}'s base path otherwise.
   */
  private Path hyphenDataDirectory;

  public static void injectAndLoad() {
    // Ensure a single instance.
    synchronized (nativeRuntimeLoader) {
      if (nativeRuntimeLoader.get() == null) {
        Injector injector = new Injector.Builder(CursorWindow.class.getClassLoader()).build();
        NativeRuntimeLoader loader = injector.getInstance(NativeRuntimeLoader.class);
        nativeRuntimeLoader.set(loader);
      }
    }
    nativeRuntimeLoader.get().ensureLoaded();
  }

  /** Overridable in Android, due to private resources. */
  protected void maybeCopyExtraResources(TempDirectory dir) {
    // default to no-op
  }

  /** Overridable in Android, due to changing shadows in private branches. */
  protected List<String> getCoreClassNatives() {
    return CORE_CLASS_NATIVES;
  }

  /** Overridable in Android, due to changing shadows in private branches. */
  protected List<String> getDeferredStaticInitializers() {
    return DEFERRED_STATIC_INITIALIZERS;
  }

  /** Overridable in Android, due to changing shadows in private branches. */
  protected List<String> getGraphicsNatives() {
    return GRAPHICS_CLASS_NATIVES;
  }

  @Override
  public synchronized void ensureLoaded() {
    if (loaded.get()) {
      return;
    }

    if (!isSupported()) {
      String errorMessage =
          String.format(
              "The Robolectric native runtime is not supported on %s (%s)",
              OS_NAME.value(), OS_ARCH.value());
      throw new AssertionError(errorMessage);
    }
    loaded.set(true);

    try {
      PerfStatsCollector.getInstance()
          .measure(
              "loadNativeRuntime",
              () -> {
                extractDirectory = new TempDirectory("nativeruntime");
                hyphenDataDirectory = extractDirectory.getBasePath();

                if (VERSION.SDK_INT >= O) {
                  // Only copy fonts if graphics is supported, not just SQLite.
                  maybeCopyFonts();
                  maybeCopyHyphenData();
                }
                Map<String, String> originalProperties = new HashMap<>();
                maybeCopyIcuData();
                maybeCopyExtraResources(extractDirectory);
                if (isAndroidVOrGreater()) {
                  originalProperties = saveSystemProperties();
                  System.setProperty("use_base_native_hostruntime", "true");
                  System.setProperty(
                      "core_native_classes", String.join(",", getCoreClassNatives()));
                  System.setProperty(
                      "graphics_native_classes", String.join(",", getGraphicsNatives()));
                  System.setProperty("method_binding_format", METHOD_BINDING_FORMAT);
                }
                try {
                  loadLibrary(extractDirectory);
                } finally {
                  restoreSystemProperties(originalProperties);
                }
                String hyphenDataDir =
                    hyphenDataDirectory.resolve(HYPHEN_DATA_DIR).toFile().getAbsolutePath();
                if (isAndroidVOrGreater()) {
                  invokeDeferredStaticInitializers();
                  setNativeSystemProperty("ro.hyphen.data.dir", hyphenDataDir);
                  Typeface.loadPreinstalledSystemFontMap();
                } else {
                  System.setProperty("hyphen.data.dir", hyphenDataDir);
                }
                if (VERSION.SDK_INT >= P) {
                  Hyphenator.init();
                }
              });
    } catch (IOException e) {
      throw new AssertionError("Unable to load Robolectric native runtime library", e);
    }
  }

  /**
   * A resource carried only by the android-all archive of the SDK under test, which from
   * VANILLA_ICE_CREAM supplies the ICU data and the hyphen data.
   */
  private static URL androidAllResource() {
    return Resources.getResource("build.prop");
  }

  private static File androidAllArchive() throws IOException {
    try {
      String jarPath =
          Iterables.get(Splitter.on('!').split(androidAllResource().toURI().toString()), 0)
              .substring("jar:file:".length());
      return new File(jarPath);
    } catch (URISyntaxException syntaxException) {
      throw new IOException(syntaxException);
    }
  }

  /** The single ICU dat entry of the given android-all archive. */
  private static JarEntry icuDatEntry(JarFile jarFile) {
    List<JarEntry> found = new ArrayList<>();
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      if (entry.getName().startsWith(ICU_DIR + "/icudt") && !entry.isDirectory()) {
        found.add(entry);
      }
    }
    if (found.size() != 1) {
      throw new RuntimeException("More than one icudt file in android-all jar: " + found);
    }
    return found.get(0);
  }

  /** Attempts to load the ICU dat file. This is only relevant for native graphics. */
  private void maybeCopyIcuData() throws IOException {
    Path baseDir =
        assetDirectory(ICU_DIR, icuDataSource(), DefaultNativeRuntimeLoader::copyIcuData);
    System.setProperty("icu.data.path", icuDatPath(baseDir).toAbsolutePath().toString());
    System.setProperty("icu.locale.default", Locale.getDefault().toLanguageTag());
  }

  /**
   * A resource of the archive that supplies the ICU data, which keys the copy of it.
   *
   * <p>Up to UPSIDE_DOWN_CAKE that is the dat file itself, whose name is fixed. From
   * VANILLA_ICE_CREAM the dat file comes from the android-all archive under a name that varies with
   * its ICU version, and the only way to learn that name is to enumerate the archive, so {@code
   * build.prop} stands in for it. That keeps the enumeration off the path of a load which finds the
   * data already copied, and {@link #copyIcuData} reads the dat file out of this same archive, so
   * the copy always comes from the archive its directory is keyed by.
   */
  private static URL icuDataSource() {
    try {
      return Build.VERSION.SDK_INT <= UPSIDE_DOWN_CAKE
          ? Resources.getResource(ICU_DIR + "/icudt68l.dat")
          : androidAllResource();
    } catch (IllegalArgumentException e) {
      System.out.println("Could not load icu data file ");
      throw new RuntimeException(e);
    }
  }

  /** The ICU dat file under the given directory, which holds exactly the one. */
  private static Path icuDatPath(Path baseDir) throws IOException {
    try (Stream<Path> files = java.nio.file.Files.list(baseDir.resolve(ICU_DIR))) {
      return Iterables.getOnlyElement(ImmutableList.copyOf(files.iterator()));
    }
  }

  private static void copyIcuData(Path baseDir) throws IOException {
    Path icuPath = baseDir.resolve(ICU_DIR);
    java.nio.file.Files.createDirectories(icuPath);
    if (Build.VERSION.SDK_INT <= UPSIDE_DOWN_CAKE) {
      URL icuDatUrl = Resources.getResource(ICU_DIR + "/icudt68l.dat");
      Resources.asByteSource(icuDatUrl)
          .copyTo(Files.asByteSink(icuPath.resolve("icudt68l.dat").toFile()));
      return;
    }
    // Read the dat file straight out of the archive that keyed this directory rather than
    // resolving it by name through the classpath. The instrumented and uninstrumented android-all
    // archives both carry one, so which of them the classpath answers with depends on the load,
    // and the copy has to come from the archive the key names.
    try (JarFile jarFile = new JarFile(androidAllArchive())) {
      JarEntry icuDatEntry = icuDatEntry(jarFile);
      String fileName = Iterables.getLast(Splitter.on('/').splitToList(icuDatEntry.getName()));
      // An archive entry can name anything, including a path that climbs out of the directory it
      // is being written under, so resolve it and check it stayed put before writing.
      Path icuDatPath = icuPath.resolve(fileName).normalize();
      if (!icuDatPath.startsWith(icuPath)) {
        throw new IOException("Unexpected ICU data entry name: " + icuDatEntry.getName());
      }
      try (InputStream input = jarFile.getInputStream(icuDatEntry)) {
        java.nio.file.Files.copy(input, icuDatPath, REPLACE_EXISTING);
      }
    }
  }

  /**
   * Attempts to copy the system fonts to a temporary directory. This is only relevant for native
   * graphics.
   */
  private void maybeCopyFonts() throws IOException {
    URL fontsResource;
    URI fontsUri;
    try {
      fontsResource = Resources.getResource(FONTS_DIR + "/");
      fontsUri = fontsResource.toURI();
    } catch (IllegalArgumentException | URISyntaxException e) {
      return;
    }

    Path baseDir =
        assetDirectory(FONTS_DIR, fontsResource, directory -> copyFonts(directory, fontsUri));

    System.setProperty(
        "robolectric.nativeruntime.fontdir",
        // Android's FontListParser expects a trailing slash for the base font directory.
        baseDir.resolve(FONTS_DIR).toAbsolutePath() + File.separator);
  }

  private static void copyFonts(Path baseDir, URI fontsUri) throws IOException {
    FileSystem zipfs = null;

    if ("jar".equals(fontsUri.getScheme())) {
      zipfs = FileSystems.newFileSystem(fontsUri, ImmutableMap.of("create", "true"));
    }

    Path fontsInputPath = Paths.get(fontsUri);
    java.nio.file.Files.createDirectories(baseDir.resolve(FONTS_DIR));

    try (Stream<Path> pathStream = java.nio.file.Files.walk(fontsInputPath)) {
      Iterator<Path> fileIterator = pathStream.iterator();
      while (fileIterator.hasNext()) {
        Path path = fileIterator.next();
        // Avoid copying parent directory.
        if (FONTS_DIR.equals(path.getFileName().toString())) {
          continue;
        }
        String fontPath = FONTS_DIR + "/" + path.getFileName();
        URL resource = Resources.getResource(fontPath);
        Path outputPath = baseDir.resolve(fontPath);
        Resources.asByteSource(resource).copyTo(Files.asByteSink(outputPath.toFile()));
      }
    }
    if (zipfs != null) {
      zipfs.close();
    }
  }

  /**
   * Attempts to copy the hyphen data to a temporary directory. This is only relevant for native
   * graphics.
   */
  private void maybeCopyHyphenData() throws IOException {
    URL hyphenDataResource;
    URI hyphenDataUri;
    try {
      hyphenDataResource = Resources.getResource(HYPHEN_DATA_DIR + "/");
      hyphenDataUri = hyphenDataResource.toURI();
    } catch (IllegalArgumentException | URISyntaxException e) {
      Logger.info("Could not load hyphen data files: " + e.getMessage());
      return;
    }

    hyphenDataDirectory =
        assetDirectory(
            HYPHEN_DATA_DIR,
            hyphenDataResource,
            directory -> copyHyphenData(directory, hyphenDataUri));
  }

  private static void copyHyphenData(Path baseDir, URI hyphenDataUri) throws IOException {
    FileSystem zipfs = null;

    if ("jar".equals(hyphenDataUri.getScheme())) {
      zipfs = FileSystems.newFileSystem(hyphenDataUri, ImmutableMap.of("create", "true"));
    }

    Path hyphenDataInputPath = Paths.get(hyphenDataUri);
    java.nio.file.Files.createDirectories(baseDir.resolve(HYPHEN_DATA_DIR));

    try (Stream<Path> pathStream = java.nio.file.Files.walk(hyphenDataInputPath)) {
      Iterator<Path> fileIterator = pathStream.iterator();
      while (fileIterator.hasNext()) {
        Path path = fileIterator.next();
        // Avoid copying parent directory.
        if (Objects.equals(path.getFileName().toString(), HYPHEN_DATA_DIR)) {
          continue;
        }
        String hyphenDataPath = HYPHEN_DATA_DIR + "/" + path.getFileName();
        URL resource = Resources.getResource(hyphenDataPath);
        Path outputPath = baseDir.resolve(hyphenDataPath);
        Resources.asByteSource(resource).copyTo(Files.asByteSink(outputPath.toFile()));
      }
    }
    if (zipfs != null) {
      zipfs.close();
    }
  }

  /** Copies a group of the native runtime's data assets into the given directory. */
  private interface AssetCopier {
    void copyTo(Path directory) throws IOException;
  }

  /**
   * Makes the named group of data assets available and returns the directory holding it. The group
   * is shared with other loads where possible, and copied into this instance's own temporary
   * directory otherwise.
   */
  private Path assetDirectory(String group, URL source, AssetCopier copier) throws IOException {
    Path shared = sharedAssetDirectory(group, source, copier);
    if (shared != null) {
      return shared;
    }
    Path own = extractDirectory.getBasePath();
    copier.copyTo(own);
    return own;
  }

  /**
   * Returns a directory shared with other loads that holds the named group of data assets, copying
   * them into it if they are not there yet, or {@code null} if the group has to be copied per
   * instance instead.
   *
   * <p>Each group is keyed by the source that supplies it rather than by a single archive for all
   * of them, because they do not all come from the same place: the fonts, and the ICU data up to
   * UPSIDE_DOWN_CAKE, come from the nativeruntime dist archive, which is the same whatever the SDK
   * under test, while from VANILLA_ICE_CREAM the ICU data and the hyphen data come from that SDK's
   * android-all archive. Keying every group on one archive would hand a directory populated for one
   * SDK to a load running at another, which then points icu.data.path at an ICU dat file that was
   * never copied, since that file is named after its ICU version. Keying the groups separately also
   * keeps the fonts, by far the largest group, to a single copy across every SDK.
   *
   * <p>A lock file is held across the copy so that concurrent test JVMs (a build tool will
   * typically run several) cooperate rather than each writing the same files, and the marker file
   * is only written once the copy has finished, so a partially populated directory is never used.
   */
  private Path sharedAssetDirectory(String group, URL source, AssetCopier copier) {
    if (!Boolean.parseBoolean(System.getProperty(CACHE_ASSETS_PROPERTY, "true"))) {
      return null;
    }
    Path directory;
    try {
      String identity = archiveIdentity(source);
      if (identity == null) {
        Logger.info(
            "Not sharing native runtime %s: %s is not supplied by an archive", group, source);
        return null;
      }
      String key =
          Hashing.sha256().hashString(identity, StandardCharsets.UTF_8).toString().substring(0, 32);
      directory =
          Paths.get(System.getProperty("java.io.tmpdir"), ASSET_CACHE_DIR_NAME, group + "-" + key);
    } catch (RuntimeException e) {
      Logger.info("Unable to determine native runtime asset cache directory: " + e.getMessage());
      return null;
    }
    Path marker = directory.resolve(ASSET_CACHE_MARKER);
    if (java.nio.file.Files.exists(marker)) {
      return directory;
    }
    Path lockFile = directory.resolveSibling(directory.getFileName() + ".lock");
    try {
      java.nio.file.Files.createDirectories(directory.getParent());
      try (RandomAccessFile lockHandle = new RandomAccessFile(lockFile.toFile(), "rw");
          FileChannel channel = lockHandle.getChannel();
          FileLock lock = channel.lock()) {
        // Another process may have finished while this one waited for the lock.
        if (java.nio.file.Files.exists(marker)) {
          return directory;
        }
        java.nio.file.Files.createDirectories(directory);
        copier.copyTo(directory);
        java.nio.file.Files.createFile(marker);
        Logger.info("Copied native runtime %s to %s", group, directory);
        return directory;
      }
    } catch (IOException | RuntimeException e) {
      // Fall back to copying into this instance's temporary directory.
      Logger.info("Unable to share native runtime " + group + ": " + e.getMessage());
      return null;
    }
  }

  /**
   * Identifies the archive that supplies the given resource by its location plus its size and
   * modification time, so that a rebuilt archive is not mistaken for a cached one. Returns {@code
   * null} if no archive supplies it.
   *
   * <p>These assets only ever arrive in a jar: the fonts and the ICU data up to UPSIDE_DOWN_CAKE
   * from the published nativeruntime dist archive, which ships prebuilt rather than being built
   * from source, and from VANILLA_ICE_CREAM the ICU and hyphen data from an android-all archive. A
   * resource sitting on the classpath unpacked could be edited in place without its location ever
   * changing, and nothing as cheap identifies it, so rather than key a shared directory on
   * something that stale it is not shared at all.
   */
  private static String archiveIdentity(URL resource) {
    if ("jar".equals(resource.getProtocol())) {
      String jarPath = Iterables.get(Splitter.on('!').split(resource.toString()), 0);
      String filePrefix = "jar:file:";
      if (jarPath.startsWith(filePrefix)) {
        File jarFile = new File(jarPath.substring(filePrefix.length()));
        if (jarFile.isFile()) {
          return jarFile.getAbsolutePath() + ":" + jarFile.length() + ":" + jarFile.lastModified();
        }
      }
    }
    return null;
  }

  private void loadLibrary(TempDirectory tempDirectory) throws IOException {
    Path libraryPath = tempDirectory.getBasePath().resolve(libraryName());
    URL libraryResource = Resources.getResource(nativeLibraryPath());
    Logger.info("Loading android native library from: %s", libraryResource);
    Resources.asByteSource(libraryResource).copyTo(Files.asByteSink(libraryPath.toFile()));
    System.load(libraryPath.toAbsolutePath().toString());
  }

  private static boolean isSupported() {
    return (OsUtil.isMac()
            && (Objects.equals(arch(), "aarch64") || Objects.equals(arch(), "x86_64")))
        || (OsUtil.isLinux() && Objects.equals(arch(), "x86_64"))
        || (OsUtil.isWindows() && Objects.equals(arch(), "x86_64"));
  }

  private static String nativeLibraryPath() {
    return String.format("native/%s/%s/%s", osName(), arch(), libraryName());
  }

  protected static String libraryName() {
    if (isAndroidVOrGreater()) {
      // For V and above, hwui's android_graphics_HardwareRenderer.cpp has shared library symbol
      // lookup logic that assumes that Windows library name is "libandroid_runtime.dll".
      return System.mapLibraryName(OsUtil.isWindows() ? "libandroid_runtime" : "android_runtime");
    } else {
      return System.mapLibraryName("robolectric-nativeruntime");
    }
  }

  private static String osName() {
    if (OsUtil.isLinux()) {
      return "linux";
    } else if (OsUtil.isMac()) {
      return "mac";
    } else if (OsUtil.isWindows()) {
      return "windows";
    }
    return "unknown";
  }

  private static String arch() {
    String arch = OS_ARCH.value().toLowerCase(Locale.US);
    if (arch.equals("x86_64") || arch.equals("amd64")) {
      return "x86_64";
    }
    return arch;
  }

  @VisibleForTesting
  static boolean isLoaded() {
    return loaded.get();
  }

  @VisibleForTesting
  Path getDirectory() {
    return extractDirectory == null ? null : extractDirectory.getBasePath();
  }

  /** The directory the hyphen data was made available in. */
  @VisibleForTesting
  Path getHyphenDataDirectory() {
    return hyphenDataDirectory;
  }

  @VisibleForTesting
  static void resetLoaded() {
    loaded.set(false);
  }

  protected void invokeDeferredStaticInitializers() {
    for (String className : DEFERRED_STATIC_INITIALIZERS) {
      ReflectionHelpers.callStaticMethod(
          Shadow.class.getClassLoader(), className, "__staticInitializer__");
    }
  }

  private static boolean isAndroidVOrGreater() {
    return VERSION.SDK_INT >= VANILLA_ICE_CREAM;
  }

  private Map<String, String> saveSystemProperties() {
    Map<String, String> originalProperties = new HashMap<>();
    for (String property : PROPERTIES_TO_RESTORE) {
      originalProperties.put(property, System.getProperty(property));
    }
    return originalProperties;
  }

  private void restoreSystemProperties(Map<String, String> originalProperties) {
    for (Map.Entry<String, String> entry : originalProperties.entrySet()) {
      if (entry.getValue() == null) {
        System.clearProperty(entry.getKey());
      } else {
        System.setProperty(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Sets a system property in native code. This is required to communicate certain pieces of
   * configuration data to native code, such as the hyphenation data directory for Android V+.
   */
  protected static void setNativeSystemProperty(String key, String value) {
    String nativeSetMethodName =
        Shadow.directNativeMethodName("android.os.SystemProperties", "native_set");
    ReflectionHelpers.callStaticMethod(
        Shadow.class.getClassLoader(),
        "android.os.SystemProperties",
        nativeSetMethodName,
        ClassParameter.from(String.class, key),
        ClassParameter.from(String.class, value));
  }
}
