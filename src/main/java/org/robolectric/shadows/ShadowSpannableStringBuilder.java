package org.robolectric.shadows;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import java.util.ArrayList;
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
    public SpannableStringBuilder replace(int start, int end, CharSequence tb) {
        return replace(start, end, tb, 0, tb.length());
    }

    @Implementation
    public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbStart, int tbEnd) {
        builder.replace(start, end, tb.subSequence(tbStart, tbEnd).toString());
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
    public SpannableStringBuilder delete(int start, int end) {
        builder.delete(start, end);
        return realSpannableStringBuilder;
    }

    @Implementation
    public void setSpan(Object what, int start, int end, int flags) {
        for (int i = 0; i < start; i++) {
            spans.add(null);
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
