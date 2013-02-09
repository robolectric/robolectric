package org.robolectric.res;

import android.view.View;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

abstract class XResourceLoader implements ResourceLoader {
    final ResourceIndex resourceIndex;

    final List<RawResourceLoader> rawResourceLoaders = new ArrayList<RawResourceLoader>();

    boolean isInitialized = false;

    final Resolver<Boolean> booleanResolver = new BooleanResolver();
    final Resolver<Integer> colorResolver = new ColorResolver();
    final Resolver<Float> dimenResolver = new DimenResolver();
    final Resolver<Integer> integerResolver = new IntegerResolver();
    final PluralsResolver pluralsResolver = new PluralsResolver();
    final Resolver<String> stringResolver = new StringResolver();
    final ResBundle<ViewNode> viewNodes = new ResBundle<ViewNode>();
    final ResBundle<MenuNode> menuNodes = new ResBundle<MenuNode>();
    final ResBundle<DrawableNode> drawableNodes = new ResBundle<DrawableNode>();

    public XResourceLoader(ResourcePath... resourcePaths) {
        this(asList(resourcePaths));
    }

    public XResourceLoader(List<ResourcePath> resourcePaths) {
        this(resourcePaths, null);
    }

    public XResourceLoader(List<ResourcePath> resourcePaths, String overrideNamespace) {
        this.resourceIndex = new ResourceExtractor(resourcePaths);
        this.resourcePaths = Collections.unmodifiableList(resourcePaths);


        if (overrideNamespace != null) {
            for (ResBundle resBundle : getResBundles()) {
                resBundle.overrideNamespace(overrideNamespace);
            }
        }
    }

    List<ResBundle<?>> getResBundles() {
        //noinspection unchecked
        return Arrays.asList(booleanResolver, colorResolver, dimenResolver, integerResolver,
                pluralsResolver, stringResolver, viewNodes, menuNodes, drawableNodes);
    }

    private File getPreferenceResourceDir(File xmlResourceDir) {
        return xmlResourceDir != null ? new File(xmlResourceDir, "xml") : null;
    }

    @Override
    public String getNameForId(int id) {
        init();
        return resourceIndex.getResourceName(id);
    }

    abstract void init();

    @Override
    public String getNameForId(int id) {
        init();
        return resourceIndex.getResourceName(id);
    }

    @Override
    public int getColorValue(ResName resName, String qualifiers) {
        init();
        Integer value = colorResolver.resolve(resName, qualifiers);
        return value == null ? -1 : value;
    }

    @Override
    public String getStringValue(ResName resName, String qualifiers) {
        init();
        return stringResolver.resolve(resName, qualifiers);
    }

    @Override
    public String getPluralStringValue(ResName resName, int quantity, String qualifiers) {
        init();
        PluralResourceLoader.PluralRules pluralRules = pluralsResolver.get(resName, qualifiers);
        if (pluralRules == null) return null;

        PluralResourceLoader.Plural plural = pluralRules.find(quantity);
        if (plural == null) return null;
        return stringResolver.resolveValue(qualifiers, plural.string, resName.namespace);
    }

    @Override
    public float getDimenValue(ResName resName, String qualifiers) {
        init();
        return dimenResolver.resolve(resName, qualifiers);
    }

    @Override
    public int getIntegerValue(ResName resName, String qualifiers) {
        init();
        return integerResolver.resolve(resName, qualifiers);
    }

    @Override
    public boolean getBooleanValue(ResName resName, String qualifiers) {
        init();
        return booleanResolver.resolve(resName, qualifiers);
    }

    @Override
    public Document getXml(ResName resName, String qualifiers) {
        init();
        return xmlDocuments.get(resName, qualifiers);
    }

    @Override
    public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
        return drawableNodes.get(resName, qualifiers);
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
    public String[] getStringArrayValue(ResName resName, String qualifiers) {
        init();

        if (resName == null) return null;
        resName = new ResName(resName.namespace, "string-array", resName.name); // ugh
        List<String> strings = stringResolver.resolveArray(resName, qualifiers);
        return strings == null ? null : strings.toArray(new String[strings.size()]);
    }

    @Override
    public int[] getIntegerArrayValue(ResName resName, String qualifiers) {
        init();

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
    public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
        return preferenceNodes.get(resName, qualifiers);
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        init();
        if (resName == null) return null;
        return viewNodes.get(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(ResName resName, String qualifiers) {
        init();
        if (resName == null) return null;
        return menuNodes.get(resName, qualifiers);
    }

    @Override
    public ResourceIndex getResourceExtractor() {
        return resourceIndex;
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
                ResName resName = new ResName(ResName.qualifyResourceName(value.substring(1), packageName));
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
