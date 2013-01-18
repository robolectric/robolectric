package com.xtremelabs.robolectric.res;

import android.R;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.shadows.ShadowStateListDrawable;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

public class DrawableBuilder {
    // Put all the states for a StateListDrawable in the into a Map for looking up
    // http://developer.android.com/guide/topics/resources/drawable-resource.html#StateList
    static final Map<String, Integer> STATE_MAP = new HashMap<String, Integer>();
    static {
        STATE_MAP.put("android:state_selected", R.attr.state_selected);
        STATE_MAP.put("android:state_pressed", R.attr.state_pressed);
        STATE_MAP.put("android:state_focused", R.attr.state_focused);
        STATE_MAP.put("android:state_checkable", R.attr.state_checkable);
        STATE_MAP.put("android:state_checked", R.attr.state_checked);
        STATE_MAP.put("android:state_enabled", R.attr.state_enabled);
        STATE_MAP.put("android:state_window_focused", R.attr.state_window_focused);
    }

    private final ResourceExtractor resourceExtractor;
    private ResBundle<DrawableNode> drawableNodes;

    public DrawableBuilder(ResBundle<DrawableNode> drawableNodes, ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
        this.drawableNodes = drawableNodes;
    }

    /**
     * Check if resource is xml.
     *
     * @param resourceId Resource id
     * @param qualifiers
     * @return Boolean
     */
    public boolean isXml(int resourceId, String qualifiers) {
        return drawableNodes.get(resourceExtractor.getResName(resourceId), qualifiers) != null;
    }

    public Drawable getXmlDrawable(int resId, String qualifiers) {
        return getXmlDrawable(resourceExtractor.getResName(resId), qualifiers);
    }

    private Drawable getXmlDrawable(ResName resName, String qualifiers) {
        DrawableNode drawableNode = drawableNodes.get(resName, qualifiers);
        if (drawableNode == null) return null;

        Document xmlDoc = drawableNode.document;
        NodeList nodes = xmlDoc.getElementsByTagName("selector");
        if (nodes != null && nodes.getLength() > 0) {
            return buildStateListDrawable(drawableNode);
        }

        nodes = xmlDoc.getElementsByTagName("layer-list");
        if (nodes != null && nodes.getLength() > 0) {
            NodeList itemNodes = findNodes("/layer-list/item", xmlDoc);
            Drawable[] layers = new Drawable[itemNodes.getLength()];
            for (int i = 0; i < itemNodes.getLength(); i++) {
                Node node = itemNodes.item(i);
                String drawableName = node.getAttributes().getNamedItemNS(ResourceLoader.ANDROID_NS, "drawable").getNodeValue();
                layers[i] = getXmlDrawable(resName.qualify(drawableName), qualifiers);
            }
            return new LayerDrawable(layers);
        }

        nodes = xmlDoc.getElementsByTagName("animation-list");
        if (nodes != null && nodes.getLength() > 0) {
            return new AnimationDrawable();
        }

        return null;
    }

    private NodeList findNodes(String xpathExpression, Document xmlDoc) {
        try {
            return (NodeList) XPathFactory.newInstance().newXPath().compile(xpathExpression).evaluate(xmlDoc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get drawables by resource id.
     *
     * @param resourceId Resource id
     * @param qualifiers
     * @return Drawables
     */
    protected int[] getDrawableIds(int resourceId, String qualifiers) {
        ResName resName = resourceExtractor.getResName(resourceId);
        DrawableNode drawableNode = drawableNodes.get(resName, qualifiers);
        Document document = drawableNode.document;

        NodeList items = document.getElementsByTagName("item");
        int[] drawableIds = new int[items.getLength()];

        for (int i = 0; i < items.getLength(); i++) {
            if (resName.namespace.equals("android:")) {
                drawableIds[i] = -1;
            } else {
                Node item = items.item(i);
                Node drawableName = item.getAttributes().getNamedItemNS(ResourceLoader.ANDROID_NS, "drawable");
                if (drawableName != null) {
                    Integer drawableId = resourceExtractor.getResourceId(drawableName.getNodeValue(), drawableNode.xmlContext.packageName);
                    drawableIds[i] = drawableId == null ? -1 : drawableId;
                }
            }
        }

        return drawableIds;
    }

    public boolean isAnimationDrawable(int resourceId, String qualifiers) {
        DrawableNode drawableNode = drawableNodes.get(resourceExtractor.getResName(resourceId), qualifiers);
        Document document = drawableNode.document;
        return "animation-list".equals(document.getDocumentElement().getLocalName());
    }

    private StateListDrawable buildStateListDrawable(DrawableNode drawableNode) {
        StateListDrawable drawable = new StateListDrawable();
        ShadowStateListDrawable shDrawable = Robolectric.shadowOf(drawable);
        NodeList items = drawableNode.document.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            Node drawableName = node.getAttributes().getNamedItemNS(ResourceLoader.ANDROID_NS, "drawable");
            if (drawableName != null) {
                int resId = resourceExtractor.getResourceId(drawableName.getNodeValue(), drawableNode.xmlContext.packageName);
                int stateId = getStateId(node);
                shDrawable.addState(stateId, resId);
            }
        }
        return drawable;
    }

    private int getStateId(Node node) {
        NamedNodeMap attrs = node.getAttributes();
        for (String state : STATE_MAP.keySet()) {
            Node attr = attrs.getNamedItem(state);
            if (attr != null) {
                return STATE_MAP.get(state);
            }
        }

        // if a state wasn't specified, return the default state
        return R.attr.state_active;
    }

}
