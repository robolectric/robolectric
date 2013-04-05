package org.robolectric;

import java.lang.reflect.Method;

public interface TestLifecycle<T> {
    void beforeTest(Method method);

    void prepareTest(Object test);

    T createApplication(Method method, AndroidManifest appManifest);

    void afterTest(Method method);
}