package org.robolectric;

import android.app.Application;
import android.content.pm.PackageManager;

import org.robolectric.res.builder.RobolectricPackageManager;

public class RuntimeEnvironment {
    public static Application application;

    private static String qualifiers;
    private static Object activityThread;
    private static RobolectricPackageManager packageManager;
    private static int apiLevel;

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
