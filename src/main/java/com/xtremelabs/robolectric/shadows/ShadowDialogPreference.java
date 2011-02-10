package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(DialogPreference.class)
public class ShadowDialogPreference {
	
	private static final String androidns="http://schemas.android.com/apk/res/android";

	private Context context;
	private AttributeSet attrs;
	private int defStyle;	

	public void __constructor__(Context context, AttributeSet attrs, int defStyle) {
		this.context = context;
		this.attrs = attrs;
		this.defStyle = defStyle;
	}
	   
	public void __constructor__(Context context, AttributeSet attrs) {
		this.context = context;
		this.attrs = attrs;
	}

	// TOOD remove when ShadowPreference implements getContext()
    public Context getContext() {
    	return context;
    }
    
    public AttributeSet getAttrs() {
    	return attrs;
    }
    
    public int getDefStyle() {
    	return defStyle;
    }
    
    @Implementation
    public CharSequence getDialogMessage() {
    	return (CharSequence) attrs.getAttributeValue(androidns,"dialogMessage");
    }
}
