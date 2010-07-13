package com.xtremelabs.droidsugar.view;

import java.awt.*;

import android.widget.TextView;

@SuppressWarnings({"ALL"})
public class FakeTextView extends FakeView {
    private CharSequence text;
    Rectangle rectangle;
    public Directions compoundDrawablesWithIntrinsicBounds;

    public FakeTextView(TextView view) {
        super(view);
    }

    public final void setText(CharSequence text) {
        this.text = text;
    }

    public CharSequence getText() {
        return text;
    }

    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        compoundDrawablesWithIntrinsicBounds = new Directions(left, top , right, bottom);
    }

    public class Directions {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public Directions(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }
}
