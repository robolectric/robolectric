package org.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLooper.assertLooperMode;

import android.app.ActivityThread;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import androidx.test.platform.app.InstrumentationRegistry;
import java.nio.file.Path;
import org.robolectric.android.Bootstrap;
import org.robolectric.android.ConfigurationV25;
import org.robolectric.res.ResourceTable;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

public class RuntimeEnvironment {
  /**
   * @deprecated Use {@link #getApplication} or {@link
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext} instead. Note that unlike
   *     the alternatives, this field is inherently incompatible with {@link
   *     org.robolectric.annotation.LazyLoadApplication}. This field may be removed in a later
   *     release
   */
  @Deprecated public static Context systemContext;

  /**
   * @deprecated Please migrate to {@link
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext}
   */
  @Deprecated public static Application application;

  private static volatile Thread mainThread;
  private static Object activityThread;
  private static int apiLevel;
  private static Scheduler masterScheduler;
  private static ResourceTable systemResourceTable;
  private static ResourceTable appResourceTable;
  private static ResourceTable compileTimeResourceTable;
  private static TempDirectory tempDirectory = new TempDirectory("no-test-yet");
  private static Path androidFrameworkJar;
  public static Path compileTimeSystemResourcesFile;

  private static boolean useLegacyResources;

  /**
   * Get a reference to the {@link Application} under test.
   *
   * The Application may be created a test setup time or created lazily at call time, based on the
   * test's {@link LazyLoadApplication) setting. If lazy loading is enabled, this method must be
   * called on the main/test thread.
   *
   * An alternate API is
   * {@link androidx.test.core.app.ApplicationProvider#getApplicationContext()}, which is
   * preferable if you desire cross platform tests that work on the JVM and real Android devices.
   */
  public static Application getApplication() {
    if (application == null) {
      application =
          (Application)
              InstrumentationRegistry.getInstrumentation()
                  .getTargetContext()
                  .getApplicationContext();
    }
    return application;
  }

  private static Class<? extends Application> applicationClass;

  public static Class<? extends Application> getConfiguredApplicationClass() {
    return applicationClass;
  }

  public static void setConfiguredApplicationClass(Class<? extends Application> clazz) {
    applicationClass = clazz;
  }

  /**
   * Tests if the given thread is currently set as the main thread.
   *
   * @param thread the thread to test.
   * @return true if the specified thread is the main thread, false otherwise.
   * @see #isMainThread()
   */
  public static boolean isMainThread(Thread thread) {
    assertLooperMode(LEGACY);
    return thread == mainThread;
  }

  /**
   * Tests if the current thread is currently set as the main thread.
   *
   * <p>Not supported in realistic looper mode.
   *
   * @return true if the current thread is the main thread, false otherwise.
   */
  public static boolean isMainThread() {
    assertLooperMode(LEGACY);
    return isMainThread(Thread.currentThread());
  }

  /**
   * Retrieves the main thread. The main thread is the thread to which the main looper is attached.
   * Defaults to the thread that initialises the {@link RuntimeEnvironment} class.
   *
   * <p>Not supported in realistic looper mode.
   *
   * @return The main thread.
   * @see #setMainThread(Thread)
   * @see #isMainThread()
   */
  public static Thread getMainThread() {
    assertLooperMode(LEGACY);
    return mainThread;
  }

  /**
   * Sets the main thread. The main thread is the thread to which the main looper is attached.
   * Defaults to the thread that initialises the {@link RuntimeEnvironment} class.
   *
   * <p>Not supported in realistic looper mode.
   *
   * @param newMainThread the new main thread.
   * @see #setMainThread(Thread)
   * @see #isMainThread()
   */
  public static void setMainThread(Thread newMainThread) {
    assertLooperMode(LEGACY);
    mainThread = newMainThread;
  }

  public static Object getActivityThread() {
    if (activityThread == null) {
      activityThread = ReflectionHelpers.newInstance(ActivityThread.class);
    }
    return activityThread;
  }

  public static void setActivityThread(Object newActivityThread) {
    activityThread = newActivityThread;
  }

  /**
   * Returns a qualifier string describing the current {@link Configuration} of the system
   * resources.
   *
   * @return a qualifier string as described
   *     (https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules)[here].
   */
  public static String getQualifiers() {
    Resources systemResources = Resources.getSystem();
    return getQualifiers(systemResources.getConfiguration(), systemResources.getDisplayMetrics());
  }

  /**
   * Returns a qualifier string describing the given configuration and display metrics.
   *
   * @param configuration the configuration.
   * @param displayMetrics the display metrics.
   * @return a qualifier string as described
   *     (https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules)[here].
   */
  public static String getQualifiers(Configuration configuration, DisplayMetrics displayMetrics) {
    return ConfigurationV25.resourceQualifierString(configuration, displayMetrics);
  }

  /**
   * Overrides the current device configuration.
   *
   * <p>If {@param newQualifiers} starts with a plus ('+'), the prior configuration is used as the
   * base configuration, with the given changes applied additively. Otherwise, default values are
   * used for unspecified properties, as described <a
   * href="http://robolectric.org/device-configuration/">here</a>.
   *
   * @param newQualifiers the qualifiers to apply
   */
  public static void setQualifiers(String newQualifiers) {
    Configuration configuration;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    if (newQualifiers.startsWith("+")) {
      configuration = new Configuration(Resources.getSystem().getConfiguration());
      displayMetrics.setTo(Resources.getSystem().getDisplayMetrics());
    } else {
      configuration = new Configuration();
    }
    Bootstrap.applyQualifiers(newQualifiers, getApiLevel(), configuration, displayMetrics);

    Resources systemResources = Resources.getSystem();
    systemResources.updateConfiguration(configuration, displayMetrics);

    if (getApplication() != null) {
      getApplication().getResources().updateConfiguration(configuration, displayMetrics);
    }
  }

  public static int getApiLevel() {
    return apiLevel;
  }

  public static Number castNativePtr(long ptr) {
    // Weird, using a ternary here doesn't work, there's some auto promotion of boxed types
    // happening.
    if (getApiLevel() >= LOLLIPOP) {
      return ptr;
    } else {
      return (int) ptr;
    }
  }

  /**
   * Retrieves the current master scheduler. This scheduler is always used by the main {@link
   * android.os.Looper Looper}, and if the global scheduler option is set it is also used for the
   * background scheduler and for all other {@link android.os.Looper Looper}s
   *
   * @return The current master scheduler.
   * @see #setMasterScheduler(Scheduler) see
   *     org.robolectric.Robolectric#getForegroundThreadScheduler() see
   *     org.robolectric.Robolectric#getBackgroundThreadScheduler()
   */
  public static Scheduler getMasterScheduler() {
    return masterScheduler;
  }

  /**
   * Sets the current master scheduler. See {@link #getMasterScheduler()} for details. Note that
   * this method is primarily intended to be called by the Robolectric core setup code. Changing the
   * master scheduler during a test will have unpredictable results.
   *
   * @param masterScheduler the new master scheduler.
   * @see #getMasterScheduler() see org.robolectric.Robolectric#getForegroundThreadScheduler() see
   *     org.robolectric.Robolectric#getBackgroundThreadScheduler()
   */
  public static void setMasterScheduler(Scheduler masterScheduler) {
    RuntimeEnvironment.masterScheduler = masterScheduler;
  }

  public static void setSystemResourceTable(ResourceTable systemResourceTable) {
    RuntimeEnvironment.systemResourceTable = systemResourceTable;
  }

  public static void setAppResourceTable(ResourceTable appResourceTable) {
    RuntimeEnvironment.appResourceTable = appResourceTable;
  }

  public static ResourceTable getSystemResourceTable() {
    return systemResourceTable;
  }

  public static ResourceTable getAppResourceTable() {
    return appResourceTable;
  }

  public static void setCompileTimeResourceTable(ResourceTable compileTimeResourceTable) {
    RuntimeEnvironment.compileTimeResourceTable = compileTimeResourceTable;
  }

  public static ResourceTable getCompileTimeResourceTable() {
    return compileTimeResourceTable;
  }

  public static void setTempDirectory(TempDirectory tempDirectory) {
    RuntimeEnvironment.tempDirectory = tempDirectory;
  }

  public static TempDirectory getTempDirectory() {
    return tempDirectory;
  }

  public static void setAndroidFrameworkJarPath(Path localArtifactPath) {
    RuntimeEnvironment.androidFrameworkJar = localArtifactPath;
  }

  public static Path getAndroidFrameworkJarPath() {
    return RuntimeEnvironment.androidFrameworkJar;
  }

  /**
   * Internal only.
   *
   * @deprecated Do not use.
   */
  @Deprecated
  public static boolean useLegacyResources() {
    return useLegacyResources;
  }

  /**
   * Internal only.
   *
   * @deprecated Do not use.
   */
  @Deprecated
  public static void setUseLegacyResources(boolean useLegacyResources) {
    RuntimeEnvironment.useLegacyResources = useLegacyResources;
  }
}
