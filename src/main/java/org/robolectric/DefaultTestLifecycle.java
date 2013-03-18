package org.robolectric;

import android.app.Application;
import org.robolectric.internal.TestLifecycle;

import java.lang.reflect.Method;

public class DefaultTestLifecycle implements TestLifecycle {
    /**
     * Called before each test method is run.
     *
     * @param method the test method about to be run
     */
    public void beforeTest(final Method method) {
    }

    /**
     * Called after each test method is run.
     *
     * @param method the test method that just ran.
     */
    public void afterTest(final Method method) {
    }

    public void prepareTest(final Object test) {
    }

    /**
     * Override this method if you want to provide your own implementation of Application.
     * <p/>
     * This method attempts to instantiate an application instance as specified by the AndroidManifest.xml.
     *
     * @param method The currently-running test method.
     * @param appManifest The application manifest.
     * @return An instance of the Application class specified by the ApplicationManifest.xml or an instance of
     *         Application if not specified.
     */
    public Application createApplication(Method method, AndroidManifest appManifest) {
        return new ApplicationResolver(appManifest).resolveApplication();
    }
}
