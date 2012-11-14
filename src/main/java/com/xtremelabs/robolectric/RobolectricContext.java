package com.xtremelabs.robolectric;

import android.net.Uri__FromAndroid;
import com.xtremelabs.robolectric.bytecode.*;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static com.xtremelabs.robolectric.RobolectricTestRunner.isBootstrapped;

public class RobolectricContext {
    private static final Map<Class<? extends RobolectricTestRunner>, RobolectricContext> contextsByTestRunner = new HashMap<Class<? extends RobolectricTestRunner>, RobolectricContext>();

    private final RobolectricConfig robolectricConfig;
    private final RobolectricClassLoader robolectricClassLoader;
    private final ClassHandler classHandler;
    private RepositorySystem repositorySystem;

    public interface Factory {
        RobolectricContext create();
    }

    public static Class<?> bootstrap(Class<? extends RobolectricTestRunner> robolectricTestRunnerClass, Class<?> testClass, Factory factory) {
        if (isBootstrapped(robolectricTestRunnerClass) || isBootstrapped(testClass)) {
            if (!isBootstrapped(testClass)) throw new IllegalStateException("test class is somehow not bootstrapped");
            return testClass;
        }

        RobolectricContext robolectricContext;
        synchronized(contextsByTestRunner) {
            robolectricContext = contextsByTestRunner.get(robolectricTestRunnerClass);
            if (robolectricContext == null) {
                robolectricContext = factory.create();
                contextsByTestRunner.put(robolectricTestRunnerClass, robolectricContext);
            }
        }

        return robolectricContext.bootstrapTestClass(robolectricTestRunnerClass, testClass);
    }

    public RobolectricContext() {
        this.robolectricConfig = createRobolectricConfig();
        ClassCache classCache = createClassCache();
        Setup setup = createSetup();
        this.classHandler = createClassHandler(setup);
        AndroidTranslator androidTranslator = createAndroidTranslator(classHandler, setup, classCache);
        this.robolectricClassLoader = createRobolectricClassLoader(classCache, androidTranslator);
        init();
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

    public AndroidTranslator createAndroidTranslator(ClassHandler classHandler, Setup setup, ClassCache classCache) {
        return new AndroidTranslator(classHandler, classCache, setup);
    }

    protected RobolectricConfig createRobolectricConfig() {
        return new RobolectricConfig(new File("."));
    }

    public RobolectricConfig getRobolectricConfig() {
        return robolectricConfig;
    }

    public ClassHandler getClassHandler() {
        return classHandler;
    }

    private void init() {
        performDelegations(robolectricClassLoader);
    }

    private void performDelegations(RobolectricClassLoader classLoader) {
        classLoader.delegateLoadingOf(Uri__FromAndroid.class.getName());
        classLoader.delegateLoadingOf(RobolectricTestRunnerInterface.class.getName());
        classLoader.delegateLoadingOf(RealObject.class.getName());
        classLoader.delegateLoadingOf(ShadowWrangler.class.getName());
        classLoader.delegateLoadingOf(Vars.class.getName());
        classLoader.delegateLoadingOf(RobolectricConfig.class.getName());
        classLoader.delegateLoadingOf(DatabaseConfig.DatabaseMap.class.getName());
        classLoader.delegateLoadingOf(android.R.class.getName());
    }

    private Class<?> bootstrapTestClass(Class<? extends RobolectricTestRunner> robolectricTestRunnerClass, Class<?> testClass) {
        Class<?> bootstrappedTestClass = robolectricClassLoader.bootstrap(testClass);
        Class<?> bootstrappedTestRunnerClass = robolectricClassLoader.bootstrap(robolectricTestRunnerClass);
        setRobolectricContextField(robolectricTestRunnerClass);
        setRobolectricContextField(bootstrappedTestRunnerClass);
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

    protected RobolectricClassLoader createRobolectricClassLoader(ClassCache classCache, AndroidTranslator androidTranslator) {
//            shadowWrangler.delegateBackToInstrumented = true;
        final ClassLoader parentClassLoader = this.getClass().getClassLoader();
        ClassLoader realAndroidJarsClassLoader = new URLClassLoader(new URL[]{
//                        parseUrl(getAndroidSdkHome() + "/add-ons/addon_google_apis_google_inc_8/libs/maps.jar"),
                getRealAndroidArtifact("android-base"),
                getRealAndroidArtifact("android-kxml2"),
                getRealAndroidArtifact("android-luni")
        }, null) {
            @Override
            protected Class<?> findClass(String s) throws ClassNotFoundException {
                try {
                    return super.findClass(s);
                } catch (ClassNotFoundException e) {
                    return parentClassLoader.loadClass(s);
                }
            }
        };
        return new RobolectricClassLoader(realAndroidJarsClassLoader, classCache, androidTranslator);
    }

    public RobolectricClassLoader getRobolectricClassLoader() {
        return robolectricClassLoader;
    }

    public Setup createSetup() {
        return new Setup();
    }

    public RepositorySystem createRepositorySystem() {
        try {
            return new DefaultPlexusContainer().lookup(RepositorySystem.class);
        } catch (Exception e) {
            throw new IllegalStateException("dependency injection failed", e);
        }
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem == null ? repositorySystem = createRepositorySystem() : repositorySystem;
    }

    private static RepositorySystemSession newSession(RepositorySystem system) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository(new File(System.getProperty("user.home"), ".m2/repository"));
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        return session;
    }

    public RemoteRepository getCentralRepository() {
        return new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
    }

    private URL getRealAndroidArtifact(String artifactId) {
        return getArtifact(new DefaultArtifact("com.squareup.robolectric", artifactId, "real", "jar", "4.1.2_r1"));
    }

    private URL getArtifact(String coords) {
        return getArtifact(new DefaultArtifact(coords));
    }

    private URL getArtifact(Artifact artifact) {
        RepositorySystem repositorySystem = createRepositorySystem();
        RepositorySystemSession session = newSession(repositorySystem);
        ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(artifact);
        artifactRequest.addRepository(getCentralRepository());

        try {
            ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);
            return parseUrl("file:" + artifactResult.getArtifact().getFile().getAbsolutePath());
        } catch (ArtifactResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** @deprecated use {@link com.xtremelabs.robolectric.Robolectric.Reflection#setFinalStaticField(Class, String, Object)} */
    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        Robolectric.Reflection.setFinalStaticField(clazz, fieldName, value);
    }
}
