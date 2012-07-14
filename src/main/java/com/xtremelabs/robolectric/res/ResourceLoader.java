package com.xtremelabs.robolectric.res;

import android.R;
import android.content.Context;
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

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ResourceLoader {
    private static final FileFilter MENU_DIR_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isMenuDirectory(file.getPath());
        }
    };
    private static final FileFilter LAYOUT_DIR_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isLayoutDirectory(file.getPath());
        }
    };
    private static final FileFilter DRAWABLE_DIR_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return isDrawableDirectory(file.getPath());
        }
    };

    private List<File> resourcePath;
    private File assetsDir;
    private int sdkVersion;
    private Class rClass;

    private final ResourceExtractor resourceExtractor;
    private ViewLoader viewLoader;
    private MenuLoader menuLoader;
    private PreferenceLoader preferenceLoader;
    private final StringResourceLoader stringResourceLoader;
    private final IntegerResourceLoader integerResourceLoader;
    private final PluralResourceLoader pluralResourceLoader;
    private final StringArrayResourceLoader stringArrayResourceLoader;
    private final AttrResourceLoader attrResourceLoader;
    private final ColorResourceLoader colorResourceLoader;
    private final DrawableResourceLoader drawableResourceLoader;
    private final List<RawResourceLoader> rawResourceLoaders = new ArrayList<RawResourceLoader>();
    private boolean isInitialized = false;
    private boolean strictI18n = false;

    // TODO: get these value from the xml resources instead [xw 20101011]
    public final Map<Integer, Integer> dimensions = new HashMap<Integer, Integer>();

    @Deprecated
    public ResourceLoader(int sdkVersion, Class rClass, File resourceDir, File assetsDir) throws Exception {
        this(sdkVersion, rClass, safeFileList(resourceDir), assetsDir);
    }

    private static List<File> safeFileList(File resourceDir) {
        return resourceDir == null ? Collections.<File> emptyList() : Collections.singletonList(resourceDir);
    }

    public ResourceLoader(int sdkVersion, Class rClass, List<File> resourcePath, File assetsDir) throws Exception {
        this.sdkVersion = sdkVersion;
        this.assetsDir = assetsDir;
        this.rClass = rClass;
        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(rClass);
        resourceExtractor.addSystemRClass(R.class);

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        integerResourceLoader = new IntegerResourceLoader(resourceExtractor);
        pluralResourceLoader = new PluralResourceLoader(resourceExtractor, stringResourceLoader);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        drawableResourceLoader = new DrawableResourceLoader(resourceExtractor);

        this.resourcePath = Collections.unmodifiableList(resourcePath);
    }
    
    public void setStrictI18n(boolean strict) {
    	this.strictI18n = strict;
    	if (viewLoader != null ) 	   { viewLoader.setStrictI18n(strict); }
    	if (menuLoader != null ) 	   { menuLoader.setStrictI18n(strict); }
    	if (preferenceLoader != null ) { preferenceLoader.setStrictI18n(strict); }
    }
    
    public boolean getStrictI18n() { return strictI18n; }
    
    private void init() {
        if (isInitialized) {
            return;
        }

        if (!resourcePath.isEmpty()) {
            try {
                viewLoader = new ViewLoader(resourceExtractor, attrResourceLoader);
                menuLoader = new MenuLoader(resourceExtractor, attrResourceLoader);
                preferenceLoader = new PreferenceLoader(resourceExtractor);
                
                viewLoader.setStrictI18n(strictI18n);
                menuLoader.setStrictI18n(strictI18n);
                preferenceLoader.setStrictI18n(strictI18n);

                File systemResourceDir = getSystemResourceDir(getPathToAndroidResources());
                File systemValueResourceDir = getValueResourceDir(systemResourceDir);

                loadStringResources(systemValueResourceDir, stringResourceLoader, true);
                loadIntegerResources(systemValueResourceDir, integerResourceLoader, true);
                loadPluralsResources(systemValueResourceDir, true);
                loadValueResources(systemValueResourceDir, stringArrayResourceLoader, colorResourceLoader, attrResourceLoader, true);
                loadViewResources(systemResourceDir, viewLoader, true);

                for (File resourceDir : resourcePath) {
                    File localValueResourceDir = getValueResourceDir(resourceDir);
                    RawResourceLoader rawResourceLoader = new RawResourceLoader(resourceExtractor, resourceDir);
                    File preferenceDir = getPreferenceResourceDir(resourceDir);

                    loadStringResources(localValueResourceDir, stringResourceLoader, false);
                    loadIntegerResources(localValueResourceDir, integerResourceLoader, false);
                    loadPluralsResources(localValueResourceDir, false);
                    loadValueResources(localValueResourceDir, stringArrayResourceLoader, colorResourceLoader, attrResourceLoader, false);
                    loadViewResources(resourceDir, viewLoader, false);
                    loadMenuResources(resourceDir, menuLoader);
                    loadDrawableResources(resourceDir);
                    loadPreferenceResources(preferenceDir);
                    loadOtherResources(resourceDir);

                    rawResourceLoaders.add(rawResourceLoader);
                }

            } catch(I18nException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        isInitialized = true;
    }

    private File getSystemResourceDir(String pathToAndroidResources) {
        return pathToAndroidResources != null ? new File(pathToAndroidResources) : null;
    }

    private void loadStringResources(File resourceDir, StringResourceLoader stringResourceLoader, boolean system) throws Exception {
        DocumentLoader stringResourceDocumentLoader = new DocumentLoader(stringResourceLoader);
        loadValueResourcesFromDirs(stringResourceDocumentLoader, resourceDir, system);
    }

    private void loadIntegerResources(File resourceDir, IntegerResourceLoader integerResourceLoader, boolean system) throws Exception {
        DocumentLoader integerResourceDocumentLoader = new DocumentLoader(integerResourceLoader);
        loadValueResourcesFromDirs(integerResourceDocumentLoader, resourceDir, system);
    }

    private void loadPluralsResources(File resourceDir, boolean system) throws Exception {
        DocumentLoader stringResourceDocumentLoader = new DocumentLoader(this.pluralResourceLoader);
        loadValueResourcesFromDirs(stringResourceDocumentLoader, resourceDir, system);
    }

    private void loadValueResources(File resourceDir, StringArrayResourceLoader stringArrayResourceLoader, ColorResourceLoader colorResourceLoader, AttrResourceLoader attrResourceLoader, boolean system) throws Exception {
        DocumentLoader valueResourceLoader = new DocumentLoader(stringArrayResourceLoader, colorResourceLoader, attrResourceLoader);
        loadValueResourcesFromDirs(valueResourceLoader, resourceDir, system);
    }

    private void loadViewResources(File resourceDir, ViewLoader viewLoader, boolean system) throws Exception {
        DocumentLoader viewDocumentLoader = new DocumentLoader(viewLoader);
        loadLayoutResourceXmlSubDirs(viewDocumentLoader, resourceDir, system);
    }

    private void loadMenuResources(File xmlResourceDir, MenuLoader menuLoader) throws Exception {
        DocumentLoader menuDocumentLoader = new DocumentLoader(menuLoader);
        loadMenuResourceXmlDirs(menuDocumentLoader, xmlResourceDir);
    }

    private void loadDrawableResources(File xmlResourceDir) throws Exception {
        DocumentLoader drawableDocumentLoader = new DocumentLoader(drawableResourceLoader);
        loadDrawableResourceXmlDirs(drawableDocumentLoader, xmlResourceDir);
    }

    private void loadPreferenceResources(File xmlResourceDir) throws Exception {
        if (xmlResourceDir.exists()) {
            DocumentLoader preferenceDocumentLoader = new DocumentLoader(preferenceLoader);
            preferenceDocumentLoader.loadResourceXmlDir(xmlResourceDir);
        }
    }

    protected void loadOtherResources(File xmlResourceDir) {
    }

    private void loadLayoutResourceXmlSubDirs(DocumentLoader layoutDocumentLoader, File xmlResourceDir, boolean isSystem) throws Exception {
        if (xmlResourceDir != null) {
            layoutDocumentLoader.loadResourceXmlDirs(isSystem, xmlResourceDir.listFiles(LAYOUT_DIR_FILE_FILTER));
        }
    }

    private void loadMenuResourceXmlDirs(DocumentLoader menuDocumentLoader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null) {
            menuDocumentLoader.loadResourceXmlDirs(xmlResourceDir.listFiles(MENU_DIR_FILE_FILTER));
        }
    }

    private void loadDrawableResourceXmlDirs(DocumentLoader drawableResourceLoader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null) {
            drawableResourceLoader.loadResourceXmlDirs(xmlResourceDir.listFiles(DRAWABLE_DIR_FILE_FILTER));
        }
    }

    private void loadValueResourcesFromDirs(DocumentLoader documentLoader, File resourceDir, boolean system) throws Exception {
        if (system) {
            loadSystemResourceXmlDir(documentLoader, resourceDir);
        } else {
            loadValueResourcesFromDir(documentLoader, resourceDir);
        }
    }

    private void loadValueResourcesFromDir(DocumentLoader documentloader, File xmlResourceDir) throws Exception {
        if (xmlResourceDir != null && xmlResourceDir.exists()) {
            documentloader.loadResourceXmlDir(xmlResourceDir);
        }
    }

    private void loadSystemResourceXmlDir(DocumentLoader documentLoader, File stringResourceDir) throws Exception {
        if (stringResourceDir != null) {
            documentLoader.loadSystemResourceXmlDir(stringResourceDir);
        }
    }

    private File getValueResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "values")
                : null;
    }

    private File getPreferenceResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "xml") : null;
    }

    private String getPathToAndroidResources() {
        String resourcePath;
        if ((resourcePath = getAndroidResourcePathFromLocalProperties()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemEnvironment()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathFromSystemProperty()) != null) {
            return resourcePath;
        } else if ((resourcePath = getAndroidResourcePathByExecingWhichAndroid()) != null) {
            return resourcePath;
        }

        System.out.println("WARNING: Unable to find path to Android SDK");
        return null;
    }

    private String getAndroidResourcePathFromLocalProperties() {
        // Hand tested
        // This is the path most often taken by IntelliJ
        File rootDir = resourcePath.get(0).getParentFile();
        String localPropertiesFileName = "local.properties";
        File localPropertiesFile = new File(rootDir, localPropertiesFileName);
        if (!localPropertiesFile.exists()) {
            localPropertiesFile = new File(localPropertiesFileName);
        }
        if (localPropertiesFile.exists()) {
            Properties localProperties = new Properties();
            try {
                localProperties.load(new FileInputStream(localPropertiesFile));
                PropertiesHelper.doSubstitutions(localProperties);
                String sdkPath = localProperties.getProperty("sdk.dir");
                if (sdkPath != null) {
                    return getResourcePathFromSdkPath(sdkPath);
                }
            } catch (IOException e) {
                // fine, we'll try something else
            }
        }
        return null;
    }

    private String getAndroidResourcePathFromSystemEnvironment() {
        // Hand tested
        String resourcePath = System.getenv().get("ANDROID_HOME");
        if (resourcePath != null) {
            return new File(resourcePath, getAndroidResourceSubPath()).toString();
        }
        return null;
    }

    private String getAndroidResourcePathFromSystemProperty() {
        // this is used by the android-maven-plugin
        String resourcePath = System.getProperty("android.sdk.path");
        if (resourcePath != null) {
            return new File(resourcePath, getAndroidResourceSubPath()).toString();
        }
        return null;
    }

    private String getAndroidResourcePathByExecingWhichAndroid() {
        // Hand tested
        // Should always work from the command line. Often fails in IDEs because
        // they don't pass the full PATH in the environment
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", "android"});
            String sdkPath = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            if (sdkPath != null && sdkPath.endsWith("tools/android")) {
                return getResourcePathFromSdkPath(sdkPath.substring(0, sdkPath.indexOf("tools/android")));
            }
        } catch (IOException e) {
            // fine we'll try something else
        }
        return null;
    }

    private String getResourcePathFromSdkPath(String sdkPath) {
        File androidResourcePath = new File(sdkPath, getAndroidResourceSubPath());
        return androidResourcePath.exists() ? androidResourcePath.toString() : null;
    }

    private String getAndroidResourceSubPath() {
        return "platforms/android-" + sdkVersion + "/data/res";
    }

    static boolean isLayoutDirectory(String path) {
        return path.contains(File.separator + "layout");
    }

    static boolean isDrawableDirectory(String path) {
        return path.contains(File.separator + "drawable");
    }

    static boolean isMenuDirectory(String path) {
        return path.contains(File.separator + "menu");
    }

    /*
     * For tests only...
     */
    protected ResourceLoader(StringResourceLoader stringResourceLoader) {
        resourceExtractor = new ResourceExtractor();
        resourcePath = Collections.emptyList();
        this.stringResourceLoader = stringResourceLoader;
        this.integerResourceLoader = null;
        pluralResourceLoader = null;
        viewLoader = null;
        stringArrayResourceLoader = null;
        attrResourceLoader = null;
        colorResourceLoader = null;
        drawableResourceLoader = null;
    }

    public static ResourceLoader getFrom(Context context) {
        ResourceLoader resourceLoader = shadowOf(
                context.getApplicationContext()).getResourceLoader();
        resourceLoader.init();
        return resourceLoader;
    }

    public String getNameForId(int viewId) {
        init();
        return resourceExtractor.getResourceName(viewId);
    }

    public View inflateView(Context context, int resource, ViewGroup viewGroup) {
        init();

        View viewNode = viewLoader.inflateView(context, resource, viewGroup);
        if (viewNode != null) return viewNode;

        throw new RuntimeException("Could not find layout " + resourceExtractor.getResourceName(resource));
    }

    public int getColorValue(int id) {
        init();

        int value = colorResourceLoader.getValue(id);
        if (value != -1) return value;

        return -1;
    }

    public String getStringValue(int id) {
        init();

        String value = stringResourceLoader.getValue(id);
        if (value != null) return value;

        return null;
    }

    public int getIntegerValue(int id) {
        init();

        Integer value = integerResourceLoader.getValue(id);
        if (value != null) return value;

        return 0;
    }

    public String getPluralStringValue(int id, int quantity) {
        init();
        return pluralResourceLoader.getValue(id, quantity);
    }
    
    public boolean isDrawableXml(int resourceId) {
        init();
        return drawableResourceLoader.isXml(resourceId);
    }

    public int[] getDrawableIds(int resourceId) {
        init();
        return drawableResourceLoader.getDrawableIds(resourceId);
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
    	if( rClass == null ) { return null; }

    	// Load the Inner Class for interrogation
    	Class animClass = null;
    	try {
			animClass  = Class.forName( rClass.getCanonicalName() + suffix );
		} catch (ClassNotFoundException e) {
			return null;
		}
		
		// Try to find the passed in resource ID
		try {
			for( Field field : animClass.getDeclaredFields() ) {
				if( field.getInt( animClass ) == drawableResourceId )  { return (Drawable) returnClass.newInstance(); }
			}			
		} catch ( Exception e ) { }  
		
		return null;
    }

    public InputStream getRawValue(int id) {
        init();

        for (RawResourceLoader rawResourceLoader : rawResourceLoaders) {
            InputStream stream = rawResourceLoader.getValue(id);
            if (stream != null) return stream;
        }

        return null;
    }

    public String[] getStringArrayValue(int id) {
        init();

        String[] arrayValue = stringArrayResourceLoader.getArrayValue(id);
        if (arrayValue != null) return arrayValue;

        return null;
    }

    public void inflateMenu(Context context, int resource, Menu root) {
        init();

        if (menuLoader.inflateMenu(context, resource, root)) return;

        throw new RuntimeException("Could not find menu " + resourceExtractor.getResourceName(resource));
    }

    public PreferenceScreen inflatePreferences(Context context, int resourceId) {
        init();
        return preferenceLoader.inflatePreferences(context, resourceId);
    }

    public File getAssetsBase() {
        return assetsDir;
    }

    @SuppressWarnings("rawtypes")
	public Class getLocalRClass() { return rClass; }
    
    public void setLocalRClass( Class clazz )  { rClass = clazz; }
    
    public ResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }

    public ViewLoader.ViewNode getLayoutViewNode(String layoutName) {
        return viewLoader.viewNodesByLayoutName.get(layoutName);
    }
}
