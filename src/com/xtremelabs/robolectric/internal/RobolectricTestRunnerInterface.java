package com.xtremelabs.robolectric.internal;

import java.lang.reflect.Method;

public interface RobolectricTestRunnerInterface {
    Object createTest() throws Exception;

    void internalBeforeTest(Method method);

    void internalAfterTest(Method method);

    void setAndroidManifestPath(String projectRoot);

    void setResourceDirectory(String resourceDirectory);
}
