package org.robolectric.shadows;

import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.HashMap;

@Implements(value = Robolectric.Anything.class, className = "android.text.SpannableStringInternal")
public class ShadowSpannableStringInternal {
    CharSequence text = "";
    HashMap<Object, SpanHolder> spans = new HashMap<Object, SpanHolder>();

    public void __constructor__(CharSequence source) {
        text = source;
    }

    @Implementation
    public String toString() {
        return text.toString();
    }

    @Implementation
    public void setSpan(Object what, int start, int end, int flags) {
        spans.put(what, new SpanHolder(start, end, flags));
    }

    @Implementation
    public void removeSpan(Object what) {
        spans.remove(what);
    }

    @Implementation
    public <T> T[] getSpans(int queryStart, int queryEnd, Class<T> kind) {
        ArrayList<T> retVal = new ArrayList<T>();

        for (Object s : spans.keySet()) {
            if (kind.isInstance(s)) {
                SpanHolder h = spans.get(s);
                if ((h.start <= queryStart && h.end >= queryStart) ||
                        (h.start <= queryEnd && h.end >= queryEnd) ||
                        (h.start >= queryStart && h.end <= queryEnd)) {
                    retVal.add((T) s);
                }
            }
        }

        return (T[]) retVal.toArray();
    }

    class SpanHolder {
        public int start;
        public int end;
        public int flags;

        public SpanHolder(int start, int end, int flags) {
            this.start = start;
            this.end = end;
            this.flags = flags;
        }
    }

}
