package org.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

abstract class XResourceLoader implements ResourceLoader {
    final AttrResourceLoader attrResourceLoader = new AttrResourceLoader();
    final ResBundle<String> booleanData = new ResBundle<String>();
    final ResBundle<String> colorData = new ResBundle<String>();
    final ResBundle<String> dimenData = new ResBundle<String>();
    final ResBundle<String> integerData = new ResBundle<String>();
    final ResBundle<PluralResourceLoader.PluralRules> pluralsData = new ResBundle<PluralResourceLoader.PluralRules>();
    final ResBundle<String> stringData = new ResBundle<String>();
    final ResBundle<ViewNode> layoutData = new ResBundle<ViewNode>();
    final ResBundle<MenuNode> menuData = new ResBundle<MenuNode>();
    final ResBundle<DrawableNode> drawableData = new ResBundle<DrawableNode>();
    final ResBundle<PreferenceNode> preferenceData = new ResBundle<PreferenceNode>();
    final ResBundle<Document> xmlDocuments = new ResBundle<Document>();
    final ResBundle<FsFile> rawResources = new ResBundle<FsFile>();
    private final ResourceIndex resourceIndex;
    boolean isInitialized = false;

    protected XResourceLoader(ResourceIndex resourceIndex) {
        this.resourceIndex = resourceIndex;
    }

    abstract void doInitialize();

    void initialize() {
        if (isInitialized) return;
        doInitialize();
        isInitialized = true;

        makeImmutable();
    }

    protected void makeImmutable() {
        booleanData.makeImmutable();
        colorData.makeImmutable();
        dimenData.makeImmutable();
        integerData.makeImmutable();
        pluralsData.makeImmutable();
        stringData.makeImmutable();
        layoutData.makeImmutable();
        menuData.makeImmutable();
        drawableData.makeImmutable();
        preferenceData.makeImmutable();
        xmlDocuments.makeImmutable();
        rawResources.makeImmutable();
    }

    @Override
    public String getNameForId(int id) {
        return resourceIndex.getResourceName(id);
    }

    @Override
    public String getColorValue(ResName resName, String qualifiers) {
        initialize();
        return new BasicResolver(colorData).resolve(resName, qualifiers);
    }

    @Override
    public String getStringValue(ResName resName, String qualifiers) {
        initialize();
        return new BasicResolver(stringData).resolve(resName, qualifiers);
    }

    @Override
    public String getPluralStringValue(ResName resName, int quantity, String qualifiers) {
        initialize();
        PluralResourceLoader.PluralRules pluralRules = pluralsData.get(resName, qualifiers);
        if (pluralRules == null) return null;

        PluralResourceLoader.Plural plural = pluralRules.find(quantity);
        if (plural == null) return null;
        return new BasicResolver(stringData).resolveValue(qualifiers, plural.string, resName.namespace);
    }

    @Override
    public String getDimenValue(ResName resName, String qualifiers) {
        initialize();
        return new BasicResolver(dimenData).resolve(resName, qualifiers);
    }

    @Override
    public int getIntegerValue(ResName resName, String qualifiers) {
        initialize();
        return new IntegerResolver(integerData).resolve(resName, qualifiers);
    }

    @Override
    public boolean getBooleanValue(ResName resName, String qualifiers) {
        initialize();
        return new BooleanResolver(booleanData).resolve(resName, qualifiers);
    }

    @Override
    public Document getXml(ResName resName, String qualifiers) {
        initialize();
        return xmlDocuments.get(resName, qualifiers);
    }

    @Override
    public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
        return drawableData.get(resName, qualifiers);
    }

    @Override
    public InputStream getRawValue(ResName resName) {
        initialize();

        FsFile file = rawResources.get(resName, "");
        try {
            return file == null ? null : file.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String[] getStringArrayValue(ResName resName, String qualifiers) {
        initialize();

        if (resName == null) return null;
        resName = new ResName(resName.namespace, "string-array", resName.name); // ugh
        List<String> strings = new BasicResolver(stringData).resolveArray(resName, qualifiers);
        return strings == null ? null : strings.toArray(new String[strings.size()]);
    }

    @Override
    public int[] getIntegerArrayValue(ResName resName, String qualifiers) {
        initialize();

        if (resName == null) return null;
        resName = new ResName(resName.namespace, "integer-array", resName.name); // ugh
        List<Integer> ints = new IntegerResolver(integerData).resolveArray(resName, qualifiers);
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
        initialize();

        return preferenceData.get(resName, qualifiers);
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        initialize();
        if (resName == null) return null;
        return layoutData.get(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(ResName resName, String qualifiers) {
        initialize();
        if (resName == null) return null;
        return menuData.get(resName, qualifiers);
    }

    @Override
    public ResourceIndex getResourceIndex() {
        return resourceIndex;
    }

    @Override
    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute) {
        initialize();
        return attrResourceLoader.hasAttributeFor(viewClass, namespace, attribute);
    }

    @Override
    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part) {
        initialize();
        return attrResourceLoader.convertValueToEnum(viewClass, namespace, attribute, part);
    }

    abstract static class Resolver<T> {
        private final ResBundle<String> resBundle;

        protected Resolver(ResBundle<String> resBundle) {
            this.resBundle = resBundle;
        }

        public T resolve(ResName resName, String qualifiers) {
            ResBundle.Value<String> value = resBundle.getValue(resName, qualifiers);
            if (value == null) return null;
            return resolveValue(qualifiers, value.value, value.xmlContext.packageName);
        }

        public List<T> resolveArray(ResName resName, String qualifiers) {
            ResBundle.Value<List<String>> value = resBundle.getListValue(resName, qualifiers);
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
                ResName resName = new ResName(ResName.qualifyResourceName(value.substring(1), packageName, null));
                return resolve(resName, qualifiers);
            } else {
                return convert(value);
            }
        }

        abstract T convert(String rawValue);
    }

    private static class BooleanResolver extends Resolver<Boolean> {
        private BooleanResolver(ResBundle<String> resBundle) {
            super(resBundle);
        }

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

    private static class IntegerResolver extends Resolver<Integer> {
        private IntegerResolver(ResBundle<String> resBundle) {
            super(resBundle);
        }

        @Override
        Integer convert(String rawValue) {
            try {
                // Decode into long, because there are some large hex values in the android resource files
                // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
                // Integer.decode() does not support large, i.e. negative values in hex numbers.
                // try parsing decimal number
                return (int) Long.parseLong(rawValue);
            } catch (NumberFormatException nfe) {
                // try parsing hex number
                try {
                    return Long.decode(rawValue).intValue();
                } catch (NumberFormatException e) {
                    throw new RuntimeException(rawValue + " is not an integer.", nfe);
                }
            }
        }
    }

    static class BasicResolver extends Resolver<String> {
        BasicResolver(ResBundle<String> resBundle) {
            super(resBundle);
        }

        @Override
        String convert(String rawValue) {
            return rawValue;
        }
    }
}
