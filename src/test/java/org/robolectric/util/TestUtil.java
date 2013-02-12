package org.robolectric.util;

import org.robolectric.AndroidManifest;
import org.robolectric.R;
import org.robolectric.res.AndroidResourcePathFinder;
import org.robolectric.res.ResourcePath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static org.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static org.junit.Assert.assertTrue;

public abstract class TestUtil {
    public static final ResourcePath TEST_RESOURCE_PATH = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
    public static final String TEST_PACKAGE = R.class.getPackage().getName();
    public static final ResourcePath SYSTEM_RESOURCE_PATH = AndroidResourcePathFinder.getSystemResourcePath(DEFAULT_SDK_VERSION, testResources());
    public static final String SYSTEM_PACKAGE = android.R.class.getPackage().getName();
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

    public static ResourcePath testResources() {
        return TEST_RESOURCE_PATH;
    }

    public static ResourcePath systemResources() {
        return SYSTEM_RESOURCE_PATH;
    }

    public static AndroidManifest newConfig(String androidManifestFile) {
        return new AndroidManifest(resourceFile(androidManifestFile), (File) null, null);
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

    public static String readString(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }

    public static String joinPath(String... parts) {
        File file = new File(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            file = new File(file, part);
        }
        return file.getPath();
    }
}
