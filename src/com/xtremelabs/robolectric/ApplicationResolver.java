package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.util.ClassNameResolver;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ApplicationResolver {
    private String androidManifestPath;

    public ApplicationResolver(String androidManifestPath) {
        this.androidManifestPath = androidManifestPath;
    }

    public Application resolveApplication() {
        Class<? extends Application> applicationClass;
        String applicationName = null;
        String projectPackage = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(androidManifestPath);

            projectPackage = doc.getElementsByTagName("manifest").item(0).getAttributes().getNamedItem("package").getTextContent();
            //TODO: should use getNamedItemNS, but that's not working as expected
            applicationName = doc.getElementsByTagName("application").item(0).getAttributes().getNamedItem("android:name").getTextContent();
        } catch (Exception ignored) {
        }

        try {
            if (applicationName != null) {
                applicationClass = new ClassNameResolver<Application>(projectPackage, applicationName).resolve();
                return applicationClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new Application();
    }
}
