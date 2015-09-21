package org.robolectric;

import android.app.Application;
import android.content.pm.PackageManager;

import org.robolectric.res.builder.RobolectricPackageManager;

public class RuntimeEnvironment {
  public static Application application;

  private volatile static Thread mainThread = Thread.currentThread();
  private static String qualifiers;
  private static Object activityThread;
  private static RobolectricPackageManager packageManager;
  private static int apiLevel;

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

  public static PackageManager getPackageManager() {
    return (PackageManager) packageManager;
  }

  public static RobolectricPackageManager getRobolectricPackageManager() {
    return packageManager;
  }

  public static void setRobolectricPackageManager(RobolectricPackageManager newPackageManager) {
    if (packageManager != null) {
      packageManager.reset();
    }
    packageManager = newPackageManager;
  }

  public static String getQualifiers() {
    return qualifiers;
  }

  public static void setQualifiers(String newQualifiers) {
    qualifiers = newQualifiers;
  }

  public static void setApiLevel(int level) {
    apiLevel = level;
  }

  public static int getApiLevel() {
    return apiLevel;
  }
}
