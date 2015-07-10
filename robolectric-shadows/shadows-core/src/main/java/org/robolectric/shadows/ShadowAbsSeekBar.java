package org.robolectric.shadows;

import android.widget.AbsSeekBar;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.widget.AbsSeekBar}.
 */
@Implements(AbsSeekBar.class)
public class ShadowAbsSeekBar extends ShadowProgressBar {
}
