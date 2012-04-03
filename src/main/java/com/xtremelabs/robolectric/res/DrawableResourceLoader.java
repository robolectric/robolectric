package com.xtremelabs.robolectric.res;

import android.R;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowStateListDrawable;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader extends XmlLoader {

    // Put all the states for a StateListDrawable in the into a Map for looking up
    // http://developer.android.com/guide/topics/resources/drawable-resource.html#StateList
    private static final Map<String, Integer> stateMap = new HashMap<String, Integer>();
    static {
        stateMap.put("android:state_selected", R.attr.state_selected);
        stateMap.put("android:state_pressed", R.attr.state_pressed);
        stateMap.put("android:state_focused", R.attr.state_focused);
        stateMap.put("android:state_checkable", R.attr.state_checkable);
        stateMap.put("android:state_checked", R.attr.state_checked);
        stateMap.put("android:state_enabled", R.attr.state_enabled);
        stateMap.put("android:state_window_focused", R.attr.state_window_focused);
    }

    /** document */
    protected Map<String, Document> documents = new HashMap<String, Document>();

    /** resource directory */
    protected File resourceDirectory;

    /**
     * DrawableResourceLoader constructor.
     *
     * @param extractor         Extractor
     * @param resourceDirectory Resource directory
     */
    public DrawableResourceLoader(ResourceExtractor extractor, File resourceDirectory) {
        super(extractor);
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * Check if resource is xml.
     *
     * @param resourceId Resource id
     * @return Boolean
     */
    public boolean isXml(int resourceId) {
        return documents.containsKey(resourceExtractor.getResourceName(resourceId));
    }

    public Drawable getXmlDrawable(int resId) {

        if (!isXml(resId)) {
            return null;
        }

        Document xmlDoc = documents.get(resourceExtractor.getResourceName(resId));
        NodeList nodes = xmlDoc.getElementsByTagName("selector");
        if (nodes != null && nodes.getLength() > 0) {
            return buildStateListDrawable(xmlDoc);
        }

        nodes = xmlDoc.getElementsByTagName("layer-list");
        if (nodes != null && nodes.getLength() > 0) {
            return new LayerDrawable(null);
        }

        nodes = xmlDoc.getElementsByTagName("animation-list");
        if (nodes != null && nodes.getLength() > 0) {
            return new AnimationDrawable();
        }

        return null;
    }

    /**
     * Store document locally keyed by resource name.
     *
     * @param xmlFile  Xml file
     * @param document Document
     * @param isSystem System resource
     * @throws Exception
     * @see com.xtremelabs.robolectric.res.XmlLoader#processResourceXml(java.io.File,
     *      org.w3c.dom.Document, boolean)
     */
    @Override
    protected void processResourceXml(File xmlFile, Document document, boolean isSystem) throws Exception {
        String name = toResourceName(xmlFile);
        if (!documents.containsKey(name)) {
            if (isSystem) {
                name = "android:" + name;
            }
            documents.put(name, document);
        }
    }

    /**
     * Convert file name to resource name.
     *
     * @param xmlFile Xml File
     * @return Resource name
     */
    private String toResourceName(File xmlFile) {
        try {
            return xmlFile.getCanonicalPath().replaceAll("[/\\\\\\\\]", "/")
                    .replaceAll("^.*?/res/", "").replaceAll("\\..+$", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Get drawables by resource id.
     *
     * @param resourceId Resource id
     * @return Drawables
     */
    protected int[] getDrawableIds(int resourceId) {
        String resourceName = resourceExtractor.getResourceName(resourceId);
        Document document = documents.get(resourceName);

        NodeList items = document.getElementsByTagName("item");
        int[] drawableIds = new int[items.getLength()];

        for (int i = 0; i < items.getLength(); i++) {
            if (resourceName.startsWith("android:")) {
                drawableIds[i] = -1;
            } else {
                Node item = items.item(i);
                Node drawableName = item.getAttributes().getNamedItem("android:drawable");
                if (drawableName != null) {
                    drawableIds[i] = resourceExtractor.getResourceId(drawableName.getNodeValue());
                }
            }
        }

        return drawableIds;
    }

    public boolean isAnimationDrawable(int resourceId) {
        Document document = documents.get(resourceExtractor.getResourceName(resourceId));
        return "animation-list".equals(document.getDocumentElement().getLocalName());
    }

    private StateListDrawable buildStateListDrawable(Document d) {
        StateListDrawable drawable = new StateListDrawable();
        ShadowStateListDrawable shDrawable = Robolectric.shadowOf(drawable);
        NodeList items = d.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            Node drawableName = node.getAttributes().getNamedItem("android:drawable");
            if (drawableName != null) {
                int resId = resourceExtractor.getResourceId(drawableName.getNodeValue());
                int stateId = getStateId(node);
                shDrawable.addState(stateId, resId);
            }
        }
        return drawable;
    }

    private int getStateId(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        for (String state : stateMap.keySet()) {
            Node attr = attrs.getNamedItem(state);
            if (attr != null) {
                return stateMap.get(state);
            }
        }

        // if a state wasn't specified, return the default state
        return R.attr.state_active;
    }
}
