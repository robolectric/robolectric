package org.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoutingResourceLoader implements ResourceLoader {
    private final Map<String, ResourceLoader> resourceLoaders;
    private final ResourceIndex resourceIndex;

    public RoutingResourceLoader(Map<String, ResourceLoader> resourceLoaders) {
        this.resourceLoaders = resourceLoaders;

        Set<ResourceIndex> resourceIndexes = new HashSet<ResourceIndex>();
        for (ResourceLoader resourceLoader : resourceLoaders.values()) {
            resourceIndexes.add(resourceLoader.getResourceExtractor());
        }
        resourceIndex = new ResourceExtractor(resourceIndexes.toArray(new ResourceIndex[resourceIndexes.size()]));
    }

    @Override
    public String getNameForId(int id) {
        return pickFor(id).getNameForId(id);
    }

    @Override
    public int getColorValue(ResName resName, String qualifiers) {
        return pickFor(resName).getColorValue(resName, qualifiers);
    }

    @Override
    public String getStringValue(ResName resName, String qualifiers) {
        return pickFor(resName).getStringValue(resName, qualifiers);
    }

    @Override
    public String getPluralStringValue(ResName resName, int quantity, String qualifiers) {
        return pickFor(resName).getPluralStringValue(resName, quantity, qualifiers);
    }

    @Override
    public float getDimenValue(ResName resName, String qualifiers) {
        return pickFor(resName).getDimenValue(resName, qualifiers);
    }

    @Override
    public int getIntegerValue(ResName resName, String qualifiers) {
        return pickFor(resName).getIntegerValue(resName, qualifiers);
    }

    @Override
    public boolean getBooleanValue(ResName resName, String qualifiers) {
        return pickFor(resName).getBooleanValue(resName, qualifiers);
    }

    @Override
    public Document getXml(ResName resName, String qualifiers) {
        return pickFor(resName).getXml(resName, "");
    }

    @Override
    public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
        return pickFor(resName).getDrawableNode(resName, qualifiers);
    }

    @Override
    public InputStream getRawValue(int id) {
        return pickFor(id).getRawValue(id);
    }

    @Override
    public String[] getStringArrayValue(ResName resName, String qualifiers) {
        return pickFor(resName).getStringArrayValue(resName, qualifiers);
    }

    @Override
    public int[] getIntegerArrayValue(ResName resName, String qualifiers) {
        return pickFor(resName).getIntegerArrayValue(resName, qualifiers);
    }

    @Override
    public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
        return pickFor(resName).getPreferenceNode(resName, qualifiers);
    }

    @Override
    public ResourceIndex getResourceExtractor() {
        return resourceIndex;
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        return pickFor(resName).getLayoutViewNode(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(ResName resName, String qualifiers) {
        return pickFor(resName).getMenuNode(resName, qualifiers);
    }

    @Override
    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute) {
        return pickFor(namespace).hasAttributeFor(viewClass, namespace, attribute);
    }

    @Override
    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part) {
        return pickFor(namespace).convertValueToEnum(viewClass, namespace, attribute, part);
    }

    private ResourceLoader pickFor(int id) {
        ResName resName = resourceIndex.getResName(id);
        return pickFor(resName);
    }

    private ResourceLoader pickFor(ResName resName) {
        if (resName == null) return new PackageResourceLoader();
        return pickFor(resName.namespace);
    }

    private ResourceLoader pickFor(String namespace) {
        ResourceLoader resourceLoader = resourceLoaders.get(namespace);
        if (resourceLoader == null) {
            throw new RuntimeException("no ResourceLoader found for " + namespace);
        }
        return resourceLoader;
    }
}
