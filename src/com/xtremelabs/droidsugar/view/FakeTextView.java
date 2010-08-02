package com.xtremelabs.droidsugar.view;

import java.util.ArrayList;
import java.util.List;

import android.text.style.URLSpan;
import android.widget.TextView;

import java.awt.*;

@SuppressWarnings({"ALL"})
public class FakeTextView extends FakeView {
    private CharSequence text;
    public Directions compoundDrawablesWithIntrinsicBounds;
    public int textResourceId = UNINITIALIZED_ATTRIBUTE;
    public int textColorResourceId = UNINITIALIZED_ATTRIBUTE;
    public int textSize = UNINITIALIZED_ATTRIBUTE;

    public FakeTextView(TextView view) {
        super(view);
    }

    public void setText(CharSequence text) {
        this.textResourceId = UNINITIALIZED_ATTRIBUTE;
        this.text = text;
    }

    public void setText(int textResourceId) {
        this.textResourceId = textResourceId;
        this.text = "text from resource"; // todo: actually fetch strings
    }

    public CharSequence getText() {
        return text;
    }

    public void setTextColor(int color) {
        textColorResourceId = color;
    }

    public void setTextSize(float size) {
        textSize = (int) size;
    }

    public URLSpan[] getUrls() {
        String[] words = text.toString().split("\\s+");
        List<URLSpan> urlSpans = new ArrayList<URLSpan>();
        for (String word : words) {
            if (word.startsWith("http://")) {
                urlSpans.add(new URLSpan(word));
            }
        }
        return urlSpans.toArray(new URLSpan[urlSpans.size()]);
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
