package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabHost.TabSpec.class)
public class ShadowTabSpec {

    @RealObject
    TabHost.TabSpec realObject;
    private String tag;
    private View indicatorView;
    private Intent intent;
	private int viewId;
	private View contentView;
	private CharSequence label;
	private Drawable icon;

    /**
     * Non-Android accessor, sets the tag on the TabSpec
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Implementation
    public java.lang.String getTag() {
        return tag;
    }

    /**
     * Non-Android accessor
     *
     * @return the view object set in a call to {@code TabSpec#setIndicator(View)}
     */
    public View getIndicatorAsView() {
        return this.indicatorView;
    }
    
    public String getIndicatorLabel() {
        return this.label.toString();
    }
    
    public Drawable getIndicatorIcon() {
        return this.icon;
    }
    
	/**
	 * Same as GetIndicatorLabel()
	 * @return
	 */
	public String getText() {
		return label.toString();
	}
    @Implementation
    public TabHost.TabSpec setIndicator(View view) {
        this.indicatorView = view;
        return realObject;
    }
    
    @Implementation
    public TabHost.TabSpec setIndicator(CharSequence label) {
    	this.label = label;
        return realObject;
    }

    @Implementation
    public TabHost.TabSpec setIndicator(CharSequence label, Drawable icon) {
    	this.label = label;
    	this.icon = icon;
        return realObject;
    }

    /**
     * Non-Android accessor
     *
     * @return the intent object set in a call to {@code TabSpec#setContent(Intent)}
     */
    public Intent getContentAsIntent() {
        return intent;
    }

    @Implementation
    public android.widget.TabHost.TabSpec setContent(Intent intent) {
        this.intent = intent;
        return realObject;
    }
    
    @Implementation
    public android.widget.TabHost.TabSpec setContent(TabContentFactory factory) {
    	contentView = factory.createTabContent(this.tag);
        return realObject;
    }
    
    
    @Implementation
    public android.widget.TabHost.TabSpec setContent(int viewId) {
    	this.viewId = viewId;
        return realObject;
    }
    
    public int getContentViewId() {
    	return viewId;
    }
    
    public View getContentView() {
    	return contentView;
    }
    
}
