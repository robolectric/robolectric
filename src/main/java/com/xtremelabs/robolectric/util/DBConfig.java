package com.xtremelabs.robolectric.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 
 * @author cvanvranken
 *
 */
public class DBConfig {

	//TODO: create a testrunner capable of switching the DB to native mode
	
	static DatabaseMap dbMap;
	static boolean isLoaded = false;
	/**
	 * Sets what database will be used and loads the database driver, based on what DBmap is provided.
	 */
	public static void LoadSQLiteDriver(DatabaseMap map) {
		if (isLoaded) return;
		dbMap = map;
	  try {
		//Class.forName("org.sqlite.JDBC").newInstance();
		  Class.forName(map.getDriverClassName()).newInstance();
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
	public static Connection OpenMemoryConnection() {
		if (!isLoaded) throw new RuntimeException("No database driver loaded!");
		try {
			return DriverManager.getConnection(dbMap.getConnectionString());
			//return DriverManager.getConnection("jdbc:sqlite::memory:");
		} catch (SQLException e) {
			throw new RuntimeException("Error in SQLiteConfig, could not retrieve connection to in memory database.");
		}
	}
	
	/**
	 * Makes any edits necessary in the SQL string for it to be compatible with the database in use. 
	 * @return
	 */
	public static String ScrubSQL(String sql) {
		return dbMap.ScrubSQL(sql);
	}
	
	public interface DatabaseMap {
	   String getDriverClassName();
	   String getConnectionString();
       String ScrubSQL(String sql);
	}
	
}
