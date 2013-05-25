package org.robolectric.shadows;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow for {@code SQLiteOpenHelper}.  Provides basic support for retrieving
 * databases and partially implements the subclass contract.  (Currently,
 * support for {@code #onUpgrade} is missing).
 */
@Implements(SQLiteOpenHelper.class)
public class ShadowSQLiteOpenHelper {
  @RealObject private SQLiteOpenHelper realHelper;
  private String name;
  private SQLiteDatabase database;
  private CursorFactory factory;

  public void __constructor__(Context context, String name, CursorFactory factory, int version) {
    this.name = name;
    this.factory = factory;
  }

  @Implementation
  public synchronized void close() {
    if (database != null) {
      database.close();
    }
  }

  private SQLiteDatabase getDatabase() {
    if (database == null) {
      database = SQLiteDatabase.openDatabase(name, factory, 0);
      if (database.getVersion() == 0) {
        realHelper.onCreate(database);
      }
    }
    return database;
  }

  @Implementation
  public synchronized SQLiteDatabase getReadableDatabase() {
    return getWritableDatabase();
  }

  @Implementation
  public synchronized SQLiteDatabase getWritableDatabase() {
    SQLiteDatabase db = getDatabase();
    realHelper.onOpen(db);
    return db;
  }

  @Implementation
  public String getDatabaseName() {
    return name;
  }
}
