package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.I18nException;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;

public class ResourceLoader {

    private List<ResourcePath> resourcePaths;
    private final ResourceExtractor resourceExtractor;

    private final ViewLoader viewLoader;
    private final MenuLoader menuLoader;
    private final PreferenceLoader preferenceLoader;
    private final XmlFileLoader xmlFileLoader;
    protected final AttrResourceLoader attrResourceLoader;
    private final DrawableResourceLoader drawableResourceLoader;
    private final List<RawResourceLoader> rawResourceLoaders = new ArrayList<RawResourceLoader>();

    private final RoboLayoutInflater roboLayoutInflater;

    private boolean isInitialized = false;
    private boolean strictI18n = false;

    private final Resolver<Boolean> booleanResolver = new BooleanResolver();
    private final Resolver<Integer> colorResolver = new ColorResolver();
    private final Resolver<Float> dimenResolver = new DimenResolver();
    private final Resolver<Integer> integerResolver = new IntegerResolver();
    private final PluralsResolver pluralsResolver = new PluralsResolver();
    private final Resolver<String> stringResolver = new StringResolver();
    private final ResBundle<ViewNode> viewNodes = new ResBundle<ViewNode>();

    private final Set<Integer> ninePatchDrawableIds = new HashSet<Integer>();
    private String qualifiers = "";

    public ResourceLoader(ResourcePath... resourcePaths) {
        this(asList(resourcePaths));
    }

    public ResourceLoader(List<ResourcePath> resourcePaths) {
        this.resourceExtractor = new ResourceExtractor(resourcePaths);
        this.resourcePaths = Collections.unmodifiableList(resourcePaths);

        attrResourceLoader = new AttrResourceLoader();
        drawableResourceLoader = new DrawableResourceLoader(resourceExtractor);
        viewLoader = new ViewLoader(viewNodes);
        menuLoader = new MenuLoader(resourceExtractor, attrResourceLoader);
        preferenceLoader = new PreferenceLoader(resourceExtractor);
        xmlFileLoader = new XmlFileLoader(resourceExtractor);

        roboLayoutInflater = new RoboLayoutInflater(resourceExtractor, viewNodes);
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
                    new ValueResourceLoader(booleanResolver, "bool", false),
                    new ValueResourceLoader(colorResolver, "color", false),
                    new ValueResourceLoader(dimenResolver, "dimen", false),
                    new ValueResourceLoader(integerResolver, "integer", true),
                    new PluralResourceLoader(resourceExtractor, pluralsResolver),
                    new ValueResourceLoader(stringResolver, "string", true),
                    attrResourceLoader
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
            roboLayoutInflater.setQualifiers(qualifiers);
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
            System.out.println("WARN: Couldn't find value resource directory: " + result.getAbsolutePath());
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
        Integer value = colorResolver.resolve(resourceExtractor.getResName(id), qualifiers);
        return value == null ? -1 : value;
    }

    public String getStringValue(int id) {
        init();
        return stringResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    public String getPluralStringValue(int id, int quantity) {
        init();
        ResName resName = resourceExtractor.getResName(id);
        PluralResourceLoader.PluralRules pluralRules = pluralsResolver.get(resName, qualifiers);
        if (pluralRules == null) return null;

        PluralResourceLoader.Plural plural = pluralRules.find(quantity);
        if (plural == null) return null;
        return stringResolver.resolveValue(qualifiers, plural.string, resName.namespace);
    }

    public float getDimenValue(int id) {
        init();
        return dimenResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    public int getIntegerValue(int id) {
        init();
        return integerResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    public boolean getBooleanValue(int id) {
        init();
        return booleanResolver.resolve(resourceExtractor.getResName(id), qualifiers);
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

        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return null;
        resName = new ResName(resName.namespace, "string-array", resName.name); // ugh
        List<String> strings = stringResolver.resolveArray(resName, qualifiers);
        return strings == null ? null : strings.toArray(new String[strings.size()]);
    }

    public int[] getIntegerArrayValue(int id) {
        init();

        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return null;
        resName = new ResName(resName.namespace, "integer-array", resName.name); // ugh
        List<Integer> ints = integerResolver.resolveArray(resName, qualifiers);
        return ints == null ? null : toIntArray(ints);
    }

    private int[] toIntArray(List<Integer> ints) {
        int num = ints.size();
        int[] array = new int[num];
        for (int i = 0; i < num; i++) {
            array[i] = ints.get(i);
        }
        return array;
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
        return viewNodes.get(new ResName(layoutName), qualifiers);
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

    abstract static class Resolver<T> extends ResBundle<String> {
        public T resolve(ResName resName, String qualifiers) {
            Value<String> value = getValue(resName, qualifiers);
            if (value == null) return null;
            return resolveValue(qualifiers, value.value, value.xmlContext.packageName);
        }

        public List<T> resolveArray(ResName resName, String qualifiers) {
            Value<List<String>> value = getListValue(resName, qualifiers);
            if (value == null) return null;

            List<T> items = new ArrayList<T>();
            for (String v : value.value) {
                items.add(resolveValue(qualifiers, v, value.xmlContext.packageName));
            }
            return items;
        }

        T resolveValue(String qualifiers, String value, String packageName) {
            if (value == null) return null;
            if (value.startsWith("@")) {
                ResName resName = new ResName(ResourceExtractor.qualifyResourceName(value.substring(1), packageName));
                return resolve(resName, qualifiers);
            } else {
                return convert(value);
            }
        }

        abstract T convert(String rawValue);
    }

    private static class BooleanResolver extends Resolver<Boolean> {
        @Override
        Boolean convert(String rawValue) {
            if ("true".equalsIgnoreCase(rawValue)) {
                return true;
            } else if ("false".equalsIgnoreCase(rawValue)) {
                return false;
            }

            int intValue = Integer.parseInt(rawValue);
            if (intValue == 0) {
                return false;
            }
            return true;

        }
    }

    private static class ColorResolver extends Resolver<Integer> {
        @Override
        Integer convert(String rawValue) {
            if (rawValue.startsWith("#")) {
                long color = Long.parseLong(rawValue.substring(1), 16);
                return (int) color;
            }
            return null;
        }
    }

    private static class DimenResolver extends Resolver<Float> {
        private static final String[] UNITS = { "dp", "dip", "pt", "px", "sp" };

        @Override
        Float convert(String rawValue) {
            int end = rawValue.length();
            for ( int i = 0; i < UNITS.length; i++ ) {
                int index = rawValue.indexOf(UNITS[i]);
                if ( index >= 0 && end > index ) {
                    end = index;
                }
            }

            return Float.parseFloat(rawValue.substring(0, end));
        }
    }

    private static class IntegerResolver extends Resolver<Integer> {
        @Override
        Integer convert(String rawValue) {
            try {
                // Decode into long, because there are some large hex values in the android resource files
                // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
                // Integer.decode() does not support large, i.e. negative values in hex numbers.
                return (int) Long.decode(rawValue).longValue();
            } catch (NumberFormatException nfe) {
                throw new RuntimeException(rawValue + " is not an integer.", nfe);
            }
        }
    }

    private static class PluralsResolver extends ResBundle<PluralResourceLoader.PluralRules> {
    }

    static class StringResolver extends Resolver<String> {
        @Override
        String convert(String rawValue) {
            return rawValue;
        }
    }

    private static class StringArrayResolver extends Resolver<String[]> {
        @Override
        String[] convert(String rawValue) {
            return new String[0];
        }
    }
}
