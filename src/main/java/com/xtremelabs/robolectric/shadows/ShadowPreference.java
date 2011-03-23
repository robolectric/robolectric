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
	protected boolean shouldPersist = false;
	protected int persistedInt;
	protected Object callChangeListenerValue = null;
	protected boolean enabled = true;
	protected String key;
	protected CharSequence title;
	protected CharSequence summary;
	protected Object defaultValue;
	
	public void __constructor__(Context context) {
		__constructor__(context, null, 0);
	}

	public void __constructor__(Context context, AttributeSet attributeSet) {
		__constructor__(context, attributeSet, 0);
	}

	public void __constructor__(Context context, AttributeSet attributeSet, int defStyle) {
		this.context = context;
		this.attrs = attributeSet;
		this.defStyle = defStyle;
		
		if (attributeSet != null) {
			key = attributeSet.getAttributeValue("android", "key");
        }
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
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Implementation
	public boolean isEnabled() {
		return enabled;
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
	
	@Implementation
	public boolean persistInt(int value) {
		this.persistedInt = value;
		return shouldPersist;
	}
	
	@Implementation
	public boolean callChangeListener(Object newValue) {
		callChangeListenerValue = newValue;
		return true;
	}
	
	public Object getCallChangeListenerValue() {
		return callChangeListenerValue;
	}

	@Implementation
	public void setSummary(int summaryResId) {
		this.summary = context.getResources().getText(summaryResId);
	}

	@Implementation
	public void setSummary(CharSequence summary) {
		this.summary = summary;
	}
	
	@Implementation 
	public CharSequence getSummary() {
		return summary;
	}
	
	@Implementation
	public void setTitle(int titleResId) {
		this.title = context.getResources().getText(titleResId);
	}

	@Implementation
	public void setTitle(CharSequence title) {
		this.title = title;
	}
	
	@Implementation 
	public CharSequence getTitle() {
		return title;
	}
	
	@Implementation
	public void setKey(String key) {
		this.key = key;
	}
	
	@Implementation 
	public String getKey() {
		return key;
	}
	
	@Implementation
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
}
