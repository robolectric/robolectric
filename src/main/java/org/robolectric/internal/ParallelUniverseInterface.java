package org.robolectric.internal;

import org.robolectric.SdkEnvironment;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.DatabaseConfig;

import java.lang.reflect.Method;

public interface ParallelUniverseInterface {
    public void resetStaticState();

    void setDatabaseMap(DatabaseConfig.DatabaseMap databaseMap);

    void setUpApplicationState(Method method, TestLifecycle testLifecycle, SdkEnvironment sdkEnvironment, boolean strictI18n, ResourceLoader systemResourceLoader);

    void tearDownApplication();

    Object getCurrentApplication();
}
