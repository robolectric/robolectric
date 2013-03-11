package org.robolectric;

import org.robolectric.bytecode.ClassHandler;
import org.robolectric.res.AndroidSdkFinder;
import org.robolectric.res.ResourcePath;

public class RobolectricContext {
    private final AndroidManifest appManifest;
    private final ClassLoader robolectricClassLoader;
    private final ClassHandler classHandler;
    private ResourcePath systemResourcePath;

    public RobolectricContext(AndroidManifest appManifest, ClassHandler classHandler, ClassLoader robolectricClassLoader) {
        this.appManifest = appManifest;
        this.classHandler = classHandler;
        this.robolectricClassLoader = robolectricClassLoader;
    }

    public AndroidManifest getAppManifest() {
        return appManifest;
    }

    public ClassHandler getClassHandler() {
        return classHandler;
    }

    public synchronized ResourcePath getSystemResourcePath() {
        if (systemResourcePath == null) {
            int targetSdkVersion = RobolectricTestRunner.getTargetVersionWhenAppManifestMightBeNullWhaaa(appManifest);
            systemResourcePath = new AndroidSdkFinder().findSystemResourcePath(targetSdkVersion);
        }
        return systemResourcePath;
    }

    public Class<?> bootstrappedClass(Class<?> testClass) {
        try {
            return robolectricClassLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassLoader getRobolectricClassLoader() {
        return robolectricClassLoader;
    }

    /**
     * @deprecated use {@link org.robolectric.Robolectric.Reflection#setFinalStaticField(Class, String, Object)}
     */
    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        Robolectric.Reflection.setFinalStaticField(clazz, fieldName, value);
    }
}
