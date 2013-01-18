package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import java.io.File;

/**
 * DrawableResourceLoader
 */
public class DrawableResourceLoader extends XmlLoader {
    private final ResBundle<DrawableNode> drawableNodes;

    public DrawableResourceLoader(ResBundle<DrawableNode> drawableNodes) {
        this.drawableNodes = drawableNodes;
    }

    /**
     * Store document locally keyed by resource name.
     *
     *
     *
     * @param xmlFile  Xml file
     * @param document Document
     * @param xmlContext System resource
     * @throws Exception
     * @see XmlLoader#processResourceXml(java.io.File, org.w3c.dom.Document, XmlContext)
     */
    @Override
    protected void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception {
        String name = toResourceName(xmlFile);
        drawableNodes.put("drawable", name, new DrawableNode(document, xmlContext), xmlContext);
    }

    /**
     * Convert file name to resource name.
     *
     * @param xmlFile Xml File
     * @return Resource name
     */
    private String toResourceName(File xmlFile) {
        return xmlFile.getName().replaceAll("\\..+$", "");
    }
}
