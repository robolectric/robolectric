package org.robolectric.shadows;

import android.widget.ProgressBar;
import org.robolectric.annotation.Implements;

@Implements(value = ProgressBar.class)
public class ShadowProgressBar extends ShadowView {
}
