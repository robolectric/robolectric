package com.xtremelabs.robolectric.res;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.*;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;
import com.xtremelabs.robolectric.util.PropertiesHelper;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;

public class ResourceLoader {

    private List<ResourcePath> resourcePaths;
    private final ResourceExtractor resourceExtractor;

    private final ViewLoader viewLoader;
    private final MenuLoader menuLoader;
    private final PreferenceLoader preferenceLoader;
    private final XmlFileLoader xmlFileLoader;
    private final StringResourceLoader stringResourceLoader;
    private final DimenResourceLoader dimenResourceLoader;
    private final IntegerResourceLoader integerResourceLoader;
    private final PluralResourceLoader pluralResourceLoader;
    private final StringArrayResourceLoader stringArrayResourceLoader;
    protected final AttrResourceLoader attrResourceLoader;
    private final ColorResourceLoader colorResourceLoader;
    private final DrawableResourceLoader drawableResourceLoader;
    private final BoolResourceLoader boolResourceLoader;
    private final List<RawResourceLoader> rawResourceLoaders = new ArrayList<RawResourceLoader>();

    private final RoboLayoutInflater roboLayoutInflater;

    private boolean isInitialized = false;
    private boolean strictI18n = false;
    private final Set<Integer> ninePatchDrawableIds = new HashSet<Integer>();
    private Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private String qualifiers = "";

    public static ResourcePath getSystemResourcePath(int sdkVersion, List<ResourcePath> resourcePaths) {
        String pathToAndroidResources = new AndroidResourcePathFinder(sdkVersion, resourcePaths).getPathToAndroidResources();
        return new ResourcePath(R.class, new File(pathToAndroidResources), null);
    }

    public ResourceLoader(List<ResourcePath> resourcePaths) {
        this(new ResourceExtractor(resourcePaths), resourcePaths);
    }

    public ResourceLoader(ResourcePath... resourcePaths) throws Exception {
        this(asList(resourcePaths));
    }

    private ResourceLoader(ResourceExtractor resourceExtractor, List<ResourcePath> resourcePaths) {
        this.resourceExtractor = resourceExtractor;
        this.resourcePaths = Collections.unmodifiableList(resourcePaths);

        stringResourceLoader = new StringResourceLoader(resourceExtractor);
        dimenResourceLoader = new DimenResourceLoader(resourceExtractor);
        integerResourceLoader = new IntegerResourceLoader(resourceExtractor);
        pluralResourceLoader = new PluralResourceLoader(resourceExtractor, stringResourceLoader);
        stringArrayResourceLoader = new StringArrayResourceLoader(resourceExtractor, stringResourceLoader);
        colorResourceLoader = new ColorResourceLoader(resourceExtractor);
        attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        drawableResourceLoader = new DrawableResourceLoader(resourceExtractor);
        boolResourceLoader = new BoolResourceLoader(resourceExtractor);
        viewLoader = new ViewLoader(resourceExtractor, viewNodesByLayoutName);
        menuLoader = new MenuLoader(resourceExtractor, attrResourceLoader);
        preferenceLoader = new PreferenceLoader(resourceExtractor);
        xmlFileLoader = new XmlFileLoader(resourceExtractor);

        roboLayoutInflater = new RoboLayoutInflater(resourceExtractor, viewNodesByLayoutName);
    }

    public ResourceLoader copy() {
        return new ResourceLoader(resourceExtractor, resourcePaths);
    }

    public void setStrictI18n(boolean strict) {
        this.strictI18n = strict;
        viewLoader.setStrictI18n(strict);
        menuLoader.setStrictI18n(strict);
        preferenceLoader.setStrictI18n(strict);
    }

    public boolean getStrictI18n() {
        return strictI18n;
    }

    private void init() {
        if (isInitialized) return;

        try {
            loadEverything(qualifiers);
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadEverything(String qualifiers) throws Exception {
        for (ResourcePath resourcePath : resourcePaths) {
            System.out.println("DEBUG: Loading resources for " + resourcePath.getPackageName() + " from " + resourcePath.resourceBase + "...");

            validateQualifiers(resourcePath, qualifiers);

            DocumentLoader valuesDocumentLoader = new DocumentLoader(
                    stringResourceLoader, pluralResourceLoader,
                    stringArrayResourceLoader, colorResourceLoader, attrResourceLoader,
                    dimenResourceLoader, integerResourceLoader, boolResourceLoader
            );

            loadValueResourcesFromDirs(valuesDocumentLoader, resourcePath, qualifiers);

            loadResourceXmlSubDirs(new DocumentLoader(viewLoader), resourcePath, "layout");
            loadResourceXmlSubDirs(new DocumentLoader(menuLoader), resourcePath, "menu");
            loadResourceXmlSubDirs(new DocumentLoader(drawableResourceLoader), resourcePath, "drawable");
            loadResourceXmlSubDirs(new DocumentLoader(preferenceLoader), resourcePath, "xml");
            loadResourceXmlSubDirs(new DocumentLoader(xmlFileLoader), resourcePath, "xml");

            loadOtherResources(resourcePath);

            listNinePatchResources(ninePatchDrawableIds, resourcePath);

            rawResourceLoaders.add(new RawResourceLoader(resourceExtractor, resourcePath.resourceBase));
        }

        isInitialized = true;
    }

    /**
     * Reload values resources, include String, Plurals, Dimen, Prefs, Menu
     *
     * @param qualifiers
     */
    public void reloadValuesResources(String qualifiers) {
        try {
            loadEverything(qualifiers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setQualifiers(String qualifiers) {
        if (qualifiers == null) throw new NullPointerException();

        if (!this.qualifiers.equals(qualifiers)) {
            System.out.println("switching from '" + this.qualifiers + "' to '" + qualifiers + "'");
            this.qualifiers = qualifiers;
            this.isInitialized = false;
        }
    }

    protected void loadOtherResources(ResourcePath resourcePath) {
    }

    private void loadResourceXmlSubDirs(DocumentLoader documentLoader, ResourcePath resourcePath, final String folderBaseName) throws Exception {
        documentLoader.loadResourceXmlDirs(resourcePath, resourcePath.resourceBase.listFiles(new DirectoryMatchingFileFilter(folderBaseName)));
    }

    private void loadValueResourcesFromDirs(DocumentLoader documentLoader, ResourcePath resourcePath, String qualifiers) throws Exception {
        File qualifiedValuesDir = new File(resourcePath.resourceBase, "".equals(qualifiers) ? "values" : "values-" + qualifiers);
        if (qualifiedValuesDir.exists()) {
            documentLoader.loadResourceXmlDirs(resourcePath, qualifiedValuesDir);
        }
    }

    private void validateQualifiers(ResourcePath resourcePath, String qualifiers) {
        String valuesDir = "values";
        if (qualifiers != null && !qualifiers.isEmpty()) {
            valuesDir += "-" + qualifiers;
        }
        File result = new File(resourcePath.resourceBase, valuesDir);
        if (!result.exists()) {
            throw new RuntimeException("Couldn't find value resource directory: " + result.getAbsolutePath());
        }
    }

    private File getPreferenceResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "xml") : null;
    }

    public String getQualifiers() {
        return qualifiers;
    }

    public TestAttributeSet createAttributeSet(List<Attribute> attributes, Class<? extends View> viewClass) {
        TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, viewClass);
        if (strictI18n) {
            attributeSet.validateStrictI18n();
        }
        return attributeSet;
    }

    public static class AndroidResourcePathFinder {
        private final int sdkVersion;
        private final ResourcePath resourcePath;

        public AndroidResourcePathFinder(int sdkVersion, List<ResourcePath> resourcePaths) {
            this.resourcePath = resourcePaths == null ? new ResourcePath(null, new File("."), null) : resourcePaths.get(0);
            this.sdkVersion = sdkVersion;
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
            File rootDir = resourcePath.resourceBase.getParentFile();
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
    }

    public static ResourceLoader getFrom(Context context) {
        ResourceLoader resourceLoader = shadowOf(context.getApplicationContext()).getResourceLoader();
        resourceLoader.init();
        return resourceLoader;
    }

    public String getNameForId(int viewId) {
        init();
        return resourceExtractor.getResourceName(viewId);
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

    public String getPluralStringValue(int id, int quantity) {
        init();
        return pluralResourceLoader.getValue(id, quantity);
    }

    public float getDimenValue(int id) {
        init();
        return dimenResourceLoader.getValue(id);
    }

    public int getIntegerValue(int id) {
        init();

        Integer value = integerResourceLoader.getValue(id);
        if (value != null) return value;

        return 0;
    }

    public boolean getBooleanValue(int id) {
        init();
        return boolResourceLoader.getValue(id);
    }

    public XmlResourceParser getXml(int id) {
        init();
        return xmlFileLoader.getXml(id);
    }

    public boolean isDrawableXml(int resourceId) {
        init();
        return drawableResourceLoader.isXml(resourceId);
    }

    public boolean isAnimatableXml(int resourceId) {
        init();
        return drawableResourceLoader.isAnimationDrawable(resourceId);
    }

    public int[] getDrawableIds(int resourceId) {
        init();
        return drawableResourceLoader.getDrawableIds(resourceId);
    }

    public Drawable getDrawable(int resourceId, Resources realResources) {
//        todo: this: String resourceName = resourceExtractor.getResourceName(resourceId);


        Drawable xmlDrawable = getXmlDrawable(resourceId);
        if (xmlDrawable != null) {
            return xmlDrawable;
        }

        Drawable animDrawable = getAnimDrawable(resourceId);
        if (animDrawable != null) {
            return animDrawable;
        }

        Drawable colorDrawable = getColorDrawable(resourceId);
        if (colorDrawable != null) {
            return colorDrawable;
        }

        if (this.isNinePatchDrawable(resourceId)) {
            return new NinePatchDrawable(realResources, null);
        }

        return new BitmapDrawable(BitmapFactory.decodeResource(realResources, resourceId));

    }

    public Drawable getXmlDrawable(int resourceId) {
        return drawableResourceLoader.getXmlDrawable(resourceId);
    }

    public Drawable getAnimDrawable(int resourceId) {
        return getInnerRClassDrawable(resourceId, "$anim", AnimationDrawable.class);
    }

    public Drawable getColorDrawable(int resourceId) {
        return getInnerRClassDrawable(resourceId, "$color", ColorDrawable.class);
    }

    @SuppressWarnings("rawtypes")
    private Drawable getInnerRClassDrawable(int drawableResourceId, String suffix, Class returnClass) {
        for (ResourcePath resourcePath : resourcePaths) {
            // Load the Inner Class for interrogation
            Class animClass;
            try {
                animClass = Class.forName(resourcePath.rClass.getCanonicalName() + suffix);
            } catch (ClassNotFoundException e) {
                return null;
            }

            // Try to find the passed in resource ID
            try {
                for (Field field : animClass.getDeclaredFields()) {
                    if (field.getInt(animClass) == drawableResourceId) {
                        return (Drawable) returnClass.newInstance();
                    }
                }
            } catch (Exception e) {
            }
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
     * @param resourcePath
     */
    private void listNinePatchResources(Set<Integer> resourceIds, ResourcePath resourcePath) {
        listNinePatchResources(resourceIds, resourcePath, resourcePath.resourceBase);
    }

    private void listNinePatchResources(Set<Integer> resourceIds, ResourcePath resourcePath, File dir) {
        DirectoryMatchingFileFilter drawableFilter = new DirectoryMatchingFileFilter("drawable");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && drawableFilter.accept(f)) {
                    listNinePatchResources(resourceIds, resourcePath, f);
                } else {
                    String name = f.getName();
                    if (name.endsWith(".9.png")) {
                        String[] tokens = name.split("\\.9\\.png$");
                        resourceIds.add(resourceExtractor.getResourceId(resourcePath.getPackageName() + ":drawable/" + tokens[0], ""));
                    }
                }
            }
        }
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
        return resourcePaths.get(0).assetsDir; // todo: do something better
    }

    ViewNode getLayoutViewNode(String layoutName) {
        return viewNodesByLayoutName.get(layoutName);
    }

    public void setLayoutQualifierSearchPath(String... locations) {
        init();
        roboLayoutInflater.setLayoutQualifierSearchPath(Arrays.asList(locations));
    }

    public ResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }

    ViewLoader getViewLoader() {
        return viewLoader;
    }

    public RoboLayoutInflater getRoboLayoutInflater() {
        init();
        return roboLayoutInflater;
    }

    private static class DirectoryMatchingFileFilter implements FileFilter {
        private final String folderBaseName;

        public DirectoryMatchingFileFilter(String folderBaseName) {
            this.folderBaseName = folderBaseName;
        }

        @Override
        public boolean accept(File file) {
            return file.getPath().contains(File.separator + folderBaseName);
        }
    }
}
