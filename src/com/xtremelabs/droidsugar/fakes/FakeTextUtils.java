package com.xtremelabs.droidsugar.fakes;

import android.text.TextUtils;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TextUtils.class)
public class FakeTextUtils {
    public static CharSequence expandTemplate(CharSequence template,
                                              CharSequence... values) {
        String s = template.toString();
        for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
            CharSequence value = values[i];
            s = s.replace("^" + (i + 1), value);
        }
        return s;
    }
}
