package org.robolectric.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import org.robolectric.R;

public class CustomView extends LinearLayout {
    public int attributeResourceValue;
    public int namespacedResourceValue;
    public static final String fakeNS = "http://example.com/fakens";

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.inner_merge, this);
        attributeResourceValue = attrs.getAttributeResourceValue(R.class.getPackage().getName(), "message", -1);
        namespacedResourceValue = attrs.getAttributeResourceValue(fakeNS, "message", -1);
    }
}
