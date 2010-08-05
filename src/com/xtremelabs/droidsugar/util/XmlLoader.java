package com.xtremelabs.droidsugar.util;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public abstract class XmlLoader {
    public static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    protected DocumentBuilderFactory documentBuilderFactory;
    protected ResourceExtractor resourceExtractor;

    public XmlLoader(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
        documentBuilderFactory = getDocumentBuilderFactory();
    }

    public void loadDirs(File... resourceXmlDirs) throws Exception {
        for (File resourceXmlDir : resourceXmlDirs) {
            addResourceXmlDir(resourceXmlDir);
        }
    }

    public DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        return documentBuilderFactory;
    }

    protected void addResourceXmlDir(File resourceXmlDir) throws Exception {
        if (!resourceXmlDir.exists()) {
            throw new RuntimeException("no such directory " + resourceXmlDir);
        }
        for (File file1 : resourceXmlDir.listFiles()) {
            processResourceXml(file1, parse(file1));
        }
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

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }
}
