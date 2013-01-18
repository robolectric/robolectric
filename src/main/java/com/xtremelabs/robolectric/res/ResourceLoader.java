package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceScreen;
import android.view.View;
import com.xtremelabs.robolectric.tester.android.util.ResName;

import java.io.InputStream;

public interface ResourceLoader {
    String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    String getNameForId(int viewId);

    int getColorValue(int id, String qualifiers);

    String getStringValue(int id, String qualifiers);

    String getPluralStringValue(int id, int quantity, String qualifiers);

    float getDimenValue(int id, String qualifiers);

    int getIntegerValue(int id, String qualifiers);

    boolean getBooleanValue(int id, String qualifiers);

    XmlResourceParser getXml(int id);

    boolean isDrawableXml(int resourceId, String qualifiers);

    boolean isAnimatableXml(int resourceId, String qualifiers);

    int[] getDrawableIds(int resourceId, String qualifiers);

    Drawable getDrawable(int resourceId, Resources realResources, String qualifiers);

    boolean isNinePatchDrawable(int drawableResourceId);

    InputStream getRawValue(int id);

    String[] getStringArrayValue(int id, String qualifiers);

    int[] getIntegerArrayValue(int id, String qualifiers);

    PreferenceScreen inflatePreferences(Context context, int resourceId);

    ResourceExtractor getResourceExtractor();

    ViewNode getLayoutViewNode(int id, String qualifiers);

    ViewNode getLayoutViewNode(ResName resName, String qualifiers);

    MenuNode getMenuNode(int id, String qualifiers);

    MenuNode getMenuNode(ResName resName, String qualifiers);

    boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute);

    String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part);
}
