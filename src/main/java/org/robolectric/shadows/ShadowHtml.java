package org.robolectric.shadows;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Html.class)
public class ShadowHtml {

    @Implementation
    public static Spanned fromHtml(String source) {
        if (source == null) {
            /*
             * Mimic the behavior of the real fromHtml() method. It uses a
             * StringReader that throws a NullPointerException when a null
             * string is passed in.
             */
            throw new NullPointerException();
        }
        return new SpannableStringBuilder(source);
    }
}
