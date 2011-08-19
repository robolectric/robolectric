package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.database.AbstractCursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;


@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    @RealObject private AbstractCursor realAbstractCursor;

//    @Implementation abstract public int getCount();
//    @Implementation  abstract public String[] getColumnNames();
//    @Implementation abstract public String getString(int columnIndex);   
//    @Implementation abstract public short getShort(int columnIndex);
//    @Implementation abstract public int getInt(int columnIndex);
//    @Implementation abstract public long getLong(int columnIndex);
//    @Implementation abstract public float getFloat(int columnIndex);
//    @Implementation abstract public double getDouble(int columnIndex);
//    @Implementation abstract public boolean isNull(int columnIndex);
    
    protected Map<String, Object> currentRow;
    protected int currentRowNumber = -1;
    protected Map<String, Integer> columnNames = new HashMap<String, Integer>();
    protected String[] columnNameArray;
    protected Map<Integer, Map<String, Object>> rows = new HashMap<Integer, Map<String, Object>>();
    protected int rowCount;
    
    @Implementation
    public int getCount() {  	
        return rowCount;
    }
    
    @Implementation
    public boolean moveToFirst() {
    	setPosition(0);
        return realAbstractCursor.getCount() > 0;
    }

    @Implementation
    public int getPosition() {
        return currentRowNumber;
    }
    


    
    @Implementation
    public boolean moveToPosition( int pos ) {
        if (pos >= realAbstractCursor.getCount()) {
            return false;
        }
    	
        setPosition(pos);
        return true;
    }
   
    /**
     * Set currentRowNumber(Int) and currentRow (Map)
     * @param pos = the position to set
     */
    private void setPosition(int pos) {
    	currentRowNumber = pos;
    	if ((-1 == currentRowNumber) || (rowCount == currentRowNumber))
    		currentRow =null;
    	else
    		currentRow = rows.get(currentRowNumber);
    }
    
        
    
   

    @Implementation
    public boolean moveToNext() {
        if (currentRowNumber >= realAbstractCursor.getCount() - 1) {
            return false;
        }
        setPosition(++currentRowNumber);
        return true;
    }
    @Implementation
    public CursorWindow getWindow() {
        return null;
    }
    @Implementation
    public String[] getColumnNames() {
    	return columnNameArray;
    }
    
    @Implementation
    public int getColumnCount() {
        return getColumnNames().length;
    }

    
    @Implementation
    public boolean moveToPrevious() {
        if (currentRowNumber < 0 || realAbstractCursor.getCount() == 0) {
            return false;
        }
        currentRowNumber--;
        return true;
    }
    
    @Implementation
    public boolean isFirst() {
    	return currentRowNumber == 0;
    }
    
    @Implementation
    public boolean isLast() {
    	return currentRowNumber == realAbstractCursor.getCount() - 1;
    }
    
    @Implementation
    public boolean isBeforeFirst() {
    	return currentRowNumber < 0;
    }
    
    @Implementation
    public boolean isAfterLast() {
    	return currentRowNumber >= realAbstractCursor.getCount();
    }
}
