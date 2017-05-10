package org.robolectric.shadows;

import android.widget.RelativeLayout;
import org.robolectric.annotation.Implements;

@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {
}
