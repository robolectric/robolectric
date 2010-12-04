package com.xtremelabs.robolectric.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.xtremelabs.robolectric.R;

public class CustomView extends LinearLayout {
    public int attributeResourceValue;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.inner_merge, this);
        attributeResourceValue = attrs.getAttributeResourceValue("some namespace", "message", -1);
    }
}
