package com.xtremelabs.robolectric.util;

import java.sql.ResultSet;

public class SQLiteMap implements DatabaseConfig.DatabaseMap {

    public String getDriverClassName() {
        return "org.sqlite.JDBC";
    }

    public String getConnectionString() {
        return "jdbc:sqlite::memory:";
    }

    public String getScrubSQL(String sql) {
        return sql;
    }

    public String getSelectLastInsertIdentity() {
        return "SELECT last_insert_rowid() AS id";
    }

    public int getResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }
}
