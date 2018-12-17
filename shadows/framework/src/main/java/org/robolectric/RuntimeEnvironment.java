package org.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import org.robolectric.android.Bootstrap;
import org.robolectric.android.ConfigurationV25;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceTable;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TempDirectory;

public class RuntimeEnvironment {
  public static Context systemContext;

  /**
   * @deprecated Please migrate to {@link
   *     androidx.test.core.app.ApplicationProvider#getApplicationContext}
   */
  @Deprecated public static Application application;

  private volatile static Thread mainThread = Thread.currentThread();
  private static Object activityThread;
  private static int apiLevel;
  private static Scheduler masterScheduler;
  private static ResourceTable systemResourceTable;
  private static ResourceTable appResourceTable;
  private static ResourceTable compileTimeResourceTable;
  private static TempDirectory tempDirectory = new TempDirectory("no-test-yet");
  private static String androidFrameworkJar;
  public static FsFile compileTimeSystemResourcesFile;

  private static boolean useLegacyResources;

  /**
   * Tests if the given thread is currently set as the main thread.
   *
   * @param thread the thread to test.
   * @return <tt>true</tt> if the specified thread is the main thread, <tt>false</tt> otherwise.
   * @see #isMainThread()
   */
  public static boolean isMainThread(Thread thread) {
    return thread == mainThread;
  }

  /**
   * Tests if the current thread is currently set as the main thread.
   *
   * @return <tt>true</tt> if the current thread is the main thread, <tt>false</tt> otherwise.
   */
  public static boolean isMainThread() {
    return isMainThread(Thread.currentThread());
  }

  /**
   * Retrieves the main thread. The main thread is the thread to which the main looper is attached.
   * Defaults to the thread that initialises the <tt>RuntimeEnvironment</tt> class.
   *
   * @return The main thread.
   * @see #setMainThread(Thread)
   * @see #isMainThread()
   */
  public static Thread getMainThread() {
    return mainThread;
  }

  /**
   * Sets the main thread. The main thread is the thread to which the main looper is attached.
   * Defaults to the thread that initialises the <tt>RuntimeEnvironment</tt> class.
   *
   * @param newMainThread the new main thread.
   * @see #setMainThread(Thread)
   * @see #isMainThread()
   */
  public static void setMainThread(Thread newMainThread) {
    mainThread = newMainThread;
  }

  public static Object getActivityThread() {
    return activityThread;
  }

  public static void setActivityThread(Object newActivityThread) {
    activityThread = newActivityThread;
  }

  /**
   * Returns a qualifier string describing the current {@link Configuration} of the system resources.
   *
   * @return a qualifier string as described (https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules)[here].
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
   * @return a qualifier string as described (https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules)[here].
   */
  public static String getQualifiers(Configuration configuration, DisplayMetrics displayMetrics) {
    return ConfigurationV25.resourceQualifierString(configuration, displayMetrics);
  }

  /**
   * Overrides the current device configuration.
   *
   * If `newQualifiers` starts with a plus (`+`), the prior configuration is used as the base
   * configuration, with the given changes applied additively. Otherwise, default values are used
   * for unspecified properties, as described [here](http://robolectric.org/device-configuration/).
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

    if (application != null) {
      application.getResources().updateConfiguration(configuration, displayMetrics);
    }
  }

  public static int getApiLevel() {
    return apiLevel;
  }

  public static Number castNativePtr(long ptr) {
    // Weird, using a ternary here doesn't work, there's some auto promotion of boxed types happening.
    if (getApiLevel() >= LOLLIPOP) {
      return ptr;
    } else {
      return (int) ptr;
    }
  }

  /**
   * Retrieves the current master scheduler. This scheduler is always used by the main
   * {@link android.os.Looper Looper}, and if the global scheduler option is set it is also used for
   * the background scheduler and for all other {@link android.os.Looper Looper}s
   * @return The current master scheduler.
   * @see #setMasterScheduler(Scheduler)
   * see org.robolectric.Robolectric#getForegroundThreadScheduler()
   * see org.robolectric.Robolectric#getBackgroundThreadScheduler()
   */
  public static Scheduler getMasterScheduler() {
    return masterScheduler;
  }

  /**
   * Sets the current master scheduler. See {@link #getMasterScheduler()} for details.
   * Note that this method is primarily intended to be called by the Robolectric core setup code.
   * Changing the master scheduler during a test will have unpredictable results.
   * @param masterScheduler the new master scheduler.
   * @see #getMasterScheduler()
   * see org.robolectric.Robolectric#getForegroundThreadScheduler()
   * see org.robolectric.Robolectric#getBackgroundThreadScheduler()
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

  public static void setAndroidFrameworkJarPath(String localArtifactPath) {
    RuntimeEnvironment.androidFrameworkJar = localArtifactPath;
  }

  public static String getAndroidFrameworkJarPath() {
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
