package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;

public class DocumentLoader {
    private final XmlLoader[] xmlLoaders;
    private final DocumentBuilderFactory documentBuilderFactory;
    private FileFilter xmlFileFilter = new FileFilter() {
        @Override public boolean accept(File file) {
            return file.getName().endsWith(".xml");
        }
    };

    public DocumentLoader(XmlLoader... xmlLoaders) {
        this.xmlLoaders = xmlLoaders;

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    }

    public void loadResourceXmlDirs(File... resourceXmlDirs) throws Exception {
        loadResourceXmlDirs(false, resourceXmlDirs);
    }

    public void loadResourceXmlDirs(boolean isSystem, File... resourceXmlDirs) throws Exception {
        for (File resourceXmlDir : resourceXmlDirs) {
            loadResourceXmlDir(resourceXmlDir, isSystem);
        }
    }

    public void loadResourceXmlDir(File resourceXmlDir) throws Exception {
        loadResourceXmlDir(resourceXmlDir, false);
    }

    public void loadSystemResourceXmlDir(File resourceXmlDir) throws Exception {
        loadResourceXmlDir(resourceXmlDir, true);
    }

    private void loadResourceXmlDir(File resourceXmlDir, boolean isSystem) throws Exception {
        if (!resourceXmlDir.exists()) {
            throw new RuntimeException("no such directory " + resourceXmlDir);
        }

        for (File file : resourceXmlDir.listFiles(xmlFileFilter)) {
            loadResourceXmlFile(file, isSystem);
        }
    }

    private void loadResourceXmlFile(File file, boolean isSystem) throws Exception {
        for (XmlLoader xmlLoader : xmlLoaders) {
            xmlLoader.processResourceXml(file, parse(file), isSystem);
        }
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

}
