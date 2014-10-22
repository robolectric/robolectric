package org.robolectric.shadows;

import android.widget.ProgressBar;
import org.robolectric.annotation.Implements;

@Implements(ProgressBar.class)
public class ShadowProgressBar extends ShadowView {
}
