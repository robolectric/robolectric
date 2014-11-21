package org.robolectric;

import android.app.Application;
import android.content.pm.PackageManager;
import org.robolectric.res.builder.IRobolectricPackageManager;

public class RuntimeEnvironment {
    public static String qualifiers;
    public static Object activityThread;
    public static Application application;
    public static IRobolectricPackageManager packageManager;

    public static PackageManager getPackageManager() {
        return (PackageManager) packageManager;
    }

    public static String getQualifiers() {
        return qualifiers;
    }

    public static void setQualifiers(String newQualifiers) {
        qualifiers = newQualifiers;
    }
}
