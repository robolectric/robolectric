package org.robolectric.shadows;

import android.text.SpannableString;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@Implements(SpannableString.class)
public class ShadowSpannableString extends ShadowSpannableStringInternal {
    @Implementation
    public void setSpan(Object what, int start, int end, int flags) {
        spans.put(what, new SpanHolder(start, end, flags));
    }

    @Implementation
    public void removeSpan(Object what) {
        spans.remove(what);
    }
}
