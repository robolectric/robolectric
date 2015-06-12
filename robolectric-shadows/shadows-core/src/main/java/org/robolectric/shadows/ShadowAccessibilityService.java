package org.robolectric.shadows;

import android.accessibilityservice.AccessibilityService;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowService;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow of AccessibilityService that saves global actions to a list.
 */
@Implements(AccessibilityService.class)
public class ShadowAccessibilityService extends ShadowService {

  private final List<Integer> mGlobalActionsPerformed = new ArrayList<>();

    @Implementation
    public final boolean performGlobalAction(int action) {
        mGlobalActionsPerformed.add(action);
        return true;
    }

    public List<Integer> getGlobalActionsPerformed() {
        return mGlobalActionsPerformed;
    }
}

