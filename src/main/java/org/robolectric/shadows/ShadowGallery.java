package org.robolectric.shadows;

import android.view.KeyEvent;
import android.widget.Gallery;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(value = Gallery.class, inheritImplementationMethods = true)
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
