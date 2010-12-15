package com.xtremelabs.robolectric.shadows;

import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

/**
 * This is a simple shadow for ExpandableListView. The only current purpose of
 * this shadow is allowing to test the {@link #performItemClick(View, int, long)}
 * method, for any {@link OnChildClickListener} (not for {@link OnGroupClickListener}).   
 * 
 * @author Cristian Castiblanco
 */
@Implements(ExpandableListView.class)
public class ShadowExpandableListView extends ShadowListView {
	@RealObject
	private ExpandableListView mExpandable;
	private OnChildClickListener mChildClickListener;
	
	@Implementation
    @Override
    public boolean performItemClick(View view, int position, long id) {
		if( mChildClickListener != null ){
			mChildClickListener.onChildClick(mExpandable, null, 0, position, id);
        	return true;
        }
        return false;
    }
	
	@Implementation
	public void setOnChildClickListener(OnChildClickListener clildListener){
		mChildClickListener = clildListener;
	}
}