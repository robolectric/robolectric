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

import static java.util.Arrays.asList;

public class PackageResourceLoader implements ResourceLoader {

    private List<ResourcePath> resourcePaths;
    private final ResourceExtractor resourceExtractor;

    private final PreferenceLoader preferenceLoader;
    private final XmlFileLoader xmlFileLoader;
    private final AttrResourceLoader attrResourceLoader;
    private final DrawableResourceLoader drawableResourceLoader;
    private final List<RawResourceLoader> rawResourceLoaders = new ArrayList<RawResourceLoader>();

    private boolean isInitialized = false;

    private final Resolver<Boolean> booleanResolver = new BooleanResolver();
    private final Resolver<Integer> colorResolver = new ColorResolver();
    private final Resolver<Float> dimenResolver = new DimenResolver();
    private final Resolver<Integer> integerResolver = new IntegerResolver();
    private final PluralsResolver pluralsResolver = new PluralsResolver();
    private final Resolver<String> stringResolver = new StringResolver();
    private final ResBundle<ViewNode> viewNodes = new ResBundle<ViewNode>();
    private final ResBundle<MenuNode> menuNodes = new ResBundle<MenuNode>();

    private final Set<Integer> ninePatchDrawableIds = new HashSet<Integer>();

    public PackageResourceLoader(ResourcePath... resourcePaths) {
        this(asList(resourcePaths));
    }

    public PackageResourceLoader(List<ResourcePath> resourcePaths) {
        this.resourceExtractor = new ResourceExtractor(resourcePaths);
        this.resourcePaths = Collections.unmodifiableList(resourcePaths);

        attrResourceLoader = new AttrResourceLoader();
        drawableResourceLoader = new DrawableResourceLoader(resourceExtractor);
        preferenceLoader = new PreferenceLoader(resourceExtractor);
        xmlFileLoader = new XmlFileLoader(resourceExtractor);
    }

    private void init() {
        if (isInitialized) return;

        try {
            loadEverything();
        } catch (I18nException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loadEverything() throws Exception {
        for (ResourcePath resourcePath : resourcePaths) {
            System.out.println("DEBUG: Loading resources for " + resourcePath.getPackageName() + " from " + resourcePath.resourceBase + "...");

            loadResourceXmlSubDirs(new DocumentLoader(
                    new ValueResourceLoader(booleanResolver, "bool", false),
                    new ValueResourceLoader(colorResolver, "color", false),
                    new ValueResourceLoader(dimenResolver, "dimen", false),
                    new ValueResourceLoader(integerResolver, "integer", true),
                    new PluralResourceLoader(resourceExtractor, pluralsResolver),
                    new ValueResourceLoader(stringResolver, "string", true),
                    attrResourceLoader
            ), resourcePath, "values");

            loadResourceXmlSubDirs(new DocumentLoader(new ViewLoader(viewNodes)), resourcePath, "layout");
            loadResourceXmlSubDirs(new DocumentLoader(new MenuLoader(menuNodes)), resourcePath, "menu");
            loadResourceXmlSubDirs(new DocumentLoader(drawableResourceLoader), resourcePath, "drawable");
            loadResourceXmlSubDirs(new DocumentLoader(preferenceLoader), resourcePath, "xml");
            loadResourceXmlSubDirs(new DocumentLoader(xmlFileLoader), resourcePath, "xml");

            loadOtherResources(resourcePath);

            listNinePatchResources(ninePatchDrawableIds, resourcePath);

            rawResourceLoaders.add(new RawResourceLoader(resourceExtractor, resourcePath.resourceBase));
        }

        isInitialized = true;
    }

    protected void loadOtherResources(ResourcePath resourcePath) {
    }

    private void loadResourceXmlSubDirs(DocumentLoader documentLoader, ResourcePath resourcePath, final String folderBaseName) throws Exception {
        documentLoader.loadResourceXmlDirs(resourcePath, resourcePath.resourceBase.listFiles(new DirectoryMatchingFileFilter(folderBaseName)));
    }

    private File getPreferenceResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "xml") : null;
    }

    @Override
    public String getNameForId(int viewId) {
        init();
        return resourceExtractor.getResourceName(viewId);
    }

    @Override
    public int getColorValue(int id, String qualifiers) {
        init();
        Integer value = colorResolver.resolve(resourceExtractor.getResName(id), qualifiers);
        return value == null ? -1 : value;
    }

    @Override
    public String getStringValue(int id, String qualifiers) {
        init();
        return stringResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    @Override
    public String getPluralStringValue(int id, int quantity, String qualifiers) {
        init();
        ResName resName = resourceExtractor.getResName(id);
        PluralResourceLoader.PluralRules pluralRules = pluralsResolver.get(resName, qualifiers);
        if (pluralRules == null) return null;

        PluralResourceLoader.Plural plural = pluralRules.find(quantity);
        if (plural == null) return null;
        return stringResolver.resolveValue(qualifiers, plural.string, resName.namespace);
    }

    @Override
    public float getDimenValue(int id, String qualifiers) {
        init();
        return dimenResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    @Override
    public int getIntegerValue(int id, String qualifiers) {
        init();
        return integerResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    @Override
    public boolean getBooleanValue(int id, String qualifiers) {
        init();
        return booleanResolver.resolve(resourceExtractor.getResName(id), qualifiers);
    }

    @Override
    public XmlResourceParser getXml(int id) {
        init();
        return xmlFileLoader.getXml(id);
    }

    @Override
    public boolean isDrawableXml(int resourceId) {
        init();
        return drawableResourceLoader.isXml(resourceId);
    }

    @Override
    public boolean isAnimatableXml(int resourceId) {
        init();
        return drawableResourceLoader.isAnimationDrawable(resourceId);
    }

    @Override
    public int[] getDrawableIds(int resourceId) {
        init();
        return drawableResourceLoader.getDrawableIds(resourceId);
    }

    @Override
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

    @Override
    public Drawable getXmlDrawable(int resourceId) {
        return drawableResourceLoader.getXmlDrawable(resourceId);
    }

    @Override
    public Drawable getAnimDrawable(int resourceId) {
        return getInnerRClassDrawable(resourceId, "$anim", AnimationDrawable.class);
    }

    @Override
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

    @Override
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

    @Override
    public InputStream getRawValue(int id) {
        init();

        for (RawResourceLoader rawResourceLoader : rawResourceLoaders) {
            InputStream stream = rawResourceLoader.getValue(id);
            if (stream != null) return stream;
        }

        return null;
    }

    @Override
    public String[] getStringArrayValue(int id, String qualifiers) {
        init();

        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return null;
        resName = new ResName(resName.namespace, "string-array", resName.name); // ugh
        List<String> strings = stringResolver.resolveArray(resName, qualifiers);
        return strings == null ? null : strings.toArray(new String[strings.size()]);
    }

    @Override
    public int[] getIntegerArrayValue(int id, String qualifiers) {
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

    @Override
    public PreferenceScreen inflatePreferences(Context context, int resourceId) {
        init();
        return preferenceLoader.inflatePreferences(context, resourceId);
    }

    @Override
    public ViewNode getLayoutViewNode(int id, String qualifiers) {
        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return null;
        return getLayoutViewNode(resName, qualifiers);
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        init();
        return viewNodes.get(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(int id, String qualifiers) {
        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return null;
        return getMenuNode(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(ResName resName, String qualifiers) {
        init();
        return menuNodes.get(resName, qualifiers);
    }

    @Override
    public ResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }

    @Override
    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute) {
        init();
        return attrResourceLoader.hasAttributeFor(viewClass, namespace, attribute);
    }

    @Override
    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part) {
        init();
        return attrResourceLoader.convertValueToEnum(viewClass, namespace, attribute, part);
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
