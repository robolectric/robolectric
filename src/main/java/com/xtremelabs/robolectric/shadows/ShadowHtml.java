package com.xtremelabs.robolectric.shadows;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Map;

@Implements(Html.class)
public class ShadowHtml {

    private static final Map<String, Spanned> EXPECTATIONS = new HashMap<String, Spanned>();

    @Implementation
    public static Spanned fromHtml(String source) {
        Spanned expected = EXPECTATIONS.get(source);
        if (expected != null){
            return expected;
        }
        return new SpannableStringBuilder(source);
    }

    public static ExpectInput expect(String s) {
        return new ExpectInput(s);
    }

    public static void clearExpectations() {
        EXPECTATIONS.clear();
    }

    public static class ExpectInput {
        private final String s;

        public ExpectInput(String s) {
            this.s = s;
        }

        public void andReturn(Spanned spanned){
            EXPECTATIONS.put(s, spanned);
        }
    }
}
