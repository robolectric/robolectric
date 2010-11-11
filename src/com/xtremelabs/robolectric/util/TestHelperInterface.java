package com.xtremelabs.robolectric.util;

import java.lang.reflect.Method;

/**
 * @deprecated The defaults used by {@link com.xtremelabs.robolectric.AbstractRobolectricTestRunner} will now work in
 * almost all cases. See that class for more information about how to customize its behavior.
 */
@Deprecated
public interface TestHelperInterface {
    /**
     * This method is run before each test.  This is intended to be used as a global before each.
     *
     * @param method The test method that is about to be run.
     */
    void before(Method method);

    /**
     * This method is run after each test.  This is intended to be used as a global after each.
     *
     * @param method The test method that has just finished running.
     */
    void after(Method method);

    /**
     * This method is run before each test.  This is a good place to perform initialization of the test class,
     * such as dependency injection using RoboGuice.
     *
     * @param test An instance of the test class.
     */
    void prepareTest(Object test);
}
