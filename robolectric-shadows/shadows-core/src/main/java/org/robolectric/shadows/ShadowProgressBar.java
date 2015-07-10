package org.robolectric.shadows;

import android.widget.ProgressBar;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.ProgressBar}.
 */
@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {
}
