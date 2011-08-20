package com.xtremelabs.robolectric.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConfig {
    private static DatabaseMap dbMap = null;
    private static boolean isLoaded = false;

    public static void setDatabaseMap(DatabaseMap map) {
        dbMap = map;
        isLoaded = false; //make sure to reset isLoaded or mixing databases in a test suite will fail.
    }

    public static DatabaseMap getDatabaseMap() {
        return dbMap;
    }

    /**
     * check if the map has been loaded
     *
     * @return
     */
    public static boolean isMapLoaded() {
        return isLoaded;
    }

    /**
     * Check if the map has been set at all.
     *
     * @return
     */
    public static boolean isMapNull() {
        return dbMap == null;
    }

    /**
     * Sets what database will be used and loads the database driver, based on what DBmap is provided.
     */
    private static void LoadSQLiteDriver() {
        if (isMapNull()) throw new NullDatabaseMapException("Error in DatabaseConfig: DatabaseMap has not been set.");
        try {
            Class.forName(dbMap.getDriverClassName()).newInstance();
        } catch (InstantiationException e) {
            throw new CannotLoadDatabaseMapDriverException("Error in DatabaseConfig: SQLite driver could not be instantiated;", e);
        } catch (IllegalAccessException e) {
            throw new CannotLoadDatabaseMapDriverException("Error in DatabaseConfig: SQLite driver could not be accessed;", e);
        } catch (ClassNotFoundException e) {
            throw new CannotLoadDatabaseMapDriverException("Error in DatabaseConfig: SQLite driver class could not be found;", e);
        }
        isLoaded = true;
    }

    /**
     * Gets an in memory DB connection.  Will load DB Driver if not already loaded.
     *
     * @return Connection to In Memory Database.
     */
    public static Connection getMemoryConnection() {
        if (!isMapLoaded()) LoadSQLiteDriver();
        try {
            return DriverManager.getConnection(dbMap.getConnectionString());
        } catch (SQLException e) {
            throw new CannotLoadDatabaseMapDriverException("Error in DatabaseConfig, could not retrieve connection to in memory database.", e);
        }
    }

    /**
     * Makes any edits necessary in the SQL string for it to be compatible with the database in use.
     *
     * @return
     * @throws SQLException
     */
    public static String getScrubSQL(String sql) throws SQLException {
        if (isMapNull()) throw new NullDatabaseMapException("No database map set!");
        return dbMap.getScrubSQL(sql);
    }

    public static String getSelectLastInsertIdentity() {
        if (isMapNull()) throw new NullDatabaseMapException("No database map set!");
        return dbMap.getSelectLastInsertIdentity();
    }

    public static int getResultSetType() {
        if (isMapNull()) throw new NullDatabaseMapException("No database map set!");
        return dbMap.getResultSetType();
    }

    public interface DatabaseMap {
        String getDriverClassName();

        String getConnectionString();

        String getScrubSQL(String sql) throws SQLException;

        String getSelectLastInsertIdentity();

        int getResultSetType();
    }

    public static class NullDatabaseMapException extends RuntimeException {
        private static final long serialVersionUID = -4580960157495617424L;

        public NullDatabaseMapException(String message) {
            super(message);
        }
    }

    public static class CannotLoadDatabaseMapDriverException extends RuntimeException {
        private static final long serialVersionUID = 2614876121296128364L;

        public CannotLoadDatabaseMapDriverException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface UsingDatabaseMap {
        /**
         * @return the classes to be run
         */
        public Class<? extends DatabaseMap> value();
    }
}
