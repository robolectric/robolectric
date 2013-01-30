package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;

/**
 * Indicates that this shadow class would like to have a Resources instance injected at creation.
 */
public interface UsesResources {
    void injectResources(Resources resources);
}
