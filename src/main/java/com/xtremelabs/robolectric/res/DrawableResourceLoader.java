package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.w3c.dom.Document;

import java.io.File;
import java.util.Set;

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

    /**
     * Returns a collection of resource IDs for all nine-patch drawables
     * in the project.
     *
     * @param resourceIds
     * @param resourcePath
     */
    public void listNinePatchResources(Set<ResName> resourceIds, ResourcePath resourcePath) {
        listNinePatchResources(resourceIds, resourcePath, resourcePath.resourceBase);
    }

    private void listNinePatchResources(Set<ResName> resourceIds, ResourcePath resourcePath, File dir) {
        DirectoryMatchingFileFilter drawableFilter = new DirectoryMatchingFileFilter("drawable");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && drawableFilter.accept(f)) {
                    listNinePatchResources(resourceIds, resourcePath, f);
                } else {
                    String name = f.getName();
                    if (name.endsWith(".9.png")) {
                        String[] tokens = name.split("\\.9\\.png$");
                        resourceIds.add(new ResName(resourcePath.getPackageName(), "drawable", tokens[0]));
                    }
                }
            }
        }
    }
}
