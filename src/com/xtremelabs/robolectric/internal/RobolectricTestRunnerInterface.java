package com.xtremelabs.robolectric.internal;

import java.io.File;
import java.lang.reflect.Method;

public interface RobolectricTestRunnerInterface {
    Object createTest() throws Exception;

    void internalBeforeTest(Method method);

    void internalAfterTest(Method method);

    void setAndroidManifestPath(File projectRoot);

    void setResourceDirectory(File resourceDirectory);
}
