package org.robolectric.shadows;

import android.widget.FrameLayout;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link FrameLayout} that simulates its implementation.
 */
@SuppressWarnings("UnusedDeclaration")
@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {
}
