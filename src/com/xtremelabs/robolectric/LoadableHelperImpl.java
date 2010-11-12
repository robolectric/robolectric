package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class LoadableHelperImpl implements LoadableHelper {

    private ResourceLoader resourceLoader;

    @Override public void setupApplicationState(String projectRoot, String resourceDir) {
        createResourceLoader(projectRoot, resourceDir);

        Robolectric.bindDefaultShadowClasses();
        Robolectric.resetStaticState();
        Robolectric.application = ShadowApplication.bind(new Application(), resourceLoader);
    }

    private void createResourceLoader(String projectRoot, String resourceDirectory) {
        if (resourceLoader == null) {
            try {
                String rClassName = findResourcePackageName(projectRoot);
                Class rClass = Class.forName(rClassName);
                resourceLoader = new ResourceLoader(rClass, new File(resourceDirectory));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String findResourcePackageName(String projectRoot) throws ParserConfigurationException, IOException, SAXException {
        File projectManifestFile = new File(projectRoot, "AndroidManifest.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(projectManifestFile);

        String projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();

        return projectPackage + ".R";
    }
}
