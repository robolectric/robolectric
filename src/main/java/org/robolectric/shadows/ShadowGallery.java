package com.xtremelabs.robolectric.shadows;

import android.view.KeyEvent;
import android.widget.Gallery;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(Gallery.class)
public class ShadowGallery extends ShadowAbsSpinner {

    @RealObject Gallery gallery;

    @Implementation
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (onKeyListener != null) {
                    onKeyListener.onKey(gallery, keyCode, event);
                }
                return true;
        }
        return false;
    }

}
