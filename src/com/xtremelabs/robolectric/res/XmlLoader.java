package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;

public abstract class XmlLoader {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    protected ResourceExtractor resourceExtractor;

    public XmlLoader(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
    }


    protected abstract void processResourceXml(File xmlFile, Document document) throws Exception;

    protected String getIdAttr(Node node) {
        return getAttr(node, "id");
    }

    protected String getAttr(Node node, String attrName) {
        NamedNodeMap attributes = node.getAttributes();
        Node idAttr = attributes.getNamedItemNS(ANDROID_NS, attrName);
        return idAttr != null ? idAttr.getNodeValue() : null;
    }

}
