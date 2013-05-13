package org.robolectric.shadows;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import java.util.HashMap;

/**
 * Shadow for {@code SQLiteOpenHelper}.  Provides basic support for retrieving
 * databases and partially implements the subclass contract.  (Currently,
 * support for {@code #onUpgrade} is missing).
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {
  @RealObject private SQLiteOpenHelper realHelper;
  private String name;
  private static HashMap<String, SQLiteDatabase> dbMap = new HashMap<String, SQLiteDatabase>();

  private SQLiteDatabase getOrCreateDb(boolean shouldCreate) {
    SQLiteDatabase db = dbMap.get(name);
    if (shouldCreate && db == null) {
      db = SQLiteDatabase.openDatabase(name, null, 0);
      dbMap.put(name, db);
      realHelper.onCreate(db);
    }
    return db;
  }

  public void __constructor__(Context context, String name, CursorFactory factory, int version) {
    this.name = name;
    this.close();
  }

  public static void reset() {
    dbMap = new HashMap<String, SQLiteDatabase>();
  }

  @Implementation
  public synchronized void close() {
    SQLiteDatabase database = getOrCreateDb(false);
    if (database != null) {
      database.close();
      dbMap.remove(name);
    }
  }

  @Implementation
  public synchronized SQLiteDatabase getReadableDatabase() {
    SQLiteDatabase database = getOrCreateDb(true);
    realHelper.onOpen(database);
    return database;
  }

  @Implementation
  public synchronized SQLiteDatabase getWritableDatabase() {
    SQLiteDatabase database = getOrCreateDb(true);
    realHelper.onOpen(database);
    return database;
  }

  @Implementation
  public String getDatabaseName() {
    return name;
  }
}
