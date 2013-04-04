package org.robolectric.shadows;

import android.widget.ImageButton;
import org.robolectric.internal.Implements;

@Implements(value = ImageButton.class, inheritImplementationMethods = true)
public class ShadowImageButton extends ShadowImageView {
    @Override
    public void applyAttributes() {
        super.applyAttributes();
        if (getBackground() == null) {
            setBackgroundColor(android.R.color.transparent);
        }
    }
}
