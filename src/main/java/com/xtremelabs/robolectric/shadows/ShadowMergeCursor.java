package com.xtremelabs.robolectric.shadows;

import android.database.Cursor;
import android.database.MergeCursor;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Implementation for Android {@code MergeCursor} class.
 */
@Implements(MergeCursor.class)
public class ShadowMergeCursor extends ShadowAbstractCursor {
	
	private Cursor [] cursors;
	private Cursor activeCursor;
	
	public void __constructor__( Cursor[] cursors ) {
		if ( cursors != null ) {
			this.cursors = cursors;
			this.activeCursor = cursors[0];
		}
		
		rowCount = getCount();
		if ( activeCursor != null ) {
			columnNameArray = activeCursor.getColumnNames();
		} else {
			columnNameArray = new String[0];
		}
	}
	
	@Implementation
	@Override
	public int getCount() {
		int count = 0;
		if ( cursors == null ) { return count; }
		
		for (int i = 0; i < cursors.length; i++) {
			Cursor c = cursors[i];
			if ( c != null ) {
				count += cursors[i].getCount();
			}
		}
		
		return count;
	}
	
	@Override
	protected void setPosition( int pos ) {
		int count = 0;
		if ( cursors == null ) { return; }
		
		currentRowNumber = pos;
		columnNameArray = new String[0];
		activeCursor = null;
		
		for ( int i = 0; i < cursors.length; i++ ) {
			Cursor c = cursors[i];
			if ( c == null ) { continue; }
			
			if ( pos < (count + c.getCount()) ) {
				c.moveToPosition( pos - count );
				columnNameArray = c.getColumnNames();
				activeCursor = c;
				break;
			}
			count += c.getCount();
		}
	}
	
	@Implementation
    public String getString(int column) {
	   return activeCursor.getString(column);
   	}
	
    @Implementation
    public long getLong(int column) {
    	return activeCursor.getLong(column);
    }
    
    @Implementation
    public short getShort(int column) {
    	return activeCursor.getShort(column);
    }
    
    @Implementation
    public int getInt(int column) {
    	return activeCursor.getInt(column);
    }
    
    @Implementation
    public float getFloat(int column) {
    	return activeCursor.getFloat(column);
    }

    @Implementation
    public double getDouble(int column) {
    	return activeCursor.getDouble(column);
    }
    
    @Implementation
    public byte[] getBlob(int column) {
    	return activeCursor.getBlob(column);
    }

    @Implementation
    public boolean isNull(int column) {
    	return activeCursor.isNull(column);
    }
    
    @Implementation
    @Override
    public void close() {
    	super.close();
    	
    	for ( int i = 0; i < cursors.length; i++ ) {
    		Cursor c = cursors[i];
    		if ( c != null ) {
    			c.close();
    		}
    	}
    }
}
