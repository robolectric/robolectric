package org.robolectric.shadows;

import android.widget.ProgressBar;
import org.robolectric.internal.Implements;

@Implements(value = ProgressBar.class)
public class ShadowProgressBar extends ShadowView {
}
