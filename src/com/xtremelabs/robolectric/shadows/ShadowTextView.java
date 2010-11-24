package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.widget.TextView;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextView.class)
public class ShadowTextView extends ShadowView {
    private CharSequence text = "";
    private CompoundDrawables compoundDrawablesImpl;
    private int textColorHexValue = UNINITIALIZED_ATTRIBUTE;
    private int textSize = UNINITIALIZED_ATTRIBUTE;
    private boolean autoLinkPhoneNumbers;
    private int autoLinkMask;
    private CharSequence hintText;
    private int compoundDrawablePadding;

    @Implementation
    public void setText(CharSequence text) {
        if (text == null) {
            text = "";
        }
        this.text = text;
    }

    @Implementation
    public void setText(int textResourceId) {
        this.text = getResources().getText(textResourceId);
    }

    @Implementation
    public CharSequence getText() {
        return text;
    }

    @Implementation
    public int length() {
        return text.length();
    }

    @Implementation
    public void setTextColor(int color) {
        textColorHexValue = color;
    }

    @Implementation
    public void setTextSize(float size) {
        textSize = (int) size;
    }

    @Implementation
    public final void setHint(int resId) {
        this.hintText = getResources().getText(resId);
    }

    @Implementation
    public CharSequence getHint() {
        return hintText;
    }

    @Implementation
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

    @Implementation
    public final void setAutoLinkMask(int mask) {
        autoLinkMask = mask;

        autoLinkPhoneNumbers = (mask & Linkify.PHONE_NUMBERS) != 0;
    }

    @Implementation
    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top,
                                                        Drawable right, Drawable bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        compoundDrawablesImpl = new CompoundDrawables(left, top, right, bottom);
    }

    @Implementation
    public Drawable[] getCompoundDrawables() {
        if (compoundDrawablesImpl == null) {
            return new Drawable[]{null, null, null, null};
        }
        return new Drawable[]{
                compoundDrawablesImpl.leftDrawable,
                compoundDrawablesImpl.topDrawable,
                compoundDrawablesImpl.rightDrawable,
                compoundDrawablesImpl.bottomDrawable
        };
    }

    @Implementation
    public void setCompoundDrawablePadding(int compoundDrawablePadding) {
        this.compoundDrawablePadding = compoundDrawablePadding;
    }

    @Implementation
    public int getCompoundDrawablePadding() {
        return compoundDrawablePadding;
    }

    @Implementation
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (onKeyListener != null) {
            return onKeyListener.onKey(realView, keyCode, event);
        } else {
            return false;
        }
    }

    /**
     * Returns the text string of this {@code TextView}.
     * 
     * Robolectric extension.
     */
    @Override
    public String innerText() {
        return (text == null || getVisibility() != VISIBLE) ? "" : text.toString();
    }

    @Override @Implementation
    public boolean equals(Object o) {
        return super.equals(shadowOf_(o));
    }

    @Override @Implementation
    public int hashCode() {
        return super.hashCode();
    }

    public CompoundDrawables getCompoundDrawablesImpl() {
        return compoundDrawablesImpl;
    }

    void setCompoundDrawablesImpl(CompoundDrawables compoundDrawablesImpl) {
        this.compoundDrawablesImpl = compoundDrawablesImpl;
    }

    public int getTextColorHexValue() {
        return textColorHexValue;
    }

    @Implementation
    public int getTextSize() {
        return textSize;
    }

    public boolean isAutoLinkPhoneNumbers() {
        return autoLinkPhoneNumbers;
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
