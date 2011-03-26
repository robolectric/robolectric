package com.xtremelabs.robolectric.shadows;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;


@Implements(CursorAdapter.class)
public class ShadowCursorAdapter {

	private Cursor cursor;
	private List<View> views;
	
    public void __constructor__( Context ctx, Cursor curs, boolean autoRequery ) {
    	cursor = curs;
    }
    
    @Implementation
	public void changeCursor( Cursor curs ) {
    	if( cursor != null && !cursor.isClosed() ) {
    		cursor.close();
    	}
    	cursor = curs;
    }
    
    @Implementation
    public Cursor getCursor() {
    	return cursor;
    }
    
    @Implementation
    public int getCount() {
    	if ( cursor == null ) {
    		return 0;
    	}
    	return cursor.getCount();
    }
    
    @Implementation
    public Object getItem(int position) {
    	if ( cursor == null ) {
    		return null;
    	}
    	
    	cursor.moveToPosition(position);
    	return new Integer(position);
    }
    
    @Implementation
    public long getItemId(int position) {
    	if ( cursor == null ) {
    		return 0;
    	}

    	cursor.moveToPosition(position);
    	int rowIdColumn = cursor.getColumnIndexOrThrow("_id");
    	return cursor.getLong(rowIdColumn);
    }
    
    @Implementation
    public View getView(int position, View convertView, ViewGroup parent) {
    	if ( cursor == null ) {
    		return null;
    	}
    	
    	if ( convertView != null ) {
    		return convertView;
    	}
    	
    	return views.get(position);
    }
    
    /**
     * Non-Android API.  Set a list of views to be returned for successive
     * calls to getView().
     * 
     * @param views
     */
    public void setViews( List<View> views ) {
    	this.views = views;
    }
}
