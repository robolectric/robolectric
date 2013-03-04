package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.util.PropertiesHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;

import static org.robolectric.util.Util.file;

public class AndroidSdkFinder {
    public static final String ANDROID_SDK_HELP_TEXT = "See http://pivotal.github.com/robolectric/resources.html#unable_to_find_android_sdk for more info.";
    public static final String LOCAL_PROPERTIES_FILE_NAME = "local.properties";

    private final File androidSdkBaseDir;

    public AndroidSdkFinder() {
        this.androidSdkBaseDir = getPathToAndroidResources();
    }

    private File getPathToAndroidResources() {
        File file;
        if ((file = getAndroidResourcePathFromLocalProperties()) != null) {
            return file;
        }
        if ((file = getAndroidResourcePathFromSystemEnvironment()) != null) {
            return file;
        }
        if ((file = getAndroidResourcePathFromSystemProperty()) != null) {
            return file;
        }
        if ((file = getAndroidResourcePathByExecingWhichAndroid()) != null) {
            return file;
        }
        return null;
    }

    public ResourcePath findSystemResourcePath() {
        verifySdkFound();
        return getNewestSdkResourcePath();
    }

    public ResourcePath findSystemResourcePath(int version) {
        verifySdkFound();
        return new ResourcePath(android.R.class, file(androidSdkBaseDir, "platforms", "android-" + version, "data", "res"), null);
    }

    private void verifySdkFound() {
        if (androidSdkBaseDir == null)
            throw new RuntimeException("Unable to find Android SDK. " + ANDROID_SDK_HELP_TEXT);

        if (!androidSdkBaseDir.isDirectory())
            throw new RuntimeException("Unable to find Android SDK: " + androidSdkBaseDir.getAbsolutePath() + " is not a directory. " + ANDROID_SDK_HELP_TEXT);
    }

    private File getAndroidResourcePathFromLocalProperties() {
        File localPropertiesFile = new File(LOCAL_PROPERTIES_FILE_NAME);
        if (!localPropertiesFile.exists()) {
            URL resource = AndroidSdkFinder.class.getClassLoader().getResource(LOCAL_PROPERTIES_FILE_NAME);
            if (resource != null) {
                localPropertiesFile = new File(resource.getFile());
            }
        }
        if (localPropertiesFile.exists()) {
            Properties localProperties = new Properties();
            try {
                localProperties.load(new FileInputStream(localPropertiesFile));
                PropertiesHelper.doSubstitutions(localProperties);
                String sdkPath = localProperties.getProperty("sdk.dir");
                if (sdkPath != null) {
                    return new File(sdkPath);
                }
            } catch (IOException e) {
                // fine, we'll try something else
            }
        }
        return null;
    }

    private File getAndroidResourcePathFromSystemEnvironment() {
        // Hand tested
        String resourcePath = System.getenv().get("ANDROID_HOME");
        if (resourcePath != null) {
            return new File(resourcePath);
        }
        return null;
    }

    private File getAndroidResourcePathFromSystemProperty() {
        // this is used by the android-maven-plugin
        String resourcePath = System.getProperty("android.sdk.path");
        if (resourcePath != null) {
            return new File(resourcePath);
        }
        return null;
    }

    private File getAndroidResourcePathByExecingWhichAndroid() {
        // Hand tested
        // Should always work from the command line. Often fails in IDEs because
        // they don't pass the full PATH in the environment
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "android"});
            String sdkPath = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (sdkPath != null && sdkPath.endsWith("tools/android")) {
                String sdkPath1 = sdkPath.substring(0, sdkPath.indexOf("tools/android"));
                return new File(sdkPath1);
            }
        } catch (IOException e) {
            // fine we'll try something else
        }
        return null;
    }

    private Integer extractSdkInt(File file) {
        String name = file.getName();
        int dashIdx = name.lastIndexOf('-');
        if (dashIdx == -1) throw new RuntimeException("not an android-XX dir: " + file.getPath());

        return Integer.parseInt(name.substring(dashIdx + 1));
    }

    public ResourcePath getNewestSdkResourcePath() {
        File platformsDir = file(androidSdkBaseDir, "platforms");
        if (!platformsDir.exists()) return null;

        File[] sdkDirs = platformsDir.listFiles(new FilenameFilter() {
            @Override public boolean accept(@NotNull File dir, @NotNull String name) {
                return name.matches("android-\\d+");
            }
        });
        Arrays.sort(sdkDirs, new Comparator<File>() {
            @Override public int compare(@NotNull File o1, @NotNull File o2) {
                return extractSdkInt(o1).compareTo(extractSdkInt(o2));
            }
        });
        if (sdkDirs.length == 0)
            throw new RuntimeException("no android-XX dirs found in " + platformsDir.getPath());

        File sdkDir = sdkDirs[sdkDirs.length - 1];
        return new ResourcePath(android.R.class, file(sdkDir, "data", "res"), null);
    }
}
