package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.shadows.ShadowNotification.LatestEventInfo;

/**
 * Shadow for {@code SQLiteOpenHelper}.  Provides basic support for retrieving
 * databases and partially implements the subclass contract.  (Currently,
 * support for {@code #onUpgrade} is missing).
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {

    @RealObject private SQLiteOpenHelper realHelper;
    private static SQLiteDatabase database;
    
    private static Context previousContext;

    public void __constructor__(Context context, String name, CursorFactory factory, int version) {
    	if (previousContext == null) {
    		previousContext = context;
    	} else {
    		if(previousContext == context) {
    			return;
    		} else {
    			previousContext = context;
    		}
    	}
        if (database != null) {
            database.close();
        }
        database = null;
    }

    @Implementation
    public synchronized void close() {
    	if(previousContext != null)
    		return;
        if (database != null) {
            database.close();
        }
        database = null;
    }

    @Implementation
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (database == null) {
            database = SQLiteDatabase.openDatabase("path", null, 0);
            realHelper.onCreate(database);
        }

        realHelper.onOpen(database);
        return database;
    }

    @Implementation
    public synchronized SQLiteDatabase getWritableDatabase() {
        if (database == null) {
            database = SQLiteDatabase.openDatabase("path", null, 0);
            realHelper.onCreate(database);
        }

        realHelper.onOpen(database);
        return database;
    }
}
