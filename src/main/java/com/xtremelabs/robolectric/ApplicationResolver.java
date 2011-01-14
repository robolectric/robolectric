package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ApplicationResolver {
    private File androidManifestPath;

    public ApplicationResolver(File androidManifestPath) {
        this.androidManifestPath = androidManifestPath;
    }

    public Application resolveApplication() {
        String applicationName = null;
        String packageName = null;
        try {
            Document manifestDoc =
                    DocumentBuilderFactory
                            .newInstance()
                            .newDocumentBuilder()
                            .parse(androidManifestPath);

            packageName = getTagAttributeText(manifestDoc, "manifest", "package");
            //TODO: should use getNamedItemNS, but that's not working as expected
            applicationName = getTagAttributeText(manifestDoc, "application", "android:name");
        } catch (Exception ignored) {
        }

        Application application;
        if (applicationName != null) {
            application = newApplicationInstance(packageName, applicationName);
        } else {
            application = new Application();
        }

        shadowOf(application).setPackageName(packageName);
        return application;
    }

    private String getTagAttributeText(Document doc, String tag, String attribute) {
        return doc.getElementsByTagName(tag).item(0).getAttributes().getNamedItem(attribute).getTextContent();
    }

    private Application newApplicationInstance(String packageName, String applicationName) {
        Application application;
        try {
            Class<? extends Application> applicationClass =
                    new ClassNameResolver<Application>(packageName, applicationName).resolve();
            application = applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return application;
    }
}
