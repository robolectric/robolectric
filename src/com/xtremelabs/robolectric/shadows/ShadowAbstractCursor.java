package com.xtremelabs.robolectric.shadows;

import android.database.AbstractCursor;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AbstractCursor.class)
public class ShadowAbstractCursor {
    @RealObject private AbstractCursor realAbstractCursor;
    
    private int currentRowNumber = 0;

    @Implementation
    public final boolean moveToFirst() {
    	currentRowNumber = 0;
        return realAbstractCursor.getCount() > 0;
    }
    
    @Implementation
    public int getPosition() {
    	return currentRowNumber;
    }
    
    @Implementation
    public final boolean moveToNext() {
    	if (currentRowNumber >= realAbstractCursor.getCount() - 1) {
    		return false;
    	}
    	currentRowNumber++;
    	return true;
    }
}
