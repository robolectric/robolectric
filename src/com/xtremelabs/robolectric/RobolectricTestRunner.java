package com.xtremelabs.robolectric;

import android.net.Uri;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.TestHelperInterface;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

/**
 * Installs a {@link RobolectricClassLoader} and {@link com.xtremelabs.robolectric.res.ResourceLoader} in order to
 * provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner {
    private static RobolectricClassLoader defaultLoader;
    private RobolectricClassLoader loader;

    private ClassHandler classHandler;
    private Class<? extends TestHelperInterface> testHelperClass;
    private TestHelperInterface testHelper;
    private String resourceDirectory;
    private String projectRoot;

    private static RobolectricClassLoader getDefaultLoader() {
        if (defaultLoader == null) {
            defaultLoader = new RobolectricClassLoader(ShadowWrangler.getInstance());
        }
        return defaultLoader;
    }

    /**
     * Creates a runner to run {@code testClass}.
     *
     * @param testClass the test class to be run
     * @throws InitializationError
     */
    public RobolectricTestRunner(Class<?> testClass) throws InitializationError {
        this(testClass, ".", "res");
    }

    /**
     * Call this constructor in subclasses in order to specify the project root directory and the resource directory.
     * The project root is used to locate the AndroidManifest.xml file which, in turn, contains package name for the
     * {@code R} class which contains the identifiers for all of the resources. The resource directory is where the
     * resource loader will look for resources to load.
     *
     * @param testClass         the test class to be run
     * @param projectRoot       the relative path to the directory containing the AndroidManifest.xml file
     * @param resourceDirectory the relative path to the directory containing the project's resources
     * @throws InitializationError
     */
    protected RobolectricTestRunner(Class<?> testClass, String projectRoot, String resourceDirectory) throws InitializationError {
        this(testClass, ShadowWrangler.getInstance(), getDefaultLoader(), projectRoot, resourceDirectory);
    }

    /**
     * @deprecated {@link TestHelperInterface} is no longer necessary for setting up default resource loading behavior.
     *             Use {@link #RobolectricTestRunner(Class, String, String)} to override the default project root and resource
     *             directory.
     */
    @Deprecated
    protected RobolectricTestRunner(Class<?> testClass, Class<? extends TestHelperInterface> testHelperClass) throws InitializationError {
        this(testClass);
        setTestHelperClass(testHelperClass);
    }

    /**
     * This is not the constructor you are looking for... probably. Providing your own class handler and class loader is
     * risky. If you need to customize the project root and resource directory, use
     * {@link #RobolectricTestRunner(Class, String, String)}
     *
     * @param testClass
     * @param classHandler
     * @param loader
     * @param projectRoot
     * @param resourceDirectory
     * @throws InitializationError
     */
    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, RobolectricClassLoader loader, String projectRoot, String resourceDirectory) throws InitializationError {
        super(loader.bootstrap(testClass));
        this.classHandler = classHandler;
        this.loader = loader;
        this.projectRoot = projectRoot;
        this.resourceDirectory = resourceDirectory;

        this.loader.delegateLoadingOf(Uri.class.getName());
        this.loader.delegateLoadingOf(LoadableHelper.class.getName());
        this.loader.delegateLoadingOf(TestHelperInterface.class.getName());
        this.loader.delegateLoadingOf(RealObject.class.getName());
        this.loader.delegateLoadingOf(ShadowWrangler.class.getName());
    }

    protected void delegateLoadingOf(String className) {
        loader.delegateLoadingOf(className);
    }

    /**
     * @deprecated {@link TestHelperInterface} is no longer necessary for setting up default resource loading behavior.
     *             Use {@link #RobolectricTestRunner(Class, String, String)} to override the default project root and resource
     *             directory.
     */
    @Deprecated
    protected void setTestHelperClass(Class<? extends TestHelperInterface> testHelperClass) {
        this.testHelperClass = testHelperClass;
    }

    @Override protected Statement methodBlock(final FrameworkMethod method) {
        if (classHandler != null) classHandler.beforeTest();
        beforeTest(method.getMethod());

        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    afterTest(method.getMethod());
                    if (classHandler != null) classHandler.afterTest();
                }
            }
        };
    }

    /**
     * Called before each test method is run. Sets up the simulation of the Android runtime environment.
     *
     * @param method the test method about to be run
     */
    protected void beforeTest(Method method) {
        if (testHelperClass != null) {
            testHelper = createTestHelper(method);
            testHelper.before(method);
        } else {
            createLoadableHelper(method).setupApplicationState(projectRoot, resourceDirectory);
        }
    }

    /**
     * Called after each test method is run.
     *
     * @param method the test method that just ran.
     */
    protected void afterTest(Method method) {
        if (testHelper != null) {
            testHelper.after(method);
        }
    }

    /**
     * {@see BlockJUnit4TestRunner#createTest()}
     */
    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        if (testHelper != null) {
            testHelper.prepareTest(test);
        }
        return test;
    }

    private TestHelperInterface createTestHelper(Method method) {
        Class<?> testClass = method.getDeclaringClass();
        try {
            return (TestHelperInterface) testClass.getClassLoader().loadClass(testHelperClass.getName()).newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private LoadableHelper createLoadableHelper(Method method) {
        Class<?> testClass = method.getDeclaringClass();
        try {
            return (LoadableHelper) testClass.getClassLoader().loadClass(LoadableHelperImpl.class.getName()).newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
