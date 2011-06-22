package com.xtremelabs.robolectric.shadows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(TabHost.class)
public class ShadowTabHost extends ShadowFrameLayout {
	@RealObject
	TabHost tabHost;
	int mCurrentTab;
	List<TabSpec> tabs;

	// AttributeSet attrs;
	// public void __constructor__(Context context) {
	// __constructor__(context,null);
	// }
	//
	// public void __constructor__(Context context, AttributeSet attrs) {
	// super.__constructor__(context, attrs);
	// }

	@Implementation
	public int getCurrentTab() {
		return mCurrentTab;
	}

	@Implementation
	public void setCurrentTab(int index) {
		mCurrentTab = index;
	}

	@Implementation
	public void addTab(TabSpec tabSpec) {
		if (tabs == null)
			tabs = new ArrayList<TabSpec>();
		tabs.add(tabSpec);
	}

	public List<TabSpec> Tabs() {
		return tabs;
	}

	@Implementation
	public TabSpec newTabSpec(String tag) {
		TabSpec spec = Robolectric.newInstanceOf(TabSpec.class);
		Robolectric.shadowOf(spec).setTag(tag);
		return spec;
	}

}
