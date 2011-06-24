package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteProgram;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(SQLiteProgram.class)
public abstract class ShadowSQLiteProgram extends ShadowSQLiteClosable {
	@RealObject	SQLiteProgram realSQLiteProgram;
	protected SQLiteDatabase mDatabase;

	public void init(SQLiteDatabase db, String sql) {
	    mDatabase = db;
       // db.acquireReference();
      //  db.addSQLiteClosable(this);
     //   this.nHandle = db.mNativeHandle;
        compile(sql, false);
	}
	
	@Implementation
	public void compile(String sql, boolean forceCompilation) {
        // Only compile if we don't have a valid statement already or the caller has
        // explicitly requested a recompile. 
       // if (nStatement == 0 || forceCompilation) {
       //   Robolectric.shadowOf(mDatabase).lock();
           // try {
                // Note that the native_compile() takes care of destroying any previously
                // existing programs before it compiles.
            //    acquireReference();                
            //    native_compile(sql);
           // } finally {
           //     releaseReference();
          //      Robolectric.shadowOf(mDatabase).unlock();
         //   }        
      //  }
    } 
	

    /**
     * Bind a NULL value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind null to
     */
	@Implementation
    public void bindNull(int index) {
        acquireReference();
        try {
         //   native_bind_null(index);
        } finally {
            releaseReference();
        }
    }

    /**
     * Bind a long value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindLong(int index, long value) {
        acquireReference();
        try {
          //  native_bind_long(index, value);
        } finally {
            releaseReference();
        }
    }

    /**
     * Bind a double value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindDouble(int index, double value) {
        acquireReference();
        try {
        //    native_bind_double(index, value);
        } finally {
            releaseReference();
        }
    }

    /**
     * Bind a String value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindString(int index, String value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        acquireReference();
        try {
        //    native_bind_string(index, value);
        } finally {
            releaseReference();
        }
    }

    /**
     * Bind a byte array value to this statement. The value remains bound until
     * {@link #clearBindings} is called.
     *
     * @param index The 1-based index to the parameter to bind
     * @param value The value to bind
     */
    @Implementation
    public void bindBlob(int index, byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        acquireReference();
        try {
         //   native_bind_blob(index, value);
        } finally {
            releaseReference();
        }
    }

    /**
     * Clears all existing bindings. Unset bindings are treated as NULL.
     */
    @Implementation
    public void clearBindings() {
        acquireReference();
        try {
        //    native_clear_bindings();
        } finally {
            releaseReference();
        }
    }
    
    @Implementation
    @Override
    public void onAllReferencesReleased() {
        // Note that native_finalize() checks to make sure that nStatement is
        // non-null before destroying it.
       // native_finalize();
        mDatabase.releaseReference();
      Robolectric.shadowOf(mDatabase).removeSQLiteClosable(realSQLiteProgram);
    }

}
