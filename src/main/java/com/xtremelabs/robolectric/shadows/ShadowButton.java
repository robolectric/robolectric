package com.xtremelabs.robolectric.shadows;

public class ShadowButton extends ShadowTextView {

    @Override
    public void applyAttributes() {
        super.applyAttributes();
        if (getBackground() == null) {
            setBackgroundColor(android.R.color.transparent);
        }
    }
}
