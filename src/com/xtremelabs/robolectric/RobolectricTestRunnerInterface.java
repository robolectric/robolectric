package com.xtremelabs.robolectric;

import java.lang.reflect.Method;

public interface RobolectricTestRunnerInterface {
    Object createTest() throws Exception;

    void internalBeforeTest(Method method);

    void internalAfterTest(Method method);

    void setProjectRoot(String projectRoot);

    void setResourceDirectory(String resourceDirectory);
}
