package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.res.RoboLayoutInflater;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

public class RoutingResourceLoader implements ResourceLoader {
    private final Map<String, ResourceLoader> resourceLoaders;
    private final ResourceExtractor resourceExtractor;

    public RoutingResourceLoader(Map<String, ResourceLoader> resourceLoaders) {
        this.resourceLoaders = resourceLoaders;

        List<ResourceExtractor> resourceExtractors = new ArrayList<ResourceExtractor>();
        for (ResourceLoader resourceLoader : resourceLoaders.values()) {
            resourceExtractors.add(resourceLoader.getResourceExtractor());
        }
        resourceExtractor = new ResourceExtractor(resourceExtractors.toArray(new ResourceExtractor[resourceExtractors.size()]));
    }

    @Override
    public String getNameForId(int viewId) {
        return pickFor(viewId).getNameForId(viewId);
    }

    @Override
    public int getColorValue(int id, String qualifiers) {
        return pickFor(id).getColorValue(id, qualifiers);
    }

    @Override
    public String getStringValue(int id, String qualifiers) {
        return pickFor(id).getStringValue(id, qualifiers);
    }

    @Override
    public String getPluralStringValue(int id, int quantity, String qualifiers) {
        return pickFor(id).getPluralStringValue(id, quantity, qualifiers);
    }

    @Override
    public float getDimenValue(int id, String qualifiers) {
        return pickFor(id).getDimenValue(id, qualifiers);
    }

    @Override
    public int getIntegerValue(int id, String qualifiers) {
        return pickFor(id).getIntegerValue(id, qualifiers);
    }

    @Override
    public boolean getBooleanValue(int id, String qualifiers) {
        return pickFor(id).getBooleanValue(id, qualifiers);
    }

    @Override
    public XmlResourceParser getXml(int id) {
        return pickFor(id).getXml(id);
    }

    @Override
    public boolean isDrawableXml(int resourceId) {
        return pickFor(resourceId).isDrawableXml(resourceId);
    }

    @Override
    public boolean isAnimatableXml(int resourceId) {
        return pickFor(resourceId).isAnimatableXml(resourceId);
    }

    @Override
    public int[] getDrawableIds(int resourceId) {
        return pickFor(resourceId).getDrawableIds(resourceId);
    }

    @Override
    public Drawable getDrawable(int resourceId, Resources realResources) {
        return pickFor(resourceId).getDrawable(resourceId, realResources);
    }

    @Override
    public Drawable getXmlDrawable(int resourceId) {
        return pickFor(resourceId).getXmlDrawable(resourceId);
    }

    @Override
    public Drawable getAnimDrawable(int resourceId) {
        return pickFor(resourceId).getAnimDrawable(resourceId);
    }

    @Override
    public Drawable getColorDrawable(int resourceId) {
        return pickFor(resourceId).getColorDrawable(resourceId);
    }

    @Override
    public boolean isNinePatchDrawable(int drawableResourceId) {
        return pickFor(drawableResourceId).isNinePatchDrawable(drawableResourceId);
    }

    @Override
    public InputStream getRawValue(int id) {
        return pickFor(id).getRawValue(id);
    }

    @Override
    public String[] getStringArrayValue(int id, String qualifiers) {
        return pickFor(id).getStringArrayValue(id, qualifiers);
    }

    @Override
    public int[] getIntegerArrayValue(int id, String qualifiers) {
        return pickFor(id).getIntegerArrayValue(id, qualifiers);
    }

    @Override
    public PreferenceScreen inflatePreferences(Context context, int resourceId) {
        return pickFor(resourceId).inflatePreferences(context, resourceId);
    }

    @Override
    public ResourceExtractor getResourceExtractor() {
        return resourceExtractor;
    }

    @Override
    public ViewNode getLayoutViewNode(int id, String qualifiers) {
        return pickFor(id).getLayoutViewNode(id, qualifiers);
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        return pickFor(resName).getLayoutViewNode(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(int id, String qualifiers) {
        return pickFor(id).getMenuNode(id, qualifiers);
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
        ResName resName = resourceExtractor.getResName(id);
        if (resName == null) return new PackageResourceLoader();
        return pickFor(resName);
    }

    private ResourceLoader pickFor(ResName resName) {
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
