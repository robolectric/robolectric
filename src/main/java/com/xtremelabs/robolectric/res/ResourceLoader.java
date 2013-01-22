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

    String getNameForId(int id);

    int getColorValue(ResName resName, String qualifiers);

    String getStringValue(ResName resName, String qualifiers);

    String getPluralStringValue(ResName resName, int quantity, String qualifiers);

    float getDimenValue(ResName resName, String qualifiers);

    int getIntegerValue(ResName resName, String qualifiers);

    boolean getBooleanValue(ResName resName, String qualifiers);

    XmlResourceParser getXml(int id);

    Drawable getDrawable(ResName resName, Resources realResources, String qualifiers);

    InputStream getRawValue(int id);

    String[] getStringArrayValue(ResName resName, String qualifiers);

    int[] getIntegerArrayValue(ResName resName, String qualifiers);

    PreferenceScreen inflatePreferences(Context context, int resourceId);

    ResourceExtractor getResourceExtractor();

    ViewNode getLayoutViewNode(ResName resName, String qualifiers);

    MenuNode getMenuNode(ResName resName, String qualifiers);

    boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute);

    String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part);
}
