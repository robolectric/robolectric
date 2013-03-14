package org.robolectric.shadows;

import android.widget.RelativeLayout;
import org.robolectric.internal.Implements;

@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {
}
