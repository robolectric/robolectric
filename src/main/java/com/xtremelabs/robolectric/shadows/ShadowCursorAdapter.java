package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.database.Cursor;
import android.widget.CursorAdapter;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;


@Implements(CursorAdapter.class)
public class ShadowCursorAdapter {

	private Cursor cursor;
	
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
}
