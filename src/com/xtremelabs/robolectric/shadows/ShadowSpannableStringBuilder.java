package com.xtremelabs.robolectric.shadows;

import android.text.SpannableStringBuilder;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(SpannableStringBuilder.class)
public class ShadowSpannableStringBuilder implements CharSequence {
    private final SpannableStringBuilder real;

    private StringBuilder builder;

    public ShadowSpannableStringBuilder(SpannableStringBuilder real) {
        this.real = real;
        builder = new StringBuilder();
    }
    
    public void __constructor__(CharSequence text) {
        builder.append(text);
    }

    @Implementation
    public SpannableStringBuilder append(char text) {
        builder.append(text);
        return real;
    }

    @Implementation
    public SpannableStringBuilder append(CharSequence text) {
        builder.append(text);
        return real;
    }

    @Implementation
    @Override public int length() {
        return builder.length();
    }

    @Implementation
    @Override public char charAt(int index) {
        return builder.charAt(index);
    }

    @Implementation
    @Override public CharSequence subSequence(int start, int end) {
        return builder.subSequence(start, end);
    }

    @Implementation
    public String toString() {
        return builder.toString();
    }
}
