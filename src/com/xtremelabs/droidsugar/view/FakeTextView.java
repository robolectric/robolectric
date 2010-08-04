package com.xtremelabs.droidsugar.view;

import android.text.style.URLSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ALL"})
public class FakeTextView extends FakeView {
    private CharSequence text;
    public CompoundDrawables compoundDrawablesWithIntrinsicBounds;
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
        compoundDrawablesWithIntrinsicBounds = new CompoundDrawables(left, top , right, bottom);
    }

    public static class CompoundDrawables {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public CompoundDrawables(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompoundDrawables that = (CompoundDrawables) o;

            if (bottom != that.bottom) return false;
            if (left != that.left) return false;
            if (right != that.right) return false;
            if (top != that.top) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = left;
            result = 31 * result + top;
            result = 31 * result + right;
            result = 31 * result + bottom;
            return result;
        }

        @Override
        public String toString() {
            return "CompoundDrawables{" +
                    "left=" + left +
                    ", top=" + top +
                    ", right=" + right +
                    ", bottom=" + bottom +
                    '}';
        }
    }
}
