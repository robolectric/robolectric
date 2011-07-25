package com.xtremelabs.robolectric.shadows;

import android.database.AbstractCursor;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    @RealObject private AbstractCursor realAbstractCursor;

    private int currentRowNumber = 0;

    @Implementation
    public boolean moveToFirst() {
        currentRowNumber = 0;
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
    	currentRowNumber = pos;
        return true;
    }

    @Implementation
    public boolean moveToNext() {
        if (currentRowNumber >= realAbstractCursor.getCount() - 1) {
            return false;
        }
        currentRowNumber++;
        return true;
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
