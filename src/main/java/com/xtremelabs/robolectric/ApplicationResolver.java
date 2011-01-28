package com.xtremelabs.robolectric;

import android.app.Application;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import org.w3c.dom.Document;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ApplicationResolver {
    RobolectricConfig config;

    public ApplicationResolver(RobolectricConfig config) {
        this.config = config;
    }

    public Application resolveApplication() {
        String applicationName = config.getApplicationName();
        String packageName = config.getPackageName();

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
