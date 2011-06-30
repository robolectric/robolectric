package com.xtremelabs.robolectric.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 * @author cvanvranken
 *
 */
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
	
	public static boolean isMapLoaded() {
		return isLoaded;
	}
	
	/**
	 * Sets what database will be used and loads the database driver, based on what DBmap is provided.
	 */
	private static void LoadSQLiteDriver() {
		if (dbMap==null) throw new RuntimeException("Error in SQLiteConfig: DatabaseMap has not been set.");
	  try {
		  Class.forName(dbMap.getDriverClassName()).newInstance();
	} catch (InstantiationException e) {
		throw new RuntimeException("Error in SQLiteConfig: SQLite driver could not be instantiated;");
	} catch (IllegalAccessException e) {
		throw new RuntimeException("Error in SQLiteConfig: SQLite driver could not be accessed;");
	} catch (ClassNotFoundException e) {
		throw new RuntimeException("Error in SQLiteConfig: SQLite driver class could not be found;");
	}
		isLoaded = true;
	}
	
	/**
	 * Gets an in memory DB connection.  Will load DB Driver if not already loaded.
	 * @return Connection to In Memory Database.
	 */
	public static Connection getMemoryConnection() {
		if (!isLoaded) LoadSQLiteDriver();
		try {
			return DriverManager.getConnection(dbMap.getConnectionString());
		} catch (SQLException e) {
			throw new RuntimeException("Error in SQLiteConfig, could not retrieve connection to in memory database.");
		}
	}
	
	/**
	 * Makes any edits necessary in the SQL string for it to be compatible with the database in use. 
	 * @return
	 * @throws SQLException 
	 */
	public static String getScrubSQL(String sql) throws SQLException {
		if (!isLoaded) throw new RuntimeException("No database driver loaded!");
		return dbMap.getScrubSQL(sql);
	}
	
	public static String getSelectLastInsertIdentity() {
		if (!isLoaded) throw new RuntimeException("No database driver loaded!");
		return dbMap.getSelectLastInsertIdentity();
	}
	
	public static int getResultSetType() {
		if (!isLoaded) throw new RuntimeException("No database driver loaded!");
		return dbMap.getResultSetType();
	}
	
	public interface DatabaseMap {
	   String getDriverClassName();
	   String getConnectionString();
       String getScrubSQL(String sql) throws SQLException;
       String getSelectLastInsertIdentity();
       int getResultSetType();
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
