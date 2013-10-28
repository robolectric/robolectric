package org.robolectric.shadows;

import android.database.sqlite.SQLiteCursor;
import org.robolectric.annotation.Implements;

import java.sql.ResultSet;

/**
 * Simulates an Android Cursor object.
 */
@Implements(value = SQLiteCursor.class, inheritImplementationMethods = true)
public class ShadowSQLiteCursor {

  public void setResultSet(ResultSet result, String sql) {
//    this.resultSet = result;
//    rowCount = 0;
//
//    //Cache all rows.  Caching rows should be thought of as a simple replacement for ShadowCursorWindow
//    if (resultSet != null) {
//      cacheColumnNames(resultSet);
//      try {
//        fillRows(sql, result.getStatement().getConnection());
//      } catch (SQLException e) {
//        throw new RuntimeException("SQL exception in setResultSet", e);
//      }
//    }
  }

}
