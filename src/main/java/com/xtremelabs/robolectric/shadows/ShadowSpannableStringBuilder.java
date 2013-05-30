package com.xtremelabs.robolectric.shadows;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shadow of {@code SpannableStringBuilder} implemented using a regular {@code StringBuilder}
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(SpannableStringBuilder.class)
public class ShadowSpannableStringBuilder implements CharSequence {
    @RealObject private SpannableStringBuilder realSpannableStringBuilder;
    private List<Object> spans = new ArrayList<Object>();

    private StringBuilder builder = new StringBuilder();

    public void __constructor__(CharSequence text) {
        builder.append(text);
    }

    @Implementation
    public SpannableStringBuilder append(char text) {
        builder.append(text);
        return realSpannableStringBuilder;
    }

    @Implementation
    public Editable replace(int st, int en, CharSequence text) {
        builder.replace(st, en, text.toString());
        return realSpannableStringBuilder;
    }

    @Implementation
    public Editable insert(int where, CharSequence text) {
        builder.insert(where, text);
        return realSpannableStringBuilder;
    }

    @Implementation
    public SpannableStringBuilder append(CharSequence text) {
        builder.append(text);
        return realSpannableStringBuilder;
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
    
    @Implementation
    public SpannableStringBuilder delete( int start, int end ) {
    	builder.delete( start, end );
        return realSpannableStringBuilder;
    }

    @Implementation
    public void setSpan(Object what, int start, int end, int flags) {
        if (spans.size() < builder.length()) {
            spans.addAll(Arrays.asList(new Object[builder.length()]));
        }
        for (int i = start; i <= end; i++) {
            spans.add(i, what);
        }
    }

    public Object getSpanAt(int position) {
        try {
            return spans.get(position);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
