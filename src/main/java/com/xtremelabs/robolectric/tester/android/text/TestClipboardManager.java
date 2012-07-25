package com.xtremelabs.robolectric.tester.android.text;

import android.text.ClipboardManager;

public class TestClipboardManager extends ClipboardManager {

    private CharSequence text;
    
    public void setText(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public boolean hasText() {
        return text != null && text.length() > 0;
    }

}
