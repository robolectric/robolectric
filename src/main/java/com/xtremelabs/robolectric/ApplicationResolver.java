package com.xtremelabs.robolectric;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.xtremelabs.robolectric.internal.ClassNameResolver;
import com.xtremelabs.robolectric.res.RobolectricPackageManager;
import com.xtremelabs.robolectric.shadows.ShadowApplication;
import org.w3c.dom.Document;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

public class ApplicationResolver {
    private final AndroidManifest androidManifest;

    public ApplicationResolver(AndroidManifest androidManifest) {
        this.androidManifest = androidManifest;
    }

    public Application resolveApplication() {
        Application application;
        if (androidManifest.getApplicationName() != null) {
            application = newApplicationInstance();
        } else {
            application = new Application();
        }

        injectShadow(application);

        return application;
    }

    public void injectShadow(Application application) {
        ShadowApplication shadowApplication = shadowOf(application);
        shadowApplication.setPackageName(androidManifest.getPackageName());
        shadowApplication.setApplicationName(androidManifest.getApplicationName());

        shadowApplication.setPackageManager(new RobolectricPackageManager(application, androidManifest));
        registerBroadcastReceivers(shadowApplication);
    }

    private void registerBroadcastReceivers(ShadowApplication shadowApplication) {
        for (int i = 0; i < androidManifest.getReceiverCount(); i++) {
            IntentFilter filter = new IntentFilter();
            for (String action : androidManifest.getReceiverIntentFilterActions(i)) {
                filter.addAction(action);
            }
            String receiverClassName = replaceLastDotWith$IfInnerStaticClass(androidManifest.getReceiverClassName(i));
            shadowApplication.registerReceiver((BroadcastReceiver) Robolectric.newInstanceOf(receiverClassName), filter);
        }
    }

    private String replaceLastDotWith$IfInnerStaticClass(String receiverClassName) {
        String[] splits = receiverClassName.split("\\.");
        String staticInnerClassRegex = "[A-Z][a-zA-Z]*";
        if (splits[splits.length - 1].matches(staticInnerClassRegex) && splits[splits.length - 2].matches(staticInnerClassRegex)) {
            int lastDotIndex = receiverClassName.lastIndexOf(".");
            StringBuilder buffer = new StringBuilder(receiverClassName);
            buffer.setCharAt(lastDotIndex, '$');
            return buffer.toString();
        }
        return receiverClassName;
    }

    private String getTagAttributeText(Document doc, String tag, String attribute) {
        return doc.getElementsByTagName(tag).item(0).getAttributes().getNamedItem(attribute).getTextContent();
    }

    private Application newApplicationInstance() {
        Application application;
        try {
            Class<? extends Application> applicationClass =
                    new ClassNameResolver<Application>(androidManifest.getPackageName(), androidManifest.getApplicationName()).resolve();
            application = applicationClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return application;
    }
}
