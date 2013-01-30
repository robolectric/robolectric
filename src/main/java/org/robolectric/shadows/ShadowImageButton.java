package com.xtremelabs.robolectric.shadows;

import android.widget.ImageButton;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ImageButton.class)
public class ShadowImageButton extends ShadowImageView {
    @Override
    public void applyAttributes() {
        super.applyAttributes();
        if (getBackground() == null) {
            setBackgroundColor(android.R.color.transparent);
        }
    }
}
