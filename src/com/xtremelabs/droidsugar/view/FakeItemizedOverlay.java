package com.xtremelabs.droidsugar.view;

import com.google.android.maps.OverlayItem;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeItemizedOverlay {
    public boolean populated;
    public boolean shouldHit;

    protected final void populate() {
        populated = true;
    }

    protected boolean hitTest(OverlayItem item, android.graphics.drawable.Drawable drawable, int i, int i1) {
        return shouldHit;
    }
}
