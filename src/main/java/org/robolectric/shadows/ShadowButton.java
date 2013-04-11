package org.robolectric.shadows;

import android.widget.Button;
import org.robolectric.internal.Implements;

@Implements(value = Button.class, inheritImplementationMethods = true)
public class ShadowButton extends ShadowTextView {

    @Override
    public void applyAttributes() {
        super.applyAttributes();
        if (getBackground() == null) {
            setBackgroundColor(android.R.color.transparent);
        }
    }
}
