package org.robolectric.shadows;

import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.SQLite;

import java.sql.ResultSet;
import java.sql.SQLException;

@Implements(value = SQLiteStatement.class, inheritImplementationMethods = true)
public class ShadowSQLiteStatement extends ShadowSQLiteProgram {

//  @Implementation
//  public void execute() {
//    if (!mDatabase.isOpen()) {
//      throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
//    }
//    try {
//      actualDbStatement.execute();
//    } catch (SQLException e) {
//      throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  @Implementation
//  public long executeInsert() {
//    try {
//      actualDbStatement.executeUpdate();
//      return SQLite.fetchGeneratedKey(actualDbStatement.getGeneratedKeys());
//    } catch (SQLException e) {
//      throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  @Implementation
//  public int executeUpdateDelete() {
//    try {
//      return actualDbStatement.executeUpdate();
//    } catch (SQLException e) {
//      throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  @Implementation
//  public long simpleQueryForLong() {
//    ResultSet rs;
//    try {
//      rs = actualDbStatement.executeQuery();
//      rs.next();
//      return rs.getLong(1);
//    } catch (SQLException e) {
//       handleException(e);
//       throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  @Implementation
//  public String simpleQueryForString() {
//    ResultSet rs;
//    try {
//      rs = actualDbStatement.executeQuery();
//      rs.next();
//      return rs.getString(1);
//    } catch (SQLException e) {
//      handleException(e);
//      throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  @Implementation
//  public void close() {
//    try {
//      actualDbStatement.close();
//    } catch (SQLException e) {
//      throw new SQLiteException(e.getMessage(), e);
//    }
//  }
//
//  private void handleException(SQLException e)  {
//    if (e.getMessage().contains("No data is available")) {
//      //if the query returns zero rows
//      throw new SQLiteDoneException("No data is available");
//    } else if (e.getMessage().contains("ResultSet closed")) {
//      //if the query returns zero rows (SQLiteMap)
//      throw new SQLiteDoneException("ResultSet closed,(probably, no data available)");
//    }
//  }
}