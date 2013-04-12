package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentLoader {
    public static boolean DEBUG_PERF = false;
    private Map<String, Long> perfResponsibleParties = new HashMap<String, Long>();

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
        if (DEBUG_PERF) perfResponsibleParties.clear();

        File[] files = resourcePath.resourceBase.listFiles(new DirectoryMatchingFileFilter(folderBaseName));
        if (files == null) {
            throw new RuntimeException(resourcePath.resourceBase + " is not a directory");
        }
        for (File dir : files) {
            loadResourceXmlDir(dir, xmlLoaders);
        }

        if (DEBUG_PERF) {
            System.out.println(String.format("%4dms spent in %s", System.currentTimeMillis() - startTime, folderBaseName));
            List<String> keys = new ArrayList<String>(perfResponsibleParties.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                System.out.println(String.format("* %-20s: %4dms", key, perfResponsibleParties.get(key)));
            }
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
        long startTime = DEBUG_PERF ? System.currentTimeMillis() : 0;
        Document document = parse(file);
        if (DEBUG_PERF) perfBlame("DocumentLoader.parse", startTime);

        for (XmlLoader xmlLoader : xmlLoaders) {
            startTime = DEBUG_PERF ? System.currentTimeMillis() : 0;
            xmlLoader.processResourceXml(file, document, packageName);
            if (DEBUG_PERF) perfBlame(xmlLoader.getClass().getName(), startTime);
        }
    }

    private void perfBlame(String responsibleParty, long startTime) {
        long myElapsedMs = System.currentTimeMillis() - startTime;
        Long totalElapsedMs = perfResponsibleParties.get(responsibleParty);
        perfResponsibleParties.put(responsibleParty, totalElapsedMs == null ? myElapsedMs : totalElapsedMs + myElapsedMs);
    }

    private Document parse(File xmlFile) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(xmlFile);
    }

}
