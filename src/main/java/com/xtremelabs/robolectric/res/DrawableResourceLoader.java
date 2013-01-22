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
        drawableNodes.put("drawable", name, new DrawableNode.Xml(document, xmlContext), xmlContext);
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

    /**
     * Returns a collection of resource IDs for all nine-patch drawables
     * in the project.
     *
     * @param resourcePath
     */
    public void findNinePatchResources(ResourcePath resourcePath) {
        listNinePatchResources(resourcePath, resourcePath.resourceBase);
    }

    private void listNinePatchResources(ResourcePath resourcePath, File dir) {
        DirectoryMatchingFileFilter drawableFilter = new DirectoryMatchingFileFilter("drawable");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && drawableFilter.accept(f)) {
                    listNinePatchResources(resourcePath, f);
                } else {
                    String name = f.getName();
                    if (name.endsWith(".9.png")) {
                        String[] tokens = name.split("\\.9\\.png$");
                        String shortName = tokens[0];
                        XmlContext fakeXmlContext = new XmlContext(resourcePath.getPackageName(), f);
                        drawableNodes.put("drawable", shortName, new DrawableNode.ImageFile(true), fakeXmlContext);
                    }
                }
            }
        }
    }
}
