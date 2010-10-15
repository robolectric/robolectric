package com.xtremelabs.robolectric.fakes;

import android.text.SpannableStringBuilder;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(SpannableStringBuilder.class)
public class FakeSpannableStringBuilder {
    private final SpannableStringBuilder real;

    private StringBuilder builder;

    public FakeSpannableStringBuilder(SpannableStringBuilder real) {
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
    public int length() {
        return builder.length();
    }

    public String toString() {
        return builder.toString();
    }
}
