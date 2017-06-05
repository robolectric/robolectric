package org.robolectric;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import org.junit.runners.model.InitializationError;

import static org.robolectric.Robolectric.shadowOf;

public class RobolectricFragmentTestRunner extends RobolectricTestRunner {
    public RobolectricFragmentTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    public static void startFragment(Fragment fragment) {
        FragmentActivity activity = createActivity();

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(fragment, null)
                .commit();
    }

    private static FragmentActivity createActivity() {
        FragmentActivity activity = new FragmentActivity();
        initActivity(activity);
        return activity;
    }

    private static void initActivity(FragmentActivity activity) {
        shadowOf(activity).callOnCreate(null);
        shadowOf(activity).callOnStart();
        shadowOf(activity).callOnResume();
    }
}
