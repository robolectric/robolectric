package org.robolectric.shadows;

import android.widget.RatingBar;
import org.robolectric.internal.Implements;

@Implements(value = RatingBar.class)
public class ShadowRatingBar extends ShadowAbsSeekBar {
}
