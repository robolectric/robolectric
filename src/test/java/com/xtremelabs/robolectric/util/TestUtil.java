package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.RobolectricConfig;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static org.junit.Assert.assertTrue;

public abstract class TestUtil {
    public static File testDirLocation;

    public static void assertEquals(Collection<?> expected, Collection<?> actual) {
        org.junit.Assert.assertEquals(stringify(expected), stringify(actual));
    }

    public static String stringify(Collection<?> collection) {
        StringBuilder buf = new StringBuilder();
        for (Object o : collection) {
            if (buf.length() > 0) buf.append("\n");
            buf.append(o);
        }
        return buf.toString();
    }

    public static <T> void assertInstanceOf(Class<? extends T> expectedClass, T object) {
        Class actualClass = object.getClass();
        assertTrue(expectedClass + " should be assignable from " + actualClass,
                expectedClass.isAssignableFrom(actualClass));
    }

    public static File file(String... pathParts) {
        return file(new File("."), pathParts);
    }

    public static File file(File f, String... pathParts) {
        for (String pathPart : pathParts) {
            f = new File(f, pathPart);
        }
        return f;
    }

    public static File resourcesBaseDir() {
        if (testDirLocation == null) {
            File testDir = file("src", "test", "resources");
            if (hasTestManifest(testDir)) return testDirLocation = testDir;

            File roboTestDir = file("robolectric", "src", "test", "resources");
            if (hasTestManifest(roboTestDir)) return testDirLocation = roboTestDir;

            File submoduleDir = file("submodules", "robolectric", "src", "test", "resources");
            if (hasTestManifest(submoduleDir)) return testDirLocation = submoduleDir;
            
            //required for robolectric-sqlite to find resources to test against
            File roboSiblingTestDir = file(new File(new File(".").getAbsolutePath()).getParentFile().getParentFile(),"robolectric", "src", "test", "resources");
            if (hasTestManifest(roboSiblingTestDir)) return testDirLocation = roboSiblingTestDir;
            
            throw new RuntimeException("can't find your TestAndroidManifest.xml in "
                    + testDir.getAbsolutePath() + " or " + roboTestDir.getAbsolutePath() + "\n or " + roboSiblingTestDir.getAbsolutePath());
        } else {
            return testDirLocation;
        }
    }

    private static boolean hasTestManifest(File testDir) {
        return new File(testDir, "TestAndroidManifest.xml").isFile();
    }

    public static File resourceFile(String... pathParts) {
        return file(resourcesBaseDir(), pathParts);
    }

    public static RobolectricConfig newConfig(String androidManifestFile) {
        return new RobolectricConfig(resourceFile(androidManifestFile), null, null);
    }

    public static File getSystemResourceDir(String... paths) throws Exception {
       
       Map<String,String> env = System.getenv();
       String sdkDir;
       if (env.containsKey("ANDROID_HOME")) {
    	   sdkDir = env.get("ANDROID_HOME");
       } else {
    	    Properties localProperties = new Properties();
           	localProperties.load(new FileInputStream(new File("local.properties")));
           	PropertiesHelper.doSubstitutions(localProperties);
           	sdkDir = localProperties.getProperty("sdk.dir");             
       }

        return file(new File(sdkDir, "platforms/android-" + DEFAULT_SDK_VERSION + "/data/res/"), paths);
    }
}
