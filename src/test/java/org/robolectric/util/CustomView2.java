package com.xtremelabs.robolectric.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CustomView2 extends LinearLayout {
    public int childCountAfterInflate;

    public CustomView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        childCountAfterInflate = getChildCount();
    }
}
