package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.bytecode.AndroidTranslator;
import com.xtremelabs.robolectric.bytecode.ClassCache;
import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.bytecode.RobolectricInternals;
import com.xtremelabs.robolectric.bytecode.Setup;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import com.xtremelabs.robolectric.res.AndroidResourcePathFinder;
import com.xtremelabs.robolectric.res.ResourcePath;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static com.xtremelabs.robolectric.RobolectricTestRunner.isBootstrapped;

public class RobolectricContext {
    private static final Map<Class<? extends RobolectricTestRunner>, RobolectricContext> contextsByTestRunner = new HashMap<Class<? extends RobolectricTestRunner>, RobolectricContext>();

    private final AndroidManifest appManifest;
    private final RobolectricClassLoader robolectricClassLoader;
    private final ClassHandler classHandler;
    public static RobolectricContext mostRecentRobolectricContext; // ick, race condition

    public interface Factory {
        RobolectricContext create();
    }

    public static Class<?> bootstrap(Class<? extends RobolectricTestRunner> robolectricTestRunnerClass, Class<?> testClass, Factory factory) {
        if (isBootstrapped(robolectricTestRunnerClass) || isBootstrapped(testClass)) {
            if (!isBootstrapped(testClass)) throw new IllegalStateException("test class is somehow not bootstrapped");
            return testClass;
        }

        RobolectricContext robolectricContext;
        synchronized (contextsByTestRunner) {
            robolectricContext = contextsByTestRunner.get(robolectricTestRunnerClass);
            if (robolectricContext == null) {
                robolectricContext = factory.create();
                contextsByTestRunner.put(robolectricTestRunnerClass, robolectricContext);
            }
        }

        mostRecentRobolectricContext = robolectricContext;

        return robolectricContext.bootstrapTestClass(testClass);
    }

    public RobolectricContext() {
        ClassCache classCache = createClassCache();
        Setup setup = createSetup();
        classHandler = createClassHandler(setup);
        appManifest = createAppManifest();
        AndroidTranslator androidTranslator = createAndroidTranslator(setup, classCache);
        robolectricClassLoader = createRobolectricClassLoader(setup, classCache, androidTranslator);
    }

    private ClassHandler createClassHandler(Setup setup) {
        return new ShadowWrangler(setup);
    }

    public ClassCache createClassCache() {
        final String classCachePath = System.getProperty("cached.robolectric.classes.path");
        final File classCacheDirectory;
        if (null == classCachePath || "".equals(classCachePath.trim())) {
            classCacheDirectory = new File("./tmp");
        } else {
            classCacheDirectory = new File(classCachePath);
        }

        return new ClassCache(new File(classCacheDirectory, "cached-robolectric-classes.jar").getAbsolutePath(), AndroidTranslator.CACHE_VERSION);
    }

    public AndroidTranslator createAndroidTranslator(Setup setup, ClassCache classCache) {
        return new AndroidTranslator(classCache, setup);
    }

    protected AndroidManifest createAppManifest() {
        return new AndroidManifest(new File("."));
    }

    public AndroidManifest getAppManifest() {
        return appManifest;
    }

    public ClassHandler getClassHandler() {
        return classHandler;
    }

    public ResourcePath getSystemResourcePath() {
        AndroidManifest manifest = getAppManifest();
        return AndroidResourcePathFinder.getSystemResourcePath(manifest.getRealSdkVersion(), manifest.getResourcePath());
    }

    private Class<?> bootstrapTestClass(Class<?> testClass) {
        Class<?> bootstrappedTestClass = robolectricClassLoader.bootstrap(testClass);
        return bootstrappedTestClass;
    }

    public RobolectricTestRunnerInterface getBootstrappedTestRunner(RobolectricTestRunnerInterface originalTestRunner) {
        Class<?> originalTestClass = originalTestRunner.getTestClass().getJavaClass();
        Class<?> bootstrappedTestClass = robolectricClassLoader.bootstrap(originalTestClass);
        Class<?> bootstrappedTestRunnerClass = robolectricClassLoader.bootstrap(originalTestRunner.getClass());

        try {
            Constructor<?> constructorForDelegate = bootstrappedTestRunnerClass.getConstructor(Class.class);
            return (RobolectricTestRunnerInterface) constructorForDelegate.newInstance(bootstrappedTestClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setRobolectricContextField(Class<?> testRunnerClass) {
        Class<?> clazz = testRunnerClass;
        while (!clazz.getName().equals(RobolectricTestRunner.class.getName())) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new RuntimeException(testRunnerClass + " doesn't extend RobolectricTestRunner");
        }
        try {
            Field field = clazz.getDeclaredField("sharedRobolectricContext");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected RobolectricClassLoader createRobolectricClassLoader(Setup setup, ClassCache classCache, AndroidTranslator androidTranslator) {
        final ClassLoader parentClassLoader = this.getClass().getClassLoader();
        ClassLoader realAndroidJarsClassLoader = new URLClassLoader(
                artifactUrls(realAndroidDependency("android-base"),
                        realAndroidDependency("android-kxml2"),
                        realAndroidDependency("android-luni"))
        , null) {
            @Override
            protected Class<?> findClass(String s) throws ClassNotFoundException {
                try {
                    return super.findClass(s);
                } catch (ClassNotFoundException e) {
                    return parentClassLoader.loadClass(s);
                }
            }
        };
        RobolectricClassLoader robolectricClassLoader = new RobolectricClassLoader(realAndroidJarsClassLoader, classCache, androidTranslator, setup);
        injectClassHandler(robolectricClassLoader);
        return robolectricClassLoader;
    }

    private void injectClassHandler(RobolectricClassLoader robolectricClassLoader) {
        try {
            Field field = robolectricClassLoader.loadClass(RobolectricInternals.class.getName()).getDeclaredField("classHandler");
            field.setAccessible(true);
            field.set(null, classHandler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RobolectricClassLoader getRobolectricClassLoader() {
        return robolectricClassLoader;
    }

    public Setup createSetup() {
        return new Setup();
    }

    private URL[] artifactUrls(Dependency... dependencies) {
        DependenciesTask dependenciesTask = new DependenciesTask();
        configureMaven(dependenciesTask);
        Project project = new Project();
        dependenciesTask.setProject(project);
        for (Dependency dependency : dependencies) {
            dependenciesTask.addDependency(dependency);
        }
        dependenciesTask.execute();

        @SuppressWarnings("unchecked")
        Hashtable<String, String> artifacts = project.getProperties();
        URL[] urls = new URL[artifacts.size()];
        int i = 0;
        for (String path : artifacts.values()) {
            try {
                urls[i++] = new URL("file://" + path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return urls;
    }

    @SuppressWarnings("UnusedParameters")
    protected void configureMaven(DependenciesTask dependenciesTask) {
        // maybe you want to override this method and some settings?
    }

    private Dependency realAndroidDependency(String artifactId) {
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.squareup.robolectric");
        dependency.setArtifactId(artifactId);
        dependency.setVersion("4.1.2_r1");
        dependency.setType("jar");
        dependency.setClassifier("real");
        return dependency;
    }

    /** @deprecated use {@link com.xtremelabs.robolectric.Robolectric.Reflection#setFinalStaticField(Class, String, Object)} */
    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        Robolectric.Reflection.setFinalStaticField(clazz, fieldName, value);
    }
}
