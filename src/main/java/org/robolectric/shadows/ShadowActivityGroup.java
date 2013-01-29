package org.robolectric.shadows;

import android.app.Activity;
import android.app.ActivityGroup;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

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
