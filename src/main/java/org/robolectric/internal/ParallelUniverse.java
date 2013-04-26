package org.robolectric.internal;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.util.DatabaseConfig;

import java.lang.reflect.Method;

import static org.robolectric.Robolectric.shadowOf;

public class ParallelUniverse implements ParallelUniverseInterface {
    public void resetStaticState() {
        Robolectric.reset();
    }

    @Override public void setDatabaseMap(DatabaseConfig.DatabaseMap databaseMap) {
        DatabaseConfig.setDatabaseMap(databaseMap);
    }

    @Override public void setUpApplicationState(Method method, TestLifecycle testLifecycle, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
        Robolectric.application = null;

        ShadowResources.setSystemResources(systemResourceLoader);
        String qualifiers = RobolectricTestRunner.determineResourceQualifiers(method);
        Resources systemResources = Resources.getSystem();
        Configuration configuration = systemResources.getConfiguration();
        shadowOf(configuration).overrideQualifiers(qualifiers);
        systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());

        ResourceLoader resourceLoader = null;
        if (appManifest != null) {
            resourceLoader = RobolectricTestRunner.getAppResourceLoader(systemResourceLoader, appManifest);
        }

        final Application application = (Application) testLifecycle.createApplication(method, appManifest);
        if (application != null) {
            shadowOf(application).bind(appManifest, resourceLoader);
            Resources resources = application.getResources();
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            shadowOf(application).setStrictI18n(strictI18n);

            Robolectric.application = application;
            application.onCreate();
        }
    }

    @Override public void tearDownApplication() {
        if (Robolectric.application != null) {
            Robolectric.application.onTerminate();
        }
    }

    @Override public Object getCurrentApplication() {
        return Robolectric.application;
    }
}
