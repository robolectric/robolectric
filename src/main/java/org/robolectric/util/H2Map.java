package com.xtremelabs.robolectric.util;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


public class H2Map implements DatabaseConfig.DatabaseMap {

    @Override
    public String getDriverClassName() {
        return "org.h2.Driver";
    }

    @Override
    public String getConnectionString() {
        return "jdbc:h2:mem:";
    }

    /**
     * Maps the SQL to the H2 Implementation
     *
     * @param sql the original SQL statement
     * @return the modified SQL statement.
     * @throws SQLException
     */
    @Override
    public String getScrubSQL(String sql) throws SQLException {

        if (sql.contains("PRIMARY KEY AUTOINCREMENT") && !sql.contains("INTEGER PRIMARY KEY AUTOINCREMENT")) {
            throw new SQLException("AUTOINCREMENT is only allowed on an INTEGER PRIMARY KEY");
        }

        // Map 'autoincrement' (sqlite) to 'auto_increment' (h2).
        String scrubbedSQL = sql.replaceAll("(?i:autoincrement)", "auto_increment");
        // Map 'integer' (sqlite) to 'bigint(19)' (h2).  	
        scrubbedSQL = scrubbedSQL.replaceAll("(?i:integer)", "bigint(19)");
        // h2 doesn't understand conflict algorithms
        scrubbedSQL = scrubbedSQL.replaceAll("INSERT OR ROLLBACK INTO", "INSERT INTO");
        scrubbedSQL = scrubbedSQL.replaceAll("INSERT OR ABORT INTO", "INSERT INTO");
        scrubbedSQL = scrubbedSQL.replaceAll("INSERT OR FAIL INTO", "INSERT INTO");
        scrubbedSQL = scrubbedSQL.replaceAll("INSERT OR IGNORE INTO", "INSERT INTO");
        scrubbedSQL = scrubbedSQL.replaceAll("INSERT OR REPLACE INTO", "INSERT INTO");
        return scrubbedSQL;
    }

    @Override
    public String getSelectLastInsertIdentity() {
        return "SELECT IDENTITY();";
    }


    public void DeregisterDriver() {

        try {
            Driver d = DriverManager.getDriver(getDriverClassName());
            DriverManager.deregisterDriver(d);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        DeregisterDriver();
    }

    @Override
    public int getResultSetType() {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

}
