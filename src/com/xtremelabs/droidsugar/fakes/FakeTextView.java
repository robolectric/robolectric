package com.xtremelabs.droidsugar.fakes;

import android.graphics.drawable.Drawable;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.widget.TextView;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class FakeTextView extends FakeView {
    private CharSequence text = "";
    public CompoundDrawables compoundDrawablesWithIntrinsicBounds;
    public int textResourceId = UNINITIALIZED_ATTRIBUTE;
    public int textColorHexValue = UNINITIALIZED_ATTRIBUTE;
    public int textSize = UNINITIALIZED_ATTRIBUTE;
    public boolean autoLinkPhoneNumbers;
    private int autoLinkMask;
    private CharSequence hintText;

    public FakeTextView(TextView view) {
        super(view);
    }

    public void setText(CharSequence text) {
        this.textResourceId = UNINITIALIZED_ATTRIBUTE;
        this.text = text;
    }

    public void setText(int textResourceId) {
        this.textResourceId = textResourceId;
        this.text = getResources().getText(textResourceId);
    }

    public CharSequence getText() {
        return text;
    }

    public int length() {
        return text.length();
    }

    public void setTextColor(int color) {
        textColorHexValue = color;
    }

    public void setTextSize(float size) {
        textSize = (int) size;
    }

    public final void setHint(int resId) {
        this.hintText = getResources().getText(resId);
    }

    public CharSequence getHint() {
        return hintText;
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

    public final void setAutoLinkMask(int mask) {
        autoLinkMask = mask;

        autoLinkPhoneNumbers = (mask & Linkify.PHONE_NUMBERS) != 0;
    }

    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        compoundDrawablesWithIntrinsicBounds = new CompoundDrawables(left, top , right, bottom);
    }

    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top,
                                                        Drawable right, Drawable bottom) {
        compoundDrawablesWithIntrinsicBounds = new CompoundDrawables(left, top , right, bottom);
    }

    public Drawable[] getCompoundDrawables() {
        if (compoundDrawablesWithIntrinsicBounds == null) {
            return new Drawable[]{null, null, null, null};
        }
        return new Drawable[]{
                compoundDrawablesWithIntrinsicBounds.leftDrawable,
                compoundDrawablesWithIntrinsicBounds.topDrawable,
                compoundDrawablesWithIntrinsicBounds.rightDrawable,
                compoundDrawablesWithIntrinsicBounds.bottomDrawable
        };
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (onKeyListener != null) {
            return onKeyListener.onKey(realView, keyCode, event);
        } else {
            return false;
        }
    }

    @Override
    public String innerText() {
        return text == null ? "" : text.toString();
    }

    public static class CompoundDrawables {
        public int left;
        public int top;
        public int right;
        public int bottom;
        
        public Drawable leftDrawable;
        public Drawable topDrawable;
        public Drawable rightDrawable;
        public Drawable bottomDrawable;

        public CompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
            leftDrawable = left;
            topDrawable = top;
            rightDrawable = right;
            bottomDrawable = bottom;
        }

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
