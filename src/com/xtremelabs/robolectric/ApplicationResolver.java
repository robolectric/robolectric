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

            projectPackage = getTagAttributeText(doc, "manifest", "package");
            //TODO: should use getNamedItemNS, but that's not working as expected
            applicationName = getTagAttributeText(doc, "application", "android:name");
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

    private String getTagAttributeText(Document doc, String tag, String attribute) {
        return doc.getElementsByTagName(tag).item(0).getAttributes().getNamedItem(attribute).getTextContent();
    }
}
