package org.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import org.robolectric.annotation.Implements;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Implements(value = SQLiteDatabase.class)
public class ShadowSQLiteDatabase {

  private List<String> querySql = new ArrayList<String>();

  /**
   * Allows test cases access to the underlying JDBC connection, for use in
   * setup or assertions.
   *
   * @return the connection
   */
  public Connection getConnection() {
    return null;
  }


  public List<String> getQuerySql() {
    return querySql;
  }

}
