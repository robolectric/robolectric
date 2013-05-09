package org.robolectric.shadows;

import android.widget.RatingBar;
import org.robolectric.annotation.Implements;

@Implements(value = RatingBar.class)
public class ShadowRatingBar extends ShadowAbsSeekBar {
}
