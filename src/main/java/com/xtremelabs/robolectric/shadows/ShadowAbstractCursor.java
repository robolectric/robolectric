package com.xtremelabs.robolectric.shadows;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentResolver;
import android.database.AbstractCursor;
import android.database.CursorWindow;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;


@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    @RealObject
    private AbstractCursor realAbstractCursor;

    protected Map<String, Object> currentRow;
    protected int currentRowNumber = -1;
    protected Map<String, Integer> columnNames = new HashMap<String, Integer>();
    protected String[] columnNameArray;
    protected Map<Integer, Map<String, Object>> rows = new HashMap<Integer, Map<String, Object>>();
    protected int rowCount;
    protected Uri notificationUri;
	protected boolean mClosed;

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
    public boolean moveToLast() {
    	if( realAbstractCursor.getCount() == 0 ) {
    		return false;
    	}
    	setPosition( realAbstractCursor.getCount() - 1 );
    	return true;
    }

    @Implementation
    public int getPosition() {
        return currentRowNumber;
    }


    @Implementation
    public boolean moveToPosition(int pos) {
        if (pos >= realAbstractCursor.getCount()) {
            return false;
        }

        setPosition(pos);
        return true;
    }

    /**
     * Set currentRowNumber(Int) and currentRow (Map)
     *
     * @param pos = the position to set
     */
    protected void setPosition(int pos) {
        currentRowNumber = pos;
        if ((-1 == currentRowNumber) || (rowCount == currentRowNumber)) {
            currentRow = null;
        } else {
            currentRow = rows.get(currentRowNumber);
        }
    }

    @Implementation
    public boolean moveToNext() {
        if (currentRowNumber + 1 >= realAbstractCursor.getCount()) {
            currentRowNumber = realAbstractCursor.getCount();
            return false;
        }
        setPosition(++currentRowNumber);
        return true;
    }

    @Implementation
    public boolean moveToPrevious() {
        if (currentRowNumber < 0 || realAbstractCursor.getCount() == 0) {
            return false;
        }
        setPosition(--currentRowNumber);
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
    public String getColumnName(int column) {
        return columnNameArray[column];
    }

    @Implementation
    public int getColumnIndex(String columnName) {
        for (int i=0; i<columnNameArray.length; i++) {
            if (columnName.equals(columnNameArray[i])) return i;
        }
        return -1;
    }

    @Implementation
    public int getColumnIndexOrThrow(String columnName) {
        int idx = getColumnIndex(columnName);
        if (idx >= 0) return idx; else throw new IllegalArgumentException("column does not exist");
    }

    @Implementation
    public int getColumnCount() {
        return getColumnNames().length;
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

    @Implementation
    public void setNotificationUri(ContentResolver cr, Uri notifyUri) {
        notificationUri = notifyUri;
    }

	@Implementation
	public boolean isClosed() {
		return mClosed;
	}

	@Implementation
	public void close() {
		mClosed = true;
	}

	/**
     * Returns the Uri set by {@code setNotificationUri()}.  Method included for testing
     * pre-API 11 projects.
     */
    public Uri getNotificationUri_Compatibility() {
        return notificationUri;
    }


}