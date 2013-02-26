package org.robolectric.res;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;

public class DocumentLoader {
    private static final FileFilter ENDS_WITH_XML = new FileFilter() {
        @Override public boolean accept(File file) {
            return file.getName().endsWith(".xml");
        }
    };

    private final XmlLoader[] xmlLoaders;
    private final DocumentBuilderFactory documentBuilderFactory;

    public DocumentLoader(XmlLoader... xmlLoaders) {
        this.xmlLoaders = xmlLoaders;

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    }

    public void loadResourceXmlSubDirs(ResourcePath resourcePath, final String folderBaseName) throws Exception {
        for (File dir : resourcePath.resourceBase.listFiles(new DirectoryMatchingFileFilter(folderBaseName))) {
            loadResourceXmlDir(resourcePath, dir);
        }
    }

    public void loadResourceXmlDir(ResourcePath resourcePath, String dirName) throws Exception {
        loadResourceXmlDir(resourcePath, new File(resourcePath.resourceBase, dirName));
    }

    private void loadResourceXmlDir(ResourcePath resourcePath, File dir) throws Exception {
        if (!dir.exists()) {
            throw new RuntimeException("no such directory " + dir);
        }

        for (File file : dir.listFiles(ENDS_WITH_XML)) {
            loadResourceXmlFile(file, resourcePath.getPackageName());
        }
    }

    private void loadResourceXmlFile(File file, String packageName) throws Exception {
        Document document = parse(file);
        for (XmlLoader xmlLoader : xmlLoaders) {
            xmlLoader.processResourceXml(file, document, packageName);
        }
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

}
