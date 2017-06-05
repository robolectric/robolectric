package transapps.mapi.osm.provider;

import java.io.File;
import java.io.FileWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricContext;
import org.robolectric.RobolectricTestRunner;

/**
 * A {@link RobolectricTestRunner} that will run test cases for non apk projects.  This is useful in situations where
 * A simple jar library project uses some android code and needs non stubbed or shadow classes from Robolectric
 * 
 * @author mriley
 */
public class NonApkTestRunner extends RobolectricTestRunner {
	
	// ===========================================================
	// Constants
	// ===========================================================
	
	/**
	 * Root dir for the fake apk directory
	 */
	private static final String BASE_FAKEAPK_DIR = System.getProperty("java.io.tmpdir");
	/**
	 * Directories required by Robolectric
	 */
	private static final String[] REQUIRED_APK_DIRS = {File.separator + "res"+File.separator+"values"+File.separator, File.separator+"assets"+File.separator};

	
	// ===========================================================
	// Inner classes
	// ===========================================================
	
	/**
	 * Allows test cases to define some info that should be in the generated
	 * AndroidManifest.xml  
	 * 
	 * @author mriley
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Manifest {
		/**
		 * @return The packageName that should be in the android manifest.  If not
		 * provided, the package name for the test class will be used.
		 */
		String packageName() default "";
		
		/**
		 * @return The minSdkVersion that should be in the android manifest
		 */
		int minSdkVersion() default 1;
		
		/**
		 * @return The targetSdkVersion that should be in the android manifest
		 */
		int targetSdkVersion() default 1;
	}
	
	/**
	 * Special {@link RobolectricContext} that will create a manifest based on a
	 * generated android project dir
	 * 
	 * @author mriley
	 */
    private static final class Context extends RobolectricContext {
    	@Override
    	protected AndroidManifest createAppManifest() {
    		return new AndroidManifest(buildFakeApkDir(ContextFactory.testClass));
    	}
    }
    
    /**
     * Special {@link Factory} for our {@link Context}
     * 
     * @author mriley
     */
    private static final class ContextFactory implements RobolectricContext.Factory {
    	
    	private static Class<?> testClass; 
    	
    	public ContextFactory( Class<?> testClass ) {
    		ContextFactory.testClass = testClass;
		}
    	
    	@Override
    	public RobolectricContext create() {
    		return new Context();
    	}
    }
	
    // ===========================================================
	// Static methods
	// ===========================================================
    
    /**
     * Builds a fake apk dir complete with generated AndroidManifest.xml
     * 
     * @param testClass
     * @return
     */
    private static File buildFakeApkDir(Class<?> testClass) {
    	
    	// guess the manifest things
    	Manifest annotation = testClass.getAnnotation(Manifest.class);
    	int target = 8;
    	int min = 8;
    	String pkg = "";
    	if( annotation != null ) {
    		target = annotation.targetSdkVersion();
    		min = annotation.minSdkVersion();
    		pkg = annotation.packageName();
    	}    	
		if( "".equals(pkg.trim()) ) {
			pkg = testClass.getPackage().getName();
		}
    	
		// do some validation
		try {
			testClass.getClassLoader().loadClass(pkg + ".R");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Robolectric wants a " + pkg + ".R.class.  Please add an empty R.java.", e);
		}
		
		// make the dir structure
        File tmpDir = mkDir(mkBaseDir(pkg));
        for( String s : REQUIRED_APK_DIRS ) {
        	mkDir(new File(tmpDir + s));
        }

        // generate the AndroidManifest.xml
        File manifest = new File(tmpDir, "AndroidManifest.xml");
        FileWriter writer = null;
        try {
            writer = new FileWriter(manifest);
            writer.write("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\""+
            		pkg+"\" android:versionCode=\"1\" android:versionName=\"TEST\"> <uses-sdk android:minSdkVersion=\""+
            		min+"\" android:targetSdkVersion=\""+target+"\"/></manifest>");
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        } finally {
            if( writer != null ) try {writer.close();} catch ( Exception ignored ) {}
        }
        return tmpDir;
    }

	private static File mkBaseDir(String pkg) {
		File baseDir = new File(BASE_FAKEAPK_DIR + "/" + pkg +"/");
		deepClean(baseDir);
		return baseDir;
	}

	private static File mkDir(File tmpDir) {
		if( !tmpDir.exists() && !tmpDir.mkdirs() ) {
            throw new RuntimeException("Failed to find or create app dir " + tmpDir);
        }
		return tmpDir;
	}
	
	private static void deepClean( File file ) {
		if( file.exists() ) {
			if( file.isDirectory() ) {
				File[] children = file.listFiles();
				if( children != null ) {
					for( File child : children ) {
						deepClean(child);
					}
				}
			}
			if( !file.delete() ) {
				throw new RuntimeException("Failed to clean fake apk dir.  Failed to delete " + file);
			}
		}
	}
	
	// ===========================================================
	// CTORs
	// ===========================================================
	
    public NonApkTestRunner(final Class<?> testClass) throws InitializationError {
        super(RobolectricContext.bootstrap(NonApkTestRunner.class, testClass, new ContextFactory(testClass)));
    }
}
