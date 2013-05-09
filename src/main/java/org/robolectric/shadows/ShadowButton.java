package org.robolectric.shadows;

import android.widget.Button;
import org.robolectric.annotation.Implements;

@Implements(value = Button.class)
public class ShadowButton extends ShadowTextView {

}
