package org.robolectric.util;

import org.robolectric.util.DatabaseConfig.DatabaseMap;

import java.io.File;
import java.sql.ResultSet;

public class NullDatabaseMap implements DatabaseMap {

  @Override
  public String getDriverClassName() {
    return NullDatabaseMap.class.getName();
  }

  @Override
  public String getMemoryConnectionString() {
    return null;
  }

  @Override
  public String getConnectionString(File file) {
    return null;
  }

  @Override
  public String getSelectLastInsertIdentity() {
    return null;
  }

  @Override
  public int getResultSetType() {
    return ResultSet.TYPE_FORWARD_ONLY;
  }
}
