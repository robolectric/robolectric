package com.xtremelabs.droidsugar.view;

import android.widget.TextView;

import java.awt.*;

@SuppressWarnings({"ALL"})
public class FakeTextView extends FakeView {
    private CharSequence text;
    Rectangle rectangle;
    public Directions compoundDrawablesWithIntrinsicBounds;
    public int textResourceId = -1;

    public FakeTextView(TextView view) {
        super(view);
    }

    public void setText(CharSequence text) {
        this.textResourceId = -1;
        this.text = text;
    }

    public void setText(int textResourceId) {
        this.textResourceId = textResourceId;
        this.text = "text from resource"; // todo: actually fetch strings
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
