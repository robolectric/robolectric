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
  private Context context;
  private SQLiteDatabase database;
  private CursorFactory factory;
  private int version;

  public void __constructor__(Context context, String name, CursorFactory factory, int version) {
    this.context = context;
    this.name = name;
    this.factory = factory;
    this.version = version;
  }

  @Implementation
  public synchronized void close() {
    if (database != null) {
      database.close();
    }
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

  private SQLiteDatabase getDatabase() {
    if (name == null) {
      database = SQLiteDatabase.create(factory);
    } else {
      database = SQLiteDatabase.openDatabase(context.getDatabasePath(name).getPath(), factory, SQLiteDatabase.OPEN_READWRITE);
    }

    if (database.getVersion() == 0) {
      realHelper.onCreate(database);
    }
    database.setVersion(version);
    return database;
  }
}
