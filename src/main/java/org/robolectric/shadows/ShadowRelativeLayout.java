package org.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import org.robolectric.internal.Implements;
import org.robolectric.res.Attribute;

import java.util.ArrayList;

import static org.robolectric.Robolectric.shadowOf;

@Implements(RelativeLayout.class)
public class ShadowRelativeLayout extends ShadowViewGroup {

    public void __constructor__(Context context) {
        __constructor__(context, shadowOf(context).createAttributeSet(new ArrayList<Attribute>(), null), 0);
    }

    public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
        setLayoutParams(new ViewGroup.MarginLayoutParams(0, 0));
        super.__constructor__(context, attributeSet, defStyle);
    }
}
