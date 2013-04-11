package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;

public class DocumentLoader {
    public static boolean DEBUG_PERF = false;

    private static final FileFilter ENDS_WITH_XML = new FileFilter() {
        @Override public boolean accept(@NotNull File file) {
            return file.getName().endsWith(".xml");
        }
    };

    private final ResourcePath resourcePath;
    private final DocumentBuilderFactory documentBuilderFactory;

    public DocumentLoader(ResourcePath resourcePath) {
        this.resourcePath = resourcePath;

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    }

    public void loadResourceXmlSubDirs(String folderBaseName, XmlLoader... xmlLoaders) throws Exception {
        long startTime = System.currentTimeMillis();

        File[] files = resourcePath.resourceBase.listFiles(new DirectoryMatchingFileFilter(folderBaseName));
        if (files == null) {
            throw new RuntimeException(resourcePath.resourceBase + " is not a directory");
        }
        for (File dir : files) {
            loadResourceXmlDir(dir, xmlLoaders);
        }

        if (DEBUG_PERF) {
            System.out.println(String.format("%4dms spent in " + folderBaseName, System.currentTimeMillis() - startTime));
        }
    }

    public void loadResourceXmlDir(String dirName, XmlLoader... xmlLoaders) throws Exception {
        loadResourceXmlDir(new File(resourcePath.resourceBase, dirName), xmlLoaders);
    }

    private void loadResourceXmlDir(File dir, XmlLoader... xmlLoaders) throws Exception {
        if (!dir.exists()) {
            throw new RuntimeException("no such directory " + dir);
        }

        for (File file : dir.listFiles(ENDS_WITH_XML)) {
            loadResourceXmlFile(file, resourcePath.getPackageName(), xmlLoaders);
        }
    }

    private void loadResourceXmlFile(File file, String packageName, XmlLoader... xmlLoaders) throws Exception {
        long startTime = System.currentTimeMillis();

        Document document = parse(file);
        for (XmlLoader xmlLoader : xmlLoaders) {
            xmlLoader.processResourceXml(file, document, packageName);
        }

        if (DEBUG_PERF) {
            System.out.println(String.format("  %4dms spent on " + file, System.currentTimeMillis() - startTime));
        }
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

}
