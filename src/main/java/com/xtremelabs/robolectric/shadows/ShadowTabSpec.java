package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(TabHost.TabSpec.class)
public class ShadowTabSpec {
	 @RealObject TabSpec tabSpec;
	        private String mTag;
	        CharSequence label;
	        Drawable icon;
	        View view;
	        int viewId;
	        TabContentFactory contentFactory;
	        Intent intent;
	        
	        //  private IndicatorStrategy mIndicatorStrategy;
	        //private ContentStrategy mContentStrategy;
	        
	  	        
	     

	        /**
	         * Specify a label as the tab indicator.
	         */
	        @Implementation
	        public TabSpec setIndicator(CharSequence label) {
	           // mIndicatorStrategy = new LabelIndicatorStrategy(label);
	        	this.label = label;
	            return tabSpec;
	        }

	        /**
	         * Specify a label and icon as the tab indicator.
	         */
	        @Implementation
	        public TabSpec setIndicator(CharSequence label, Drawable icon) {
	        	this.label = label;
	        	this.icon = icon;
	            return tabSpec;
	        }

	        /**
	         * Specify a view as the tab indicator.
	         */
	        @Implementation
	        public TabSpec setIndicator(View view) {
	        	this.view = view;
	            //mIndicatorStrategy = new ViewIndicatorStrategy(view);
	            return tabSpec;
	        }

	        /**
	         * Specify the id of the view that should be used as the content
	         * of the tab.
	         */
	        @Implementation
	        public TabSpec setContent(int viewId) {
	        	this.viewId = viewId;
	            //mContentStrategy = new ViewIdContentStrategy(viewId);
	            return tabSpec;
	        }

	        /**
	         * Specify a {@link android.widget.TabHost.TabContentFactory} to use to
	         * create the content of the tab.
	         */
	        @Implementation
	        public TabSpec setContent(TabContentFactory contentFactory) {
	            //mContentStrategy = new FactoryContentStrategy(mTag, contentFactory);
	        	this.contentFactory = contentFactory;
	            return tabSpec;
	        }

	        /**
	         * Specify an intent to use to launch an activity as the tab content.
	         */
	        @Implementation
	        public TabSpec setContent(Intent intent) {
	        	this.intent = intent;
	            return tabSpec;
	        }
	        @Implementation
	        public String getTag() {
	            return mTag;
	        }
	        
	        public void setTag(String tag) {
	            mTag = tag;
	        }

  }
