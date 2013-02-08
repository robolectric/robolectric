package org.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;

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

    Document getXml(ResName resName, String qualifiers);

    DrawableNode getDrawableNode(ResName resName, String qualifiers);

    InputStream getRawValue(int id);

    String[] getStringArrayValue(ResName resName, String qualifiers);

    int[] getIntegerArrayValue(ResName resName, String qualifiers);

    PreferenceNode getPreferenceNode(ResName resName, String qualifiers);

    ResourceIndex getResourceExtractor();

    ViewNode getLayoutViewNode(ResName resName, String qualifiers);

    MenuNode getMenuNode(ResName resName, String qualifiers);

    boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute);

    String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part);
}
