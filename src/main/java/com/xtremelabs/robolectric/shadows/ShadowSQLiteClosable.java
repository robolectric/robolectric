package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteClosable;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(SQLiteClosable.class)
public abstract class ShadowSQLiteClosable {
	private int mReferenceCount = 1;
    private Object mLock = new Object();
    public abstract void onAllReferencesReleased();
    protected void onAllReferencesReleasedFromContainer(){}
    
    public void acquireReference() {
        synchronized(mLock) {
            if (mReferenceCount <= 0) {
                throw new IllegalStateException(
                        "attempt to acquire a reference on a close SQLiteClosable");
            }
            mReferenceCount++;     
        }
    }
    
    public void releaseReference() {
        synchronized(mLock) {
            mReferenceCount--;
            if (mReferenceCount == 0) {
                onAllReferencesReleased();
            }
        }
    }
    
    public void releaseReferenceFromContainer() {
        synchronized(mLock) {
            mReferenceCount--;
            if (mReferenceCount == 0) {
                onAllReferencesReleasedFromContainer();
            }
        }        
    }
}
