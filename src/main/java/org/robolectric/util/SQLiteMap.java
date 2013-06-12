package org.robolectric.util;

import java.io.File;
import java.sql.ResultSet;

public class SQLiteMap implements DatabaseConfig.DatabaseMap {

  public String getDriverClassName() {
    return "org.sqlite.JDBC";
  }

  public String getConnectionString(File file) {
    return "jdbc:sqlite:"+file.getAbsolutePath();
  }

  public String getMemoryConnectionString() {
    return "jdbc:sqlite::memory:";
  }

  public String getSelectLastInsertIdentity() {
    return "SELECT last_insert_rowid() AS id";
  }

  public int getResultSetType() {
    return ResultSet.TYPE_FORWARD_ONLY;
  }
}
