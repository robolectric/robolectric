package com.xtremelabs.droidsugar.view;

import android.widget.TextView;

@SuppressWarnings({"ALL"})
public class FakeTextView extends FakeView {
    private CharSequence text;

    public FakeTextView(TextView view) {
        super(view);
    }

    public final void setText(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }
}
