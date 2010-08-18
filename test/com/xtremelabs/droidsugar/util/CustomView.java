package com.xtremelabs.droidsugar.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CustomView extends LinearLayout {
    public int attributeResourceValue;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        attributeResourceValue = attrs.getAttributeResourceValue("some namespace", "message", -1);
    }
}
