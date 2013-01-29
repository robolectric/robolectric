package org.robolectric.shadows;

import android.content.res.ColorStateList;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ColorStateList.class)
public class ShadowColorStateList {
    @Implementation
    public static ColorStateList valueOf(int color) {
        return new ColorStateList(null, null);
    }
}
