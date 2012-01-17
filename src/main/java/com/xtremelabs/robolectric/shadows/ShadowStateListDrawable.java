package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.StateListDrawable;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(StateListDrawable.class)
public class ShadowStateListDrawable extends ShadowDrawable {
	
	private Map<Integer, Integer> stateToResource;
	
	public void __constructor__() {
		stateToResource = new HashMap<Integer, Integer>();
	}
	
	public void addState( int stateId, int resId ) {
		stateToResource.put( stateId, resId );
	}	
	
	public int getResourceIdForState( int stateId ) {
		return stateToResource.get( stateId );
	}
}
