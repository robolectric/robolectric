package org.robolectric;

import android.support.v4.app.Fragment;
import org.junit.runners.model.InitializationError;

public class RobolectricFragmentTestRunner extends RobolectricTestRunner {
    public RobolectricFragmentTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public static void startFragment(Fragment fragment) {
    }
}
