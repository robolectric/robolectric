package com.xtremelabs.robolectric;

import android.app.Application;
import android.net.Uri__FromAndroid;
import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
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
    private static Map<RobolectricConfig, ResourceLoader> resourceLoaderForRootAndDirectory = new HashMap<RobolectricConfig, ResourceLoader>();

    // fields in the RobolectricTestRunner in the original ClassLoader
    private RobolectricClassLoader classLoader;
    private ClassHandler classHandler;
    private RobolectricTestRunnerInterface delegate;

    // fields in the RobolectricTestRunner in the instrumented ClassLoader
    private RobolectricConfig robolectricConfig;

    private static RobolectricClassLoader getDefaultLoader() {
        if (defaultLoader == null) {
            defaultLoader = new RobolectricClassLoader(ShadowWrangler.getInstance());
        }
        return defaultLoader;
    }

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public RobolectricTestRunner(Class<?> testClass) throws InitializationError {
        this(testClass, new RobolectricConfig(new File(".")));
    }

    /**
     * Call this constructor in subclasses in order to specify non-default configuration (e.g. location of the
     * AndroidManifest.xml file and resource directory).
     *
     * @param testClass         the test class to be run
     * @param robolectricConfig the configuration data
     * @throws InitializationError if junit says so
     */
    protected RobolectricTestRunner(Class<?> testClass, RobolectricConfig robolectricConfig)
            throws InitializationError {
        this(testClass,
                isInstrumented() ? null : ShadowWrangler.getInstance(),
                isInstrumented() ? null : getDefaultLoader(),
                robolectricConfig);
    }

    /**
     * Call this constructor in subclasses in order to specify the project root directory.
     *
     * @param testClass          the test class to be run
     * @param androidProjectRoot the directory containing your AndroidManifest.xml file and res dir
     * @throws InitializationError if the test class is malformed
     */
    public RobolectricTestRunner(Class<?> testClass, File androidProjectRoot) throws InitializationError {
        this(testClass, new RobolectricConfig(androidProjectRoot));
    }

    /**
     * Call this constructor in subclasses in order to specify the project root directory.
     *
     * @param testClass          the test class to be run
     * @param androidProjectRoot the directory containing your AndroidManifest.xml file and res dir
     * @throws InitializationError if junit says so
     * @deprecated Use {@link #RobolectricTestRunner(Class, File)} instead.
     */
    public RobolectricTestRunner(Class<?> testClass, String androidProjectRoot) throws InitializationError {
        this(testClass, new RobolectricConfig(new File(androidProjectRoot)));
    }

    /**
     * Call this constructor in subclasses in order to specify the location of the AndroidManifest.xml file and the
     * resource directory. The #androidManifestPath is used to locate the AndroidManifest.xml file which, in turn,
     * contains package name for the {@code R} class which contains the identifiers for all of the resources. The
     * resource directory is where the resource loader will look for resources to load.
     *
     * @param testClass           the test class to be run
     * @param androidManifestPath the AndroidManifest.xml file
     * @param resourceDirectory   the directory containing the project's resources
     * @throws InitializationError if junit says so
     */
    protected RobolectricTestRunner(Class<?> testClass, File androidManifestPath, File resourceDirectory)
            throws InitializationError {
        this(testClass, new RobolectricConfig(androidManifestPath, resourceDirectory));
    }

    /**
     * Call this constructor in subclasses in order to specify the location of the AndroidManifest.xml file and the
     * resource directory. The #androidManifestPath is used to locate the AndroidManifest.xml file which, in turn,
     * contains package name for the {@code R} class which contains the identifiers for all of the resources. The
     * resource directory is where the resource loader will look for resources to load.
     *
     * @param testClass           the test class to be run
     * @param androidManifestPath the relative path to the AndroidManifest.xml file
     * @param resourceDirectory   the relative path to the directory containing the project's resources
     * @throws InitializationError if junit says so
     * @deprecated Use {@link #RobolectricTestRunner(Class, File, File)} instead.
     */
    protected RobolectricTestRunner(Class<?> testClass, String androidManifestPath, String resourceDirectory)
            throws InitializationError {
        this(testClass, new RobolectricConfig(new File(androidManifestPath), new File(resourceDirectory)));
    }

    /**
     * This is not the constructor you are looking for... probably. This constructor creates a bridge between the test
     * runner called by JUnit and a second instance of the test runner that is loaded via the instrumenting class
     * loader. This instrumented instance of the test runner, along with the instrumented instance of the actual test,
     * provides access to Robolectric's features and the un-instrumented instance of the test runner delegates most of
     * the interesting test runner behavior to it. Providing your own class handler and class loader here in order to
     * get different functionality is a difficult and dangerous project. If you need to customize the project root and
     * resource directory, use {@link #RobolectricTestRunner(Class, String, String)}. For other extensions, consider
     * creating a subclass and overriding the documented methods of this class.
     *
     * @param testClass         the test class to be run
     * @param classHandler      the {@link ClassHandler} to use to in shadow delegation
     * @param classLoader       the {@link RobolectricClassLoader}
     * @param robolectricConfig the configuration
     * @throws InitializationError if junit says so
     */
    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, RobolectricClassLoader classLoader, RobolectricConfig robolectricConfig) throws InitializationError {
        super(isInstrumented() ? testClass : classLoader.bootstrap(testClass));

        if (!isInstrumented()) {
            this.classHandler = classHandler;
            this.classLoader = classLoader;
            this.robolectricConfig = robolectricConfig;

            delegateLoadingOf(Uri__FromAndroid.class.getName());
            delegateLoadingOf(RobolectricTestRunnerInterface.class.getName());
            delegateLoadingOf(RealObject.class.getName());
            delegateLoadingOf(ShadowWrangler.class.getName());
            delegateLoadingOf(RobolectricConfig.class.getName());
            delegateLoadingOf(android.R.class.getName());

            Class<?> delegateClass = classLoader.bootstrap(this.getClass());
            try {
                Constructor constructorForDelegate = delegateClass.getConstructor(Class.class);
                this.delegate = (RobolectricTestRunnerInterface) constructorForDelegate.newInstance(classLoader.bootstrap(testClass));
                this.delegate.setRobolectricConfig(robolectricConfig);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static boolean isInstrumented() {
        return RobolectricTestRunner.class.getClassLoader().getClass().getName().contains(RobolectricClassLoader.class.getName());
    }

    /**
     * Only used when creating the delegate instance within the instrumented ClassLoader.
     * <p/>
     * This is not the constructor you are looking for.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, RobolectricConfig robolectricConfig) throws InitializationError {
        super(testClass);
        this.classHandler = classHandler;
        this.robolectricConfig = robolectricConfig;
    }

    protected void delegateLoadingOf(String className) {
        classLoader.delegateLoadingOf(className);
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
        setupApplicationState(robolectricConfig);

        beforeTest(method);
    }

    @Override public void internalAfterTest(Method method) {
        afterTest(method);
    }

    @Override public void setRobolectricConfig(RobolectricConfig robolectricConfig) {
        this.robolectricConfig = robolectricConfig;
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
     * You probably don't want to override this method. Override #prepareTest(Object) instead.
     *
     * @see BlockJUnit4ClassRunner#createTest()
     */
    @Override
    public Object createTest() throws Exception {
        if (delegate != null) {
            return delegate.createTest();
        } else {
            Object test = super.createTest();
            prepareTest(test);
            return test;
        }
    }

    public void prepareTest(Object test) {
    }

    public void setupApplicationState(RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = createResourceLoader(robolectricConfig);

        Robolectric.bindDefaultShadowClasses();
        bindShadowClasses();

        Robolectric.resetStaticState();
        resetStaticState();

        Robolectric.application = ShadowApplication.bind(createApplication(), resourceLoader);
    }

    /**
     * Override this method to bind your own shadow classes
     */
    protected void bindShadowClasses() {
    }

    /**
     * Override this method to reset the state of static members before each test.
     */
    protected void resetStaticState() {
    }

    /**
     * Override this method if you want to provide your own implementation of Application.
     * <p/>
     * This method attempts to instantiate an application instance as specified by the AndroidManifest.xml.
     *
     * @return An instance of the Application class specified by the ApplicationManifest.xml or an instance of
     *         Application if not specified.
     */
    protected Application createApplication() {
        return new ApplicationResolver(robolectricConfig).resolveApplication();
    }

    private ResourceLoader createResourceLoader(RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = resourceLoaderForRootAndDirectory.get(robolectricConfig);
        if (resourceLoader == null) {
            try {
                robolectricConfig.validate();

                String rClassName = robolectricConfig.getRClassName();
                Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(robolectricConfig.getSdkVersion(), rClass, robolectricConfig.getResourceDirectory(), robolectricConfig.getAssetsDirectory());
                resourceLoaderForRootAndDirectory.put(robolectricConfig, resourceLoader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return resourceLoader;
    }

    private String findResourcePackageName(File projectManifestFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(projectManifestFile);

        String projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();

        return projectPackage + ".R";
    }
}
