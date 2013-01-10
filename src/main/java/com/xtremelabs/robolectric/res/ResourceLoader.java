package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public interface ResourceLoader {
    void setStrictI18n(boolean strict);

    boolean getStrictI18n();

    TestAttributeSet createAttributeSet(List<Attribute> attributes, Class<? extends View> viewClass);

    String getNameForId(int viewId);

    int getColorValue(int id, String qualifiers);

    String getStringValue(int id, String qualifiers);

    String getPluralStringValue(int id, int quantity, String qualifiers);

    float getDimenValue(int id, String qualifiers);

    int getIntegerValue(int id, String qualifiers);

    boolean getBooleanValue(int id, String qualifiers);

    XmlResourceParser getXml(int id);

    boolean isDrawableXml(int resourceId);

    boolean isAnimatableXml(int resourceId);

    int[] getDrawableIds(int resourceId);

    Drawable getDrawable(int resourceId, Resources realResources);

    Drawable getXmlDrawable(int resourceId);

    Drawable getAnimDrawable(int resourceId);

    Drawable getColorDrawable(int resourceId);

    boolean isNinePatchDrawable(int drawableResourceId);

    InputStream getRawValue(int id);

    String[] getStringArrayValue(int id, String qualifiers);

    int[] getIntegerArrayValue(int id, String qualifiers);

    void inflateMenu(Context context, int resource, Menu root);

    PreferenceScreen inflatePreferences(Context context, int resourceId);

    File getAssetsBase();

    ResourceExtractor getResourceExtractor();

    ViewNode getLayoutViewNode(int id, String qualifiers);

    ViewNode getLayoutViewNode(ResName resName, String qualifiers);
}
