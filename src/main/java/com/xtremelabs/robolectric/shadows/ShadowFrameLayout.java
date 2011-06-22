package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TabHost;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(FrameLayout.class)
public class ShadowFrameLayout extends ShadowViewGroup {
	@RealObject FrameLayout frameLayout;
}
