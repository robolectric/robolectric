package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.content.res.Configuration;

@Implements(Configuration.class)
public class ShadowConfiguration {
	
	  @RealObject
	  private Configuration realConfiguration;
	  
	  @Implementation
	  public void setToDefaults() {
		  realConfiguration.screenLayout = Configuration.SCREENLAYOUT_LONG_NO | 
		  								   Configuration.SCREENLAYOUT_SIZE_NORMAL;
	  }
}
