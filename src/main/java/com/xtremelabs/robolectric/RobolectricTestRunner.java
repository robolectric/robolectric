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
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.DatabaseConfig.DatabaseMap;
import com.xtremelabs.robolectric.util.DatabaseConfig.UsingDatabaseMap;
import com.xtremelabs.robolectric.util.SQLiteMap;
import javassist.Loader;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    private DatabaseMap databaseMap;
    
	// fields in the RobolectricTestRunner in the instrumented ClassLoader
    protected RobolectricConfig robolectricConfig;

    private static RobolectricClassLoader getDefaultLoader() {
        if (defaultLoader == null) {
            defaultLoader = new RobolectricClassLoader(ShadowWrangler.getInstance());
        }
        return defaultLoader;
    }

    public static void setDefaultLoader(Loader robolectricClassLoader) {
    	//used by the RoboSpecs project to allow for mixed scala\java tests to be run with Maven Surefire (see the RoboSpecs project on github)
        if (defaultLoader == null) {
            defaultLoader = (RobolectricClassLoader)robolectricClassLoader;
        } else throw new RuntimeException("You may not set the default robolectricClassLoader unless it is null!");
    }

    /**
     * Call this if you would like Robolectric to rewrite additional classes and turn them
     * into "do nothing" classes which proxy all method calls to shadow classes, just like it does
     * with the android classes by default.
     *
     * @param classOrPackageToBeInstrumented fully-qualified class or package name
     */
    protected static void addClassOrPackageToInstrument(String classOrPackageToBeInstrumented) {
        if (!isInstrumented()) {
            defaultLoader.addCustomShadowClass(classOrPackageToBeInstrumented);
        }
    }

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public RobolectricTestRunner(final Class<?> testClass) throws InitializationError {
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
    protected RobolectricTestRunner(final Class<?> testClass, final RobolectricConfig robolectricConfig)
            throws InitializationError {
        this(testClass,
                isInstrumented() ? null : ShadowWrangler.getInstance(),
                isInstrumented() ? null : getDefaultLoader(),
                robolectricConfig, new SQLiteMap());
    }
    
    /**
     * Call this constructor in subclasses in order to specify non-default configuration (e.g. location of the
     * AndroidManifest.xml file, resource directory, and DatabaseMap).
     *
     * @param testClass         the test class to be run
     * @param robolectricConfig the configuration data
     * @param databaseMap		the database mapping
     * @throws InitializationError if junit says so
     */
    protected RobolectricTestRunner(Class<?> testClass, RobolectricConfig robolectricConfig, DatabaseMap databaseMap)
            throws InitializationError {
        this(testClass,
                isInstrumented() ? null : ShadowWrangler.getInstance(),
                isInstrumented() ? null : getDefaultLoader(),
                robolectricConfig, databaseMap);
    }

    /**
     * Call this constructor in subclasses in order to specify the project root directory.
     *
     * @param testClass          the test class to be run
     * @param androidProjectRoot the directory containing your AndroidManifest.xml file and res dir
     * @throws InitializationError if the test class is malformed
     */
    public RobolectricTestRunner(final Class<?> testClass, final File androidProjectRoot) throws InitializationError {
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
    @Deprecated
    public RobolectricTestRunner(final Class<?> testClass, final String androidProjectRoot) throws InitializationError {
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
    protected RobolectricTestRunner(final Class<?> testClass, final File androidManifestPath, final File resourceDirectory)
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
    @Deprecated
    protected RobolectricTestRunner(final Class<?> testClass, final String androidManifestPath, final String resourceDirectory)
            throws InitializationError {
        this(testClass, new RobolectricConfig(new File(androidManifestPath), new File(resourceDirectory)));
    }

    protected RobolectricTestRunner(Class<?> testClass, ClassHandler classHandler, RobolectricClassLoader classLoader, RobolectricConfig robolectricConfig) throws InitializationError {
        this(testClass, classHandler, classLoader, robolectricConfig, new SQLiteMap());
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
    protected RobolectricTestRunner(final Class<?> testClass, final ClassHandler classHandler, final RobolectricClassLoader classLoader, final RobolectricConfig robolectricConfig, final DatabaseMap map) throws InitializationError {
        super(isInstrumented() ? testClass : classLoader.bootstrap(testClass));
                
        if (!isInstrumented()) {
            this.classHandler = classHandler;
            this.classLoader = classLoader;
            this.robolectricConfig = robolectricConfig;
            this.databaseMap = setupDatabaseMap(testClass, map);
            
            Thread.currentThread().setContextClassLoader(classLoader);
            
            delegateLoadingOf(Uri__FromAndroid.class.getName());
            delegateLoadingOf(RobolectricTestRunnerInterface.class.getName());
            delegateLoadingOf(RealObject.class.getName());
            delegateLoadingOf(ShadowWrangler.class.getName());
            delegateLoadingOf(RobolectricConfig.class.getName());
            delegateLoadingOf(DatabaseMap.class.getName());
            delegateLoadingOf(android.R.class.getName());

            Class<?> delegateClass = classLoader.bootstrap(this.getClass());
            try {
                Constructor<?> constructorForDelegate = delegateClass.getConstructor(Class.class);
                this.delegate = (RobolectricTestRunnerInterface) constructorForDelegate.newInstance(classLoader.bootstrap(testClass));
                this.delegate.setRobolectricConfig(robolectricConfig);
                this.delegate.setDatabaseMap(databaseMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static boolean isInstrumented() {
        return RobolectricTestRunner.class.getClassLoader().getClass().getName().contains(RobolectricClassLoader.class.getName());
    }

    /**
     * Only used when creating the delegate instance within the instrumented ClassLoader.
     * <p/>
     * This is not the constructor you are looking for.
     */
    @SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
    protected RobolectricTestRunner(final Class<?> testClass, final ClassHandler classHandler, final RobolectricConfig robolectricConfig) throws InitializationError {
        super(testClass);
        this.classHandler = classHandler;
        this.robolectricConfig = robolectricConfig;
    }

    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getField(fieldName);
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void delegateLoadingOf(final String className) {
        classLoader.delegateLoadingOf(className);
    }

    @Override protected Statement methodBlock(final FrameworkMethod method) {
        setupI18nStrictState(method.getMethod(), robolectricConfig);

    	if (classHandler != null) {
            classHandler.configure(robolectricConfig);
            classHandler.beforeTest();
        }
        delegate.internalBeforeTest(method.getMethod());

        final Statement statement = super.methodBlock(method);
        return new Statement() {
            @Override public void evaluate() throws Throwable {
                // todo: this try/finally probably isn't right -- should mimic RunAfters? [xw]
                try {
                    statement.evaluate();
                } finally {
                    delegate.internalAfterTest(method.getMethod());
                    if (classHandler != null) {
                        classHandler.afterTest();
                    }
                }
            }
        };
    }

    /*
     * Called before each test method is run. Sets up the simulation of the Android runtime environment.
     */
    @Override public void internalBeforeTest(final Method method) {
        setupApplicationState(robolectricConfig);

        beforeTest(method);
    }

    @Override public void internalAfterTest(final Method method) {
        afterTest(method);
    }

    @Override public void setRobolectricConfig(final RobolectricConfig robolectricConfig) {
        this.robolectricConfig = robolectricConfig;
    }

    /**
     * Called before each test method is run.
     *
     * @param method the test method about to be run
     */
    public void beforeTest(final Method method) {
    }

    /**
     * Called after each test method is run.
     *
     * @param method the test method that just ran.
     */
    public void afterTest(final Method method) {
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

    public void prepareTest(final Object test) {
    }

    public void setupApplicationState(final RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = createResourceLoader(robolectricConfig);

        Robolectric.bindDefaultShadowClasses();
        bindShadowClasses();

        Robolectric.resetStaticState();
        resetStaticState();
        
        DatabaseConfig.setDatabaseMap(this.databaseMap);//Set static DatabaseMap in DBConfig
        
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
     * Sets Robolectric config to determine if Robolectric should blacklist API calls that are not
     * I18N/L10N-safe.
     * <p/>
     * I18n-strict mode affects suitably annotated shadow methods. Robolectric will throw exceptions
     * if these methods are invoked by application code. Additionally, Robolectric's ResourceLoader
     * will throw exceptions if layout resources use bare string literals instead of string resource IDs.
     * <p/>
     * To enable or disable i18n-strict mode for specific test cases, annotate them with
     * {@link com.xtremelabs.robolectric.annotation.EnableStrictI18n} or
     * {@link com.xtremelabs.robolectric.annotation.DisableStrictI18n}.
     * <p/>
     *
     * By default, I18n-strict mode is disabled.
     * 
     * @param method
     * @param robolectricConfig
     */
    private void setupI18nStrictState(Method method, RobolectricConfig robolectricConfig) {
    	// Global
    	boolean strictI18n = globalI18nStrictEnabled();
 
    	// Test case class
    	Annotation[] annos = method.getDeclaringClass().getAnnotations();
    	strictI18n = lookForI18nAnnotations(strictI18n, annos);
    	
    	// Test case methods
    	annos = method.getAnnotations();
    	strictI18n = lookForI18nAnnotations(strictI18n, annos);

		robolectricConfig.setStrictI18n(strictI18n);
    }
    
    /**
     * Default implementation of global switch for i18n-strict mode.
     * To enable i18n-strict mode globally, set the system property
     * "robolectric.strictI18n" to true. This can be done via java
     * system properties in either Ant or Maven.
     * <p/>
     * Subclasses can override this method and establish their own policy
     * for enabling i18n-strict mode.
     * 
     * @return
     */
    protected boolean globalI18nStrictEnabled() {
    	return Boolean.valueOf(System.getProperty("robolectric.strictI18n"));
    }

    /**
     * As test methods are loaded by the delegate's class loader, the normal
 	 * method#isAnnotationPresent test fails. Look at string versions of the
     * annotation names to test for their presence.
     * 
     * @param strictI18n
     * @param annos
     * @return
     */
	private boolean lookForI18nAnnotations(boolean strictI18n, Annotation[] annos) {
		for ( int i = 0; i < annos.length; i++ ) {
    		String name = annos[i].annotationType().getName();
    		if (name.equals("com.xtremelabs.robolectric.annotation.EnableStrictI18n")) {
    			strictI18n = true;
    			break;
    		}
    		if (name.equals("com.xtremelabs.robolectric.annotation.DisableStrictI18n")) {
    			strictI18n = false;
    			break;
    		}
    	}
		return strictI18n;
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

    private ResourceLoader createResourceLoader(final RobolectricConfig robolectricConfig) {
        ResourceLoader resourceLoader = resourceLoaderForRootAndDirectory.get(robolectricConfig);
        if (resourceLoader == null) {
            try {
                robolectricConfig.validate();

                String rClassName = robolectricConfig.getRClassName();
                Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(robolectricConfig.getRealSdkVersion(), rClass, robolectricConfig.getResourceDirectory(), robolectricConfig.getAssetsDirectory());
                resourceLoaderForRootAndDirectory.put(robolectricConfig, resourceLoader);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        resourceLoader.setStrictI18n(robolectricConfig.getStrictI18n());
        return resourceLoader;
    }

    private String findResourcePackageName(final File projectManifestFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(projectManifestFile);

        String projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();

        return projectPackage + ".R";
    }
       
    /*
     * Specifies what database to use for testing (ex: H2 or Sqlite),
     * this will load H2 by default, the SQLite TestRunner version will override this.
     */
    protected DatabaseMap setupDatabaseMap(Class<?> testClass, DatabaseMap map) {
    	DatabaseMap dbMap = map;
  
    	if (testClass.isAnnotationPresent(UsingDatabaseMap.class)) {
	    	UsingDatabaseMap usingMap = testClass.getAnnotation(UsingDatabaseMap.class);
	    	if(usingMap.value()!=null){
	    		dbMap = Robolectric.newInstanceOf(usingMap.value());
	    	} else {
	    		if (dbMap==null)
		    		throw new RuntimeException("UsingDatabaseMap annotation value must provide a class implementing DatabaseMap");
	    	}
    	}
    	return dbMap;
    }
    
    public DatabaseMap getDatabaseMap() {
		return databaseMap;
	}

	public void setDatabaseMap(DatabaseMap databaseMap) {
		this.databaseMap = databaseMap;
	}
	
}
