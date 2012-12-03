package com.xtremelabs.robolectric.res;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import android.R;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowContextWrapper;
import com.xtremelabs.robolectric.util.I18nException;
import com.xtremelabs.robolectric.util.PropertiesHelper;

public class ResourceLoader {
	private static final FileFilter MENU_DIR_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( File file ) {
			return isMenuDirectory( file.getPath() );
		}
	};
	private static final FileFilter LAYOUT_DIR_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( File file ) {
			return isLayoutDirectory( file.getPath() );
		}
	};
	private static final FileFilter DRAWABLE_DIR_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept( File file ) {
			return isDrawableDirectory( file.getPath() );
		}
	};

	private File resourceDir;
	private File assetsDir;
	private int sdkVersion;
	private Class rClass;

	private final ResourceExtractor resourceExtractor;
	private ViewLoader viewLoader;
	private MenuLoader menuLoader;
	private XmlFileLoader xmlFileLoader;
	private PreferenceLoader preferenceLoader;
	private final StringResourceLoader stringResourceLoader;
	private final PluralResourceLoader pluralResourceLoader;
	private final StringArrayResourceLoader stringArrayResourceLoader;
	private final AttrResourceLoader attrResourceLoader;
	private final ColorResourceLoader colorResourceLoader;
	private final DrawableResourceLoader drawableResourceLoader;
	private final RawResourceLoader rawResourceLoader;
	private final DimenResourceLoader dimenResourceLoader;
	private final IntegerResourceLoader integerResourceLoader;
	private final BoolResourceLoader boolResourceLoader;
	private boolean isInitialized = false;
	private boolean strictI18n = false;
	
	private final Set<Integer> ninePatchDrawableIds = new HashSet<Integer>();

	public ResourceLoader( int sdkVersion, Class rClass, File resourceDir, File assetsDir) throws Exception {
		this.sdkVersion = sdkVersion;
		this.assetsDir = assetsDir;
		this.rClass = rClass;
		
		resourceExtractor = new ResourceExtractor();
		resourceExtractor.addLocalRClass( rClass );
		resourceExtractor.addSystemRClass( R.class );

		stringResourceLoader = new StringResourceLoader( resourceExtractor );
		pluralResourceLoader = new PluralResourceLoader( resourceExtractor, stringResourceLoader );
		stringArrayResourceLoader = new StringArrayResourceLoader( resourceExtractor, stringResourceLoader );
		colorResourceLoader = new ColorResourceLoader( resourceExtractor );
		attrResourceLoader = new AttrResourceLoader( resourceExtractor );
		drawableResourceLoader = new DrawableResourceLoader( resourceExtractor, resourceDir );
		rawResourceLoader = new RawResourceLoader( resourceExtractor, resourceDir );
		dimenResourceLoader = new DimenResourceLoader( resourceExtractor );
		integerResourceLoader = new IntegerResourceLoader( resourceExtractor );
		boolResourceLoader = new BoolResourceLoader( resourceExtractor );

		this.resourceDir = resourceDir;
	}

	public void setStrictI18n( boolean strict ) {
		this.strictI18n = strict;
		if ( viewLoader != null ) {
			viewLoader.setStrictI18n( strict );
		}
		if ( menuLoader != null ) {
			menuLoader.setStrictI18n( strict );
		}
		if ( preferenceLoader != null ) {
			preferenceLoader.setStrictI18n( strict );
		}
		if ( xmlFileLoader != null ) {
			xmlFileLoader.setStrictI18n( strict );
		}
	}

	public boolean getStrictI18n() {
		return strictI18n;
	}

	private void init() {
		if ( isInitialized ) {
			return;
		}
		
		try {
			if ( resourceDir != null ) {
				viewLoader = new ViewLoader( resourceExtractor, attrResourceLoader );
				menuLoader = new MenuLoader( resourceExtractor, attrResourceLoader );
				preferenceLoader = new PreferenceLoader( resourceExtractor );
				xmlFileLoader = new XmlFileLoader( resourceExtractor );

				viewLoader.setStrictI18n( strictI18n );
				menuLoader.setStrictI18n( strictI18n );
				preferenceLoader.setStrictI18n( strictI18n );
				xmlFileLoader.setStrictI18n( strictI18n );

				File systemResourceDir = getSystemResourceDir( getPathToAndroidResources() );
				File localValueResourceDir = getValueResourceDir( resourceDir, null, true );
				File systemValueResourceDir = getValueResourceDir( systemResourceDir, null, false );
				File preferenceDir = getPreferenceResourceDir( resourceDir );

				loadStringResources( localValueResourceDir, systemValueResourceDir );
				loadPluralsResources( localValueResourceDir, systemValueResourceDir );
				loadValueResources( localValueResourceDir, systemValueResourceDir );
				loadDimenResources( localValueResourceDir, systemValueResourceDir );
				loadIntegerResource( localValueResourceDir, systemValueResourceDir );
				loadBooleanResources( localValueResourceDir, systemValueResourceDir );
				loadViewResources( systemResourceDir, resourceDir );
				loadMenuResources( resourceDir );
				loadDrawableResources( resourceDir );
				loadPreferenceResources( preferenceDir );
				loadXmlFileResources( preferenceDir );
				
				listNinePatchResources(ninePatchDrawableIds, resourceDir);
			} else {
				viewLoader = null;
				menuLoader = null;
				preferenceLoader = null;
				xmlFileLoader = null;
			}
		} catch ( I18nException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		}
		isInitialized = true;
	}

	/**
	 * Reload values resources, include String, Plurals, Dimen, Prefs, Menu
	 *
	 * @param locale
	 */
	public void reloadValuesResouces( String qualifiers ) {
		
		File systemResourceDir = getSystemResourceDir( getPathToAndroidResources() );
		File localValueResourceDir = getValueResourceDir( resourceDir, qualifiers, true );
		File systemValueResourceDir = getValueResourceDir( systemResourceDir, null, false );
		File preferenceDir = getPreferenceResourceDir( resourceDir );
		
		try {
			loadStringResources( localValueResourceDir, systemValueResourceDir );
			loadBooleanResources( localValueResourceDir, systemValueResourceDir );
			loadPluralsResources( localValueResourceDir, systemValueResourceDir );
			loadValueResources( localValueResourceDir, systemValueResourceDir );
			loadDimenResources( localValueResourceDir, systemValueResourceDir );
			loadIntegerResource( localValueResourceDir, systemValueResourceDir );
			loadMenuResources( resourceDir );
			loadPreferenceResources( preferenceDir );
		} catch ( Exception e ) {
			throw new RuntimeException( e );
		} 
	}
	
	private File getSystemResourceDir( String pathToAndroidResources ) {
		return pathToAndroidResources != null ? new File( pathToAndroidResources ) : null;
	}

	private void loadStringResources( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader stringResourceDocumentLoader = new DocumentLoader( this.stringResourceLoader );
		loadValueResourcesFromDirs( stringResourceDocumentLoader, localResourceDir, systemValueResourceDir );
	}

    private void loadBooleanResources( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader boolResourceDocumentLoader = new DocumentLoader( this.boolResourceLoader );
		loadValueResourcesFromDirs( boolResourceDocumentLoader, localResourceDir, systemValueResourceDir );
    }

	private void loadPluralsResources( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader stringResourceDocumentLoader = new DocumentLoader( this.pluralResourceLoader );
		loadValueResourcesFromDirs( stringResourceDocumentLoader, localResourceDir, systemValueResourceDir );
	}

	private void loadValueResources( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader valueResourceLoader = new DocumentLoader( stringArrayResourceLoader, colorResourceLoader,
				attrResourceLoader );
		loadValueResourcesFromDirs( valueResourceLoader, localResourceDir, systemValueResourceDir );
	}

	private void loadDimenResources( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader dimenResourceDocumentLoader = new DocumentLoader( this.dimenResourceLoader );
		loadValueResourcesFromDirs( dimenResourceDocumentLoader, localResourceDir, systemValueResourceDir );
	}

	private void loadIntegerResource( File localResourceDir, File systemValueResourceDir ) throws Exception {
		DocumentLoader integerResourceDocumentLoader = new DocumentLoader( this.integerResourceLoader );
		loadValueResourcesFromDirs( integerResourceDocumentLoader, localResourceDir, systemValueResourceDir );
	}

	private void loadViewResources( File systemResourceDir, File xmlResourceDir ) throws Exception {
		DocumentLoader viewDocumentLoader = new DocumentLoader( viewLoader );
		loadLayoutResourceXmlSubDirs( viewDocumentLoader, xmlResourceDir, false );
		loadLayoutResourceXmlSubDirs( viewDocumentLoader, systemResourceDir, true );
	}

	private void loadMenuResources( File xmlResourceDir ) throws Exception {
		DocumentLoader menuDocumentLoader = new DocumentLoader( menuLoader );
		loadMenuResourceXmlDirs( menuDocumentLoader, xmlResourceDir );
	}

	private void loadDrawableResources( File xmlResourceDir ) throws Exception {
		DocumentLoader drawableDocumentLoader = new DocumentLoader( drawableResourceLoader );
		loadDrawableResourceXmlDirs( drawableDocumentLoader, xmlResourceDir );
	}

	private void loadPreferenceResources( File xmlResourceDir ) throws Exception {
		if ( xmlResourceDir.exists() ) {
			DocumentLoader preferenceDocumentLoader = new DocumentLoader( preferenceLoader );
			preferenceDocumentLoader.loadResourceXmlDir( xmlResourceDir );
		}
	}
	
	/**
	 * All the Xml files should be loaded. 
	 */
	private void loadXmlFileResources( File xmlResourceDir ) throws Exception {
		if ( xmlResourceDir.exists() ) {
			DocumentLoader xmlFileDocumentLoader = 
					new DocumentLoader( xmlFileLoader );
			xmlFileDocumentLoader.loadResourceXmlDir( xmlResourceDir );
		}
	}

	private void loadLayoutResourceXmlSubDirs( DocumentLoader layoutDocumentLoader, File xmlResourceDir, boolean isSystem )
			throws Exception {
		if ( xmlResourceDir != null ) {
			layoutDocumentLoader.loadResourceXmlDirs( isSystem, xmlResourceDir.listFiles( LAYOUT_DIR_FILE_FILTER ) );
		}
	}

	private void loadMenuResourceXmlDirs( DocumentLoader menuDocumentLoader, File xmlResourceDir ) throws Exception {
		if ( xmlResourceDir != null ) {
			menuDocumentLoader.loadResourceXmlDirs( xmlResourceDir.listFiles( MENU_DIR_FILE_FILTER ) );
		}
	}

	private void loadDrawableResourceXmlDirs( DocumentLoader drawableResourceLoader, File xmlResourceDir ) throws Exception {
		if ( xmlResourceDir != null ) {
			drawableResourceLoader.loadResourceXmlDirs( xmlResourceDir.listFiles( DRAWABLE_DIR_FILE_FILTER ) );
		}
	}

	private void loadValueResourcesFromDirs( DocumentLoader documentLoader, File localValueResourceDir,
			File systemValueResourceDir ) throws Exception {
		loadValueResourcesFromDir( documentLoader, localValueResourceDir );
		loadSystemResourceXmlDir( documentLoader, systemValueResourceDir );
	}

	private void loadValueResourcesFromDir( DocumentLoader documentloader, File xmlResourceDir ) throws Exception {
		if ( xmlResourceDir != null ) {
			documentloader.loadResourceXmlDir( xmlResourceDir );
		}
	}

	private void loadSystemResourceXmlDir( DocumentLoader documentLoader, File stringResourceDir ) throws Exception {
		if ( stringResourceDir != null ) {
			documentLoader.loadSystemResourceXmlDir( stringResourceDir );
		}
	}

	private File getValueResourceDir( File xmlResourceDir, String qualifiers, boolean isLocal ) {
		String valuesDir = "values";
		if( qualifiers != null && !qualifiers.isEmpty() && isLocal ){
			valuesDir += "-"+ qualifiers;
		}
		File result = ( xmlResourceDir != null ) ? new File( xmlResourceDir, valuesDir ) : null;
		if( result == null || !result.exists() ){
			throw new RuntimeException("Couldn't find value resource directory: " + result.getAbsolutePath() );
		}
		return result;
	}

	private File getPreferenceResourceDir( File xmlResourceDir ) {
		return xmlResourceDir != null ? new File( xmlResourceDir, "xml" ) : null;
	}
	
	private String getPathToAndroidResources() {
		String resourcePath;
		if ( ( resourcePath = getAndroidResourcePathFromLocalProperties() ) != null ) {
			return resourcePath;
		} else if ( ( resourcePath = getAndroidResourcePathFromSystemEnvironment() ) != null ) {
			return resourcePath;
		} else if ( ( resourcePath = getAndroidResourcePathFromSystemProperty() ) != null ) {
			return resourcePath;
		} else if ( ( resourcePath = getAndroidResourcePathByExecingWhichAndroid() ) != null ) {
			return resourcePath;
		}

		System.out.println( "WARNING: Unable to find path to Android SDK" );
		return null;
	}

	private String getAndroidResourcePathFromLocalProperties() {
		// Hand tested
		// This is the path most often taken by IntelliJ
		File rootDir = resourceDir.getParentFile();
		String localPropertiesFileName = "local.properties";
		File localPropertiesFile = new File( rootDir, localPropertiesFileName );
		if ( !localPropertiesFile.exists() ) {
			localPropertiesFile = new File( localPropertiesFileName );
		}
		if ( localPropertiesFile.exists() ) {
			Properties localProperties = new Properties();
			try {
				localProperties.load( new FileInputStream( localPropertiesFile ) );
				PropertiesHelper.doSubstitutions( localProperties );
				String sdkPath = localProperties.getProperty( "sdk.dir" );
				if ( sdkPath != null ) {
					return getResourcePathFromSdkPath( sdkPath );
				}
			} catch ( IOException e ) {
				// fine, we'll try something else
			}
		}
		return null;
	}

	private String getAndroidResourcePathFromSystemEnvironment() {
		// Hand tested
		String resourcePath = System.getenv().get( "ANDROID_HOME" );
		if ( resourcePath != null ) {
			return new File( resourcePath, getAndroidResourceSubPath() ).toString();
		}
		return null;
	}

	private String getAndroidResourcePathFromSystemProperty() {
		// this is used by the android-maven-plugin
		String resourcePath = System.getProperty( "android.sdk.path" );
		if ( resourcePath != null ) {
			return new File( resourcePath, getAndroidResourceSubPath() ).toString();
		}
		return null;
	}

	private String getAndroidResourcePathByExecingWhichAndroid() {
		// Hand tested
		// Should always work from the command line. Often fails in IDEs because
		// they don't pass the full PATH in the environment
		try {
			Process process = Runtime.getRuntime().exec( new String[] { "which", "android" } );
			String sdkPath = new BufferedReader( new InputStreamReader( process.getInputStream() ) ).readLine();
			if ( sdkPath != null && sdkPath.endsWith( "tools/android" ) ) {
				return getResourcePathFromSdkPath( sdkPath.substring( 0, sdkPath.indexOf( "tools/android" ) ) );
			}
		} catch ( IOException e ) {
			// fine we'll try something else
		}
		return null;
	}

	private String getResourcePathFromSdkPath( String sdkPath ) {
		File androidResourcePath = new File( sdkPath, getAndroidResourceSubPath() );
		return androidResourcePath.exists() ? androidResourcePath.toString() : null;
	}

	private String getAndroidResourceSubPath() {
		return "platforms/android-" + sdkVersion + "/data/res";
	}

	static boolean isLayoutDirectory( String path ) {
		return path.contains( File.separator + "layout" );
	}

	static boolean isDrawableDirectory( String path ) {
		return path.contains( File.separator + "drawable" );
	}

	static boolean isMenuDirectory( String path ) {
		return path.contains( File.separator + "menu" );
	}

	/*
	 * For tests only...
	 */
	protected ResourceLoader( StringResourceLoader stringResourceLoader ) {
		resourceExtractor = new ResourceExtractor();
		this.stringResourceLoader = stringResourceLoader;
		pluralResourceLoader = null;
		viewLoader = null;
		stringArrayResourceLoader = null;
		attrResourceLoader = null;
		colorResourceLoader = null;
		drawableResourceLoader = null;
		rawResourceLoader = null;
		dimenResourceLoader = null;
		integerResourceLoader = null;
		boolResourceLoader = null;
	}

	public static ResourceLoader getFrom( Context context ) {
		ResourceLoader resourceLoader = shadowOf( context.getApplicationContext() ).getResourceLoader();
		resourceLoader.init();
		return resourceLoader;
	}

	public String getNameForId( int viewId ) {
		init();
		return resourceExtractor.getResourceName( viewId );
	}

	public View inflateView( Context context, int resource, ViewGroup viewGroup ) {
		init();
		return viewLoader.inflateView( context, resource, viewGroup );
	}

	public int getColorValue( int id ) {
		init();
		return colorResourceLoader.getValue( id );
	}

	public String getStringValue( int id ) {
		init();
		return stringResourceLoader.getValue( id );
	}

	public String getPluralStringValue( int id, int quantity ) {
		init();
		return pluralResourceLoader.getValue( id, quantity );
	}

	public float getDimenValue( int id ) {
		init();
		return dimenResourceLoader.getValue( id );
	}

	public int getIntegerValue( int id ) {
		init();
		return integerResourceLoader.getValue( id );
	}
	
	public boolean getBooleanValue( int id ) {
		init();
		return boolResourceLoader.getValue( id );
	}
	
	public XmlResourceParser getXml( int id ) {
		init();
		return xmlFileLoader.getXml( id );
	}

	public boolean isDrawableXml( int resourceId ) {
		init();
		return drawableResourceLoader.isXml( resourceId );
	}

    public boolean isAnimatableXml( int resourceId ) {
        init();
        return drawableResourceLoader.isAnimationDrawable( resourceId );
    }

	public int[] getDrawableIds( int resourceId ) {
		init();
		return drawableResourceLoader.getDrawableIds( resourceId );
	}

	public Drawable getXmlDrawable( int resourceId ) {
		return drawableResourceLoader.getXmlDrawable( resourceId );
	}

	public Drawable getAnimDrawable( int resourceId ) {
		return getInnerRClassDrawable( resourceId, "$anim", AnimationDrawable.class );
	}

	public Drawable getColorDrawable( int resourceId ) {
		return getInnerRClassDrawable( resourceId, "$color", ColorDrawable.class );
	}

	@SuppressWarnings("rawtypes")
	private Drawable getInnerRClassDrawable( int drawableResourceId, String suffix, Class returnClass ) {
		ShadowContextWrapper shadowApp = Robolectric.shadowOf( Robolectric.application );
		Class rClass = shadowApp.getResourceLoader().getLocalRClass();

		// Check to make sure there is actually an R Class, if not
		// return just a BitmapDrawable
		if ( rClass == null ) {
			return null;
		}

		// Load the Inner Class for interrogation
		Class animClass = null;
		try {
			animClass = Class.forName( rClass.getCanonicalName() + suffix );
		} catch ( ClassNotFoundException e ) {
			return null;
		}

		// Try to find the passed in resource ID
		try {
			for ( Field field : animClass.getDeclaredFields() ) {
				if ( field.getInt( animClass ) == drawableResourceId ) {
					return ( Drawable ) returnClass.newInstance();
				}
			}
		} catch ( Exception e ) {
		}

		return null;
	}
	
	public boolean isNinePatchDrawable(int drawableResourceId) {
		return ninePatchDrawableIds.contains(drawableResourceId);
	}
	
	/**
	 * Returns a collection of resource IDs for all nine-patch drawables
	 * in the project.
	 * 
	 * @param resourceIds
	 * @param dir
	 */
	private void listNinePatchResources(Set<Integer> resourceIds, File dir) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory() && isDrawableDirectory(f.getPath())) {
					listNinePatchResources(resourceIds, f);
				} else {
					String name = f.getName();
					if (name.endsWith(".9.png")) {
						String[] tokens = name.split("\\.9\\.png$");
						resourceIds.add(resourceExtractor.getResourceId("@drawable/" + tokens[0]));
					}
				}
			}
		}
	}

	public InputStream getRawValue( int id ) {
		init();
		return rawResourceLoader.getValue( id );
	}

	public String[] getStringArrayValue( int id ) {
		init();
		return stringArrayResourceLoader.getArrayValue( id );
	}

	public void inflateMenu( Context context, int resource, Menu root ) {
		init();
		menuLoader.inflateMenu( context, resource, root );
	}

	public PreferenceScreen inflatePreferences( Context context, int resourceId ) {
		init();
		return preferenceLoader.inflatePreferences( context, resourceId );
	}

	public File getAssetsBase() {
		return assetsDir;
	}

	@SuppressWarnings("rawtypes")
	public Class getLocalRClass() {
		return rClass;
	}

	public void setLocalRClass( Class clazz ) {
		rClass = clazz;
	}

	public ResourceExtractor getResourceExtractor() {
		return resourceExtractor;
	}

	public ViewLoader.ViewNode getLayoutViewNode( String layoutName ) {
		return viewLoader.viewNodesByLayoutName.get( layoutName );
	}

	public void setLayoutQualifierSearchPath( String... locations ) {
		init();
		viewLoader.setLayoutQualifierSearchPath( locations );
	}
}
