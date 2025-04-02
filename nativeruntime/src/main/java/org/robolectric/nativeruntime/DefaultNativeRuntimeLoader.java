package org.robolectric.nativeruntime;

import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;

import android.database.CursorWindow;
import android.graphics.Typeface;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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
import org.robolectric.util.TempDirectory;
import org.robolectric.util.inject.Injector;
import org.robolectric.versioning.AndroidVersions;

/** Loads the Robolectric native runtime. */
@AutoService(NativeRuntimeLoader.class)
@Priority(Integer.MIN_VALUE)
public class DefaultNativeRuntimeLoader implements NativeRuntimeLoader {
  protected static final AtomicBoolean loaded = new AtomicBoolean(false);

  private static final AtomicReference<NativeRuntimeLoader> nativeRuntimeLoader =
      new AtomicReference<>();

  protected static final String METHOD_BINDING_FORMAT = "$$robo$$${method}$nativeBinding";

  // Core classes for which native methods are to be registered for Android V and above.
  protected static final ImmutableList<String> CORE_CLASS_NATIVES =
      ImmutableList.copyOf(
          new String[] {
            "android.animation.PropertyValuesHolder",
            "android.database.CursorWindow",
            "android.database.sqlite.SQLiteConnection",
            "android.database.sqlite.SQLiteRawStatement",
            "android.media.ImageReader",
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
                if (AndroidVersions.CURRENT.getSdkInt() >= AndroidVersions.O.SDK_INT) {
                  // Only copy fonts if graphics is supported, not just SQLite.
                  maybeCopyFonts(extractDirectory);
                }
                maybeCopyIcuData(extractDirectory);
                maybeCopyExtraResources(extractDirectory);
                if (isAndroidVOrGreater()) {
                  System.setProperty(
                      "core_native_classes", String.join(",", getCoreClassNatives()));
                  System.setProperty(
                      "graphics_native_classes", String.join(",", getGraphicsNatives()));
                  System.setProperty("method_binding_format", METHOD_BINDING_FORMAT);
                }
                loadLibrary(extractDirectory);
                if (isAndroidVOrGreater()) {
                  invokeDeferredStaticInitializers();
                  Typeface.loadPreinstalledSystemFontMap();
                }
              });
    } catch (IOException e) {
      throw new AssertionError("Unable to load Robolectric native runtime library", e);
    }
  }

  private static List<String> getResourcesInAndroidAll(String prefix) throws IOException {
    try {
      String jarPath =
          Iterables.get(
                  Splitter.on('!').split(Resources.getResource("build.prop").toURI().toString()), 0)
              .substring("jar:file:".length());
      List<String> resources = new ArrayList<>();
      try (JarFile jarFile = new JarFile(jarPath)) {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          if (entry.getName().startsWith(prefix) && !entry.isDirectory()) {
            resources.add(entry.getName());
          }
        }
      }
      return resources;
    } catch (URISyntaxException syntaxException) {
      throw new IOException(syntaxException);
    }
  }

  /** Attempts to load the ICU dat file. This is only relevant for native graphics. */
  private void maybeCopyIcuData(TempDirectory tempDirectory) throws IOException {
    URL icuDatUrl;
    try {
      if (AndroidVersions.CURRENT.getSdkInt() <= AndroidVersions.U.SDK_INT) {
        icuDatUrl = Resources.getResource("icu/icudt68l.dat");
      } else {
        List<String> resources = getResourcesInAndroidAll("icu/icudt");
        if (resources.size() != 1) {
          throw new RuntimeException("More than one icudt file in android-all jar: " + resources);
        } else {
          icuDatUrl = Resources.getResource(resources.get(0));
        }
      }
    } catch (IllegalArgumentException e) {
      System.out.println("Could not load icu data file ");
      throw new RuntimeException(e);
    }
    Path icuPath = tempDirectory.create("icu");
    Path icuDatPath;
    if (AndroidVersions.CURRENT.getSdkInt() <= AndroidVersions.U.SDK_INT) {
      icuDatPath = icuPath.resolve("icudt68l.dat");
    } else {
      List<String> parts = Splitter.on('/').splitToList(icuDatUrl.toString());
      icuDatPath = icuPath.resolve(Iterables.getLast(parts));
    }
    Resources.asByteSource(icuDatUrl).copyTo(Files.asByteSink(icuDatPath.toFile()));
    System.setProperty("icu.data.path", icuDatPath.toAbsolutePath().toString());
    System.setProperty("icu.locale.default", Locale.getDefault().toLanguageTag());
  }

  /**
   * Attempts to copy the system fonts to a temporary directory. This is only relevant for native
   * graphics.
   */
  private void maybeCopyFonts(TempDirectory tempDirectory) throws IOException {
    URI fontsUri;
    try {
      fontsUri = Resources.getResource("fonts/").toURI();
    } catch (IllegalArgumentException | URISyntaxException e) {
      return;
    }

    FileSystem zipfs = null;

    if ("jar".equals(fontsUri.getScheme())) {
      zipfs = FileSystems.newFileSystem(fontsUri, ImmutableMap.of("create", "true"));
    }

    Path fontsInputPath = Paths.get(fontsUri);
    Path fontsOutputPath = tempDirectory.create("fonts");

    try (Stream<Path> pathStream = java.nio.file.Files.walk(fontsInputPath)) {
      Iterator<Path> fileIterator = pathStream.iterator();
      while (fileIterator.hasNext()) {
        Path path = fileIterator.next();
        // Avoid copying parent directory.
        if ("fonts".equals(path.getFileName().toString())) {
          continue;
        }
        String fontPath = "fonts/" + path.getFileName();
        URL resource = Resources.getResource(fontPath);
        Path outputPath = tempDirectory.getBasePath().resolve(fontPath);
        Resources.asByteSource(resource).copyTo(Files.asByteSink(outputPath.toFile()));
      }
    }
    System.setProperty(
        "robolectric.nativeruntime.fontdir",
        // Android's FontListParser expects a trailing slash for the base font directory.
        fontsOutputPath.toAbsolutePath() + File.separator);
    if (zipfs != null) {
      zipfs.close();
    }
  }

  private void loadLibrary(TempDirectory tempDirectory) throws IOException {
    Path libraryPath = tempDirectory.getBasePath().resolve(libraryName());
    URL libraryResource = Resources.getResource(nativeLibraryPath());
    Logger.info("Loading android native library from: " + libraryResource);
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
    return AndroidVersions.CURRENT.getSdkInt() >= AndroidVersions.V.SDK_INT;
  }
}
