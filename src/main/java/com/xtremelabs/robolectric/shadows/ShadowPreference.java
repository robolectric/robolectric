package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Preference.class)
public class ShadowPreference {

	protected Context context;
	protected AttributeSet attrs;
	protected int defStyle;	
	protected boolean shouldPersist = true;
	protected int persistedInt;

	public void __constructor__(Context context, AttributeSet attrs, int defStyle) {
		this.context = context;
		this.attrs = attrs;
		this.defStyle = defStyle;
	}
	   
	public void __constructor__(Context context, AttributeSet attrs) {
		this.context = context;
		this.attrs = attrs;
	}

	@Implementation
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
	public boolean shouldPersist() {
		return shouldPersist;
	}
	
	public void setShouldPersist(boolean shouldPersist) {
		this.shouldPersist = shouldPersist;
	}
	
	@Implementation
	public int getPersistedInt(int defaultReturnValue) {
		return shouldPersist ? persistedInt : defaultReturnValue;
	}
	
	public void setPersistedInt(int persistedInt) {
		this.persistedInt = persistedInt;
	}
}
