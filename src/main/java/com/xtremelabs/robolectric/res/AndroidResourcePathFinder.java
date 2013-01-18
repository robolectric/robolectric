package com.xtremelabs.robolectric.res;

import android.R;
import com.xtremelabs.robolectric.util.PropertiesHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

public class AndroidResourcePathFinder {
    private final int sdkVersion;
    private final ResourcePath resourcePath;

    public static ResourcePath getSystemResourcePath(int sdkVersion, ResourcePath resourcePath) {
        String pathToAndroidResources = new AndroidResourcePathFinder(sdkVersion, resourcePath).getPathToAndroidResources();
        return new ResourcePath(R.class, new File(pathToAndroidResources), null);
    }

    public AndroidResourcePathFinder(int sdkVersion, ResourcePath resourcePath) {
        this.resourcePath = resourcePath == null ? new ResourcePath(null, new File("."), null) : resourcePath;
        this.sdkVersion = sdkVersion;
    }

    String getPathToAndroidResources() {
        String resourcePath;
        if ((resourcePath = getAndroidResourcePathFromLocalProperties()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemEnvironment()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemProperty()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathByExecingWhichAndroid()) != null) {
            return resourcePath;
        }

        System.out.println("WARNING: Unable to find path to Android SDK");
        return null;
    }

    private String getAndroidResourcePathFromLocalProperties() {
        // Hand tested
        // This is the path most often taken by IntelliJ
        File rootDir = resourcePath.resourceBase.getParentFile();
        String localPropertiesFileName = "local.properties";
        File localPropertiesFile = new File(rootDir, localPropertiesFileName);
        if (!localPropertiesFile.exists()) {
            localPropertiesFile = new File(localPropertiesFileName);
        }
        if (localPropertiesFile.exists()) {
            Properties localProperties = new Properties();
            try {
                localProperties.load(new FileInputStream(localPropertiesFile));
                PropertiesHelper.doSubstitutions(localProperties);
                String sdkPath = localProperties.getProperty("sdk.dir");
                if (sdkPath != null) {
                    return getResourcePathFromSdkPath(sdkPath);
                }
            } catch (IOException e) {
                // fine, we'll try something else
            }
        }
        return null;
    }

    private String getAndroidResourcePathFromSystemEnvironment() {
        // Hand tested
        String resourcePath = System.getenv().get("ANDROID_HOME");
        if (resourcePath != null) {
            return new File(resourcePath, getAndroidResourceSubPath()).toString();
        }
        return null;
    }

    private String getAndroidResourcePathFromSystemProperty() {
        // this is used by the android-maven-plugin
        String resourcePath = System.getProperty("android.sdk.path");
        if (resourcePath != null) {
            return new File(resourcePath, getAndroidResourceSubPath()).toString();
        }
        return null;
    }

    private String getAndroidResourcePathByExecingWhichAndroid() {
        // Hand tested
        // Should always work from the command line. Often fails in IDEs because
        // they don't pass the full PATH in the environment
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "android"});
            String sdkPath = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (sdkPath != null && sdkPath.endsWith("tools/android")) {
                return getResourcePathFromSdkPath(sdkPath.substring(0, sdkPath.indexOf("tools/android")));
            }
        } catch (IOException e) {
            // fine we'll try something else
        }
        return null;
    }

    private String getResourcePathFromSdkPath(String sdkPath) {
        File androidResourcePath = new File(sdkPath, getAndroidResourceSubPath());
        return androidResourcePath.exists() ? androidResourcePath.toString() : null;
    }

    private String getAndroidResourceSubPath() {
        return "platforms/android-" + sdkVersion + "/data/res";
    }
}
