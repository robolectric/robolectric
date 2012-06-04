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

        ShadowApplication shadowApplication = shadowOf(application);
        shadowApplication.setPackageName(packageName);
        shadowApplication.setApplicationName(applicationName);

        shadowApplication.setPackageManager(new RobolectricPackageManager(application, config));
        registerBroadcastReceivers(shadowApplication);

        return application;
    }

    private void registerBroadcastReceivers(ShadowApplication shadowApplication) {
        for (int i = 0; i < config.getReceiverCount(); i++) {
            IntentFilter filter = new IntentFilter();
            for (String action : config.getReceiverIntentFilterActions(i)) {
                filter.addAction(action);
            }
            String receiverClassName = replaceLastDotWith$IfInnerStaticClass(config.getReceiverClassName(i));
            shadowApplication.registerReceiver((BroadcastReceiver) Robolectric.newInstanceOf(receiverClassName), filter);
        }
    }

    private String replaceLastDotWith$IfInnerStaticClass(String receiverClassName) {
        String[] splits = receiverClassName.split("\\.");
        String staticInnerClassRegex = "[A-Z][a-zA-Z]*";
        if (splits[splits.length - 1].matches(staticInnerClassRegex) && splits[splits.length - 2].matches(staticInnerClassRegex)) {
            int lastDotIndex = receiverClassName.lastIndexOf(".");
            StringBuffer buffer = new StringBuffer(receiverClassName);
            buffer.setCharAt(lastDotIndex,'$');
            return buffer.toString();
        }
        return receiverClassName;
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
