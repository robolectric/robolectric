package com.xtremelabs.droidsugar.fakes;

import android.text.SpannableStringBuilder;

@SuppressWarnings({"UnusedDeclaration"})
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

    public SpannableStringBuilder append(char text) {
        builder.append(text);
        return real;
    }

    public SpannableStringBuilder append(CharSequence text) {
        builder.append(text);
        return real;
    }

    public String toString() {
        return builder.toString();
    }
}
