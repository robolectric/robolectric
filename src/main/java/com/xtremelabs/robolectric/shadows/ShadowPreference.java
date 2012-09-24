package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(Preference.class)
public class ShadowPreference {

    @RealObject private Preference realPreference;

    protected Context context;
	protected AttributeSet attrs;
	protected int defStyle;	

	protected String key;
	protected CharSequence title;
	protected CharSequence summary;
	protected Object defaultValue;
	protected int order;
	protected boolean enabled = true;
	protected String dependencyKey;
	protected boolean persistent = false;
	protected int persistedInt;
	protected Object callChangeListenerValue = null;
	
	protected Preference.OnPreferenceClickListener  onClickListener;
	protected Preference.OnPreferenceChangeListener onPreferenceChangeListener;
	private Intent intent;

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
		return persistent;
	}

	@Implementation
	public boolean isPersistent() {
		return persistent;
	}

	@Implementation
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}
	
	@Implementation
	public int getPersistedInt(int defaultReturnValue) {
		return persistent ? persistedInt : defaultReturnValue;
	}
	
	@Implementation
	public boolean persistInt(int value) {
		this.persistedInt = value;
		return persistent;
	}
	
	@Implementation
	public boolean callChangeListener(Object newValue) {
		callChangeListenerValue = newValue;
		return (onPreferenceChangeListener == null) ? true : onPreferenceChangeListener.onPreferenceChange(realPreference, newValue);
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
	
	@Implementation
	public int getOrder() {
		return order;
	}
	
	@Implementation
	public void setOrder(int order) {
		this.order = order;
	}
	
	@Implementation
	public void setOnPreferenceClickListener( Preference.OnPreferenceClickListener onPreferenceClickListener ) {
		this.onClickListener = onPreferenceClickListener;
	}
	
	@Implementation
	public Preference.OnPreferenceClickListener getOnPreferenceClickListener() {
		return onClickListener;
	}

	@Implementation
	public void setOnPreferenceChangeListener( Preference.OnPreferenceChangeListener onPreferenceChangeListener ) {
		this.onPreferenceChangeListener = onPreferenceChangeListener;
	}

	@Implementation
	public Preference.OnPreferenceChangeListener getOnPreferenceChangeListener() {
		return onPreferenceChangeListener;
	}

	public boolean click() {
		return onClickListener.onPreferenceClick(realPreference);
	}

	@Implementation
	public void setIntent(Intent i) {
		this.intent = i;
	}
	
	@Implementation
	public Intent getIntent() {
		return this.intent;

	}
	
	@Implementation
	public void setDependency(String dependencyKey) {
		this.dependencyKey = dependencyKey;
	}
	
	@Implementation
	public String getDependency() {
		return this.dependencyKey;
	}
}
