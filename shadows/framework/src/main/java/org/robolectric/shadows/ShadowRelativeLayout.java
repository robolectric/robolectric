package org.robolectric.shadows;

import android.widget.RelativeLayout;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.RelativeLayout}.
 */
@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {
}
