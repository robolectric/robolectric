package com.xtremelabs.robolectric;

import android.app.Application;
import android.net.Uri;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import com.xtremelabs.robolectric.util.RealObject;
import com.xtremelabs.robolectric.util.TestHelperInterface;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Installs a {@link RobolectricClassLoader} and {@link com.xtremelabs.robolectric.res.ResourceLoader} in order to
 * provide a simulation of the Android runtime environment.
 */
public class RobolectricTestRunner extends BlockJUnit4ClassRunner implements RobolectricTestRunnerInterface {
    private static RobolectricClassLoader defaultLoader;
    private static Map<RootAndDirectory, ResourceLoader> resourceLoaderForRootAndDirectory = new HashMap<RootAndDirectory, ResourceLoader>();

    // fields in the RobolectricTestRunner in the original ClassLoader
    private RobolectricClassLoader classLoader;
    private ClassHandler classHandler;
    private Class<? extends TestHelperInterface> testHelperClass;
    private RobolectricTestRunnerInterface delegate;
    private TestHelperInterface testHelper;

    // fields in the RobolectricTestRunner in the instrumented ClassLoader
    private String resourceDirectory;
    private String projectRoot;
    private ResourceLoader resourceLoader;

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
     * @param classLoader
     * @param projectRoot
     * @param resourceDirectory
     * @throws InitializationError
     */
    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, RobolectricClassLoader classLoader, String projectRoot, String resourceDirectory) throws InitializationError {
        super(classLoader.bootstrap(testClass));
        this.classHandler = classHandler;
        this.classLoader = classLoader;
        this.projectRoot = projectRoot;
        this.resourceDirectory = resourceDirectory;

        delegateLoadingOf(Uri.class.getName());
        delegateLoadingOf(RobolectricTestRunnerInterface.class.getName());
        delegateLoadingOf(TestHelperInterface.class.getName());
        delegateLoadingOf(RealObject.class.getName());
        delegateLoadingOf(ShadowWrangler.class.getName());

        Class<?> delegateClass = classLoader.bootstrap(this.getClass());
        try {
            Constructor constructorForDelegate = delegateClass.getConstructor(Class.class, ClassHandler.class, String.class, String.class);
            this.delegate = (RobolectricTestRunnerInterface) constructorForDelegate.newInstance(classLoader.bootstrap(testClass), classHandler, projectRoot, resourceDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Only used when creating the delegate instance within the instrumented ClassLoader.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, String projectRoot, String resourceDirectory) throws InitializationError {
        super(testClass);
        this.classHandler = classHandler;
        this.projectRoot = projectRoot;
        this.resourceDirectory = resourceDirectory;
    }

    protected void delegateLoadingOf(String className) {
        classLoader.delegateLoadingOf(className);
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
        delegate.internalBeforeTest(method.getMethod());

        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    delegate.internalAfterTest(method.getMethod());
                    if (classHandler != null) classHandler.afterTest();
                }
            }
        };
    }

    /*
     * Called before each test method is run. Sets up the simulation of the Android runtime environment.
     */
    @Override public void internalBeforeTest(Method method) {
        if (testHelperClass != null) {
            testHelper = createTestHelper(method);
            testHelper.before(method);
        }
        setupApplicationState(projectRoot, resourceDirectory);

        beforeTest(method);
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override public void internalAfterTest(Method method) {
        if (testHelper != null) {
            testHelper.after(method);
        }

        afterTest(method);
    }

    /**
     * Called before each test method is run.
     *
     * @param method the test method about to be run
     */
    public void beforeTest(Method method) {
    }

    /**
     * Called after each test method is run.
     *
     * @param method the test method that just ran.
     */
    public void afterTest(Method method) {
    }

    /**
     * {@see BlockJUnit4TestRunner#createTest()}
     */
    @Override
    public Object createTest() throws Exception {
        if (delegate != null) {
            return delegate.createTest();
        } else {
            Object test = super.createTest();
            if (testHelper != null) {
                testHelper.prepareTest(test);
            }

            prepareTest(test);
            return test;
        }
    }

    public void prepareTest(Object test) {
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


    public void setupApplicationState(String projectRoot, String resourceDir) {
        resourceLoader = createResourceLoader(projectRoot, resourceDir);

        Robolectric.bindDefaultShadowClasses();
        Robolectric.resetStaticState();
        Robolectric.application = ShadowApplication.bind(new Application(), resourceLoader);
    }

    private ResourceLoader createResourceLoader(String projectRoot, String resourceDirectory) {
        RootAndDirectory rootAndDirectory = new RootAndDirectory(projectRoot, resourceDirectory);
        ResourceLoader resourceLoader = resourceLoaderForRootAndDirectory.get(rootAndDirectory);
        if (resourceLoader == null) {
            try {
                String rClassName = findResourcePackageName(projectRoot);
                Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(rClass, new File(resourceDirectory));
                resourceLoaderForRootAndDirectory.put(rootAndDirectory, resourceLoader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return resourceLoader;
    }

    private String findResourcePackageName(String projectRoot) throws ParserConfigurationException, IOException, SAXException {
        File projectManifestFile = new File(projectRoot, "AndroidManifest.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(projectManifestFile);

        String projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();

        return projectPackage + ".R";
    }

    private static class RootAndDirectory {
        public String projectRoot;
        public String resourceDirectory;

        private RootAndDirectory(String projectRoot, String resourceDirectory) {
            this.projectRoot = projectRoot;
            this.resourceDirectory = resourceDirectory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RootAndDirectory that = (RootAndDirectory) o;

            if (projectRoot != null ? !projectRoot.equals(that.projectRoot) : that.projectRoot != null) return false;
            if (resourceDirectory != null ? !resourceDirectory.equals(that.resourceDirectory) : that.resourceDirectory != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = projectRoot != null ? projectRoot.hashCode() : 0;
            result = 31 * result + (resourceDirectory != null ? resourceDirectory.hashCode() : 0);
            return result;
        }
    }

}
