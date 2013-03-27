package org.robolectric;

import org.robolectric.bytecode.ClassHandler;
import org.robolectric.bytecode.ShadowMap;
import org.robolectric.bytecode.ShadowWrangler;
import org.robolectric.res.AndroidSdkFinder;
import org.robolectric.res.ResourcePath;

import java.util.HashMap;
import java.util.Map;

public class SdkEnvironment {
    private final AndroidManifest appManifest;
    private final ClassLoader robolectricClassLoader;
    private ResourcePath systemResourcePath;
    public final Map<ShadowMap, ShadowWrangler> classHandlersByShadowMap = new HashMap<ShadowMap, ShadowWrangler>();
    private ClassHandler currentClassHandler;

    public SdkEnvironment(AndroidManifest appManifest, ClassLoader robolectricClassLoader) {
        this.appManifest = appManifest;
        this.robolectricClassLoader = robolectricClassLoader;
    }

    public AndroidManifest getAppManifest() {
        return appManifest;
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

    public ClassHandler getCurrentClassHandler() {
        return currentClassHandler;
    }

    public void setCurrentClassHandler(ClassHandler currentClassHandler) {
        this.currentClassHandler = currentClassHandler;
    }

    public interface Factory {
        public SdkEnvironment create();
    }
}
