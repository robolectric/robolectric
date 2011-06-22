package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ActivityGroup.class)
public class ShadowActivityGroup extends ShadowActivity {
    private Activity currentActivity;

    @Implementation
    public android.app.Activity getCurrentActivity() {
        return currentActivity;
    }

    /**
     * Non-Android accessor Sets the current {@code Activity} for this {@code ActivityGroup}
     *
     * @param activity
     */
    public void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }
}
