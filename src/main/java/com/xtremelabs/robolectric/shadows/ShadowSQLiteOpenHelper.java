package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadow for {@code SQLiteOpenHelper}.  Provides basic support for retrieving
 * databases and partially implements the subclass contract.  (Currently,
 * support for {@code #onUpgrade} is missing).
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {

	private static final Map<String, SQLiteDatabase> DATABASES = new HashMap<String, SQLiteDatabase>();
	
    @RealObject private SQLiteOpenHelper realHelper;
	private SQLiteDatabase database;
	private String name;

    public void __constructor__(Context context, String name, CursorFactory factory, int version) {
		this.name = name;
    }

    @Implementation
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        database = null;
    }
    
	private synchronized void open() {
		if (database == null) {
			database = DATABASES.get(name);
			if (database == null) {
				database = ShadowSQLiteDatabase.openDatabase(name, null, 0);
				DATABASES.put(name, database);
				realHelper.onCreate(database);
			}
		}
		shadowOf(database).reOpen();
		realHelper.onOpen(database);
	}

	@Implementation
	public synchronized SQLiteDatabase getReadableDatabase() {
		open();
		return database;
	}

	@Implementation
	public synchronized SQLiteDatabase getWritableDatabase() {
		open();
		return database;
	}

	static void resetInMemoryDatabases() {
		DATABASES.clear();
	}
}
