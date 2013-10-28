package org.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Implements(value = SQLiteDatabase.class)
public class ShadowSQLiteDatabase {

  private static HashMap<String, SQLiteDatabase> dbMap = new HashMap<String, SQLiteDatabase>();

  @RealObject
  private SQLiteDatabase realSQLiteDatabase;

  /** Database path. */
  private String path;
  /** Open flags. */
  private int openFlags;

  /** JDBC connection. */
  private Connection connection;
  private final Object connectionLock = new Object();
  private boolean open;

  private List<String> querySql = new ArrayList<String>();

//  private boolean throwOnInsert;

  /**
   * Allows test cases access to the underlying JDBC connection, for use in
   * setup or assertions.
   *
   * @return the connection
   */
  public Connection getConnection() {
    return null;
  }

//  // TODO: think about older Android implementations
//  @Implementation
//  public Cursor rawQueryWithFactory(
//      SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs,
//      String editTable, CancellationSignal cancellationSignal) {
//    querySql.add(sql);
//    return realSQLiteDatabase.rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable, cancellationSignal);
//  }

//  @Implementation
//  public long insertWithOnConflict(String table, String nullColumnHack,
//                                   ContentValues initialValues, int conflictAlgorithm) {
//    if (throwOnInsert) {
//      throw new SQLiteException("Simulated exception");
//    }
//    return realSQLiteDatabase.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
//  }

  @Implementation
  public void onAllReferencesReleased() {
    // closing
    open = false;
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void setThrowOnInsert(boolean throwOnInsert) {
  }

  public List<String> getQuerySql() {
    return querySql;
  }

}
