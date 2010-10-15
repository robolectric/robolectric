package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class DocumentLoader {
    private final XmlLoader[] xmlLoaders;
    private final DocumentBuilderFactory documentBuilderFactory;

    public DocumentLoader(XmlLoader... xmlLoaders) {
        this.xmlLoaders = xmlLoaders;

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    }

    public void loadResourceXmlDirs(File... resourceXmlDirs) throws Exception {
        for (File resourceXmlDir : resourceXmlDirs) {
            loadResourceXmlDir(resourceXmlDir);
        }
    }

    public void loadResourceXmlDir(File resourceXmlDir) throws Exception {
        if (!resourceXmlDir.exists()) {
            throw new RuntimeException("no such directory " + resourceXmlDir);
        }
        for (File file : resourceXmlDir.listFiles()) {
            loadResourceXmlFile(file);
        }
    }

    public void loadResourceXmlFile(File file) throws Exception {
        for (XmlLoader xmlLoader : xmlLoaders) {
            xmlLoader.processResourceXml(file, parse(file));
        }
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

}
