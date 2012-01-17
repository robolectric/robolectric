package com.xtremelabs.robolectric.shadows;

import android.text.ClipboardManager;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ClipboardManager.class)
public class ShadowClipboardManager {
    private CharSequence text;

    @Implementation
    public void setText(CharSequence text) {
        this.text = text;
    }

    @Implementation
    public CharSequence getText() {
        return text;
    }

    @Implementation
    public boolean hasText() {
        return text != null && text.length() > 0;
    }
}
