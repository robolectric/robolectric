package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteClosable;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SQLiteClosable.class)
public abstract class ShadowSQLiteClosable {
	private int mReferenceCount = 1;
    private Object mLock = new Object();
    
    @Implementation
    public abstract void onAllReferencesReleased();
    @Implementation
    public void onAllReferencesReleasedFromContainer(){}
    
    @Implementation
    public void acquireReference() {
        synchronized(mLock) {
            if (mReferenceCount <= 0) {
                throw new IllegalStateException(
                        "attempt to acquire a reference on a close SQLiteClosable");
            }
            mReferenceCount++;     
        }
    }
    
    @Implementation
    public void releaseReference() {
        synchronized(mLock) {
            mReferenceCount--;
            if (mReferenceCount == 0) {
                onAllReferencesReleased();
            }
        }
    }
    
    @Implementation
    public void releaseReferenceFromContainer() {
        synchronized(mLock) {
            mReferenceCount--;
            if (mReferenceCount == 0) {
                onAllReferencesReleasedFromContainer();
            }
        }        
    }
}
