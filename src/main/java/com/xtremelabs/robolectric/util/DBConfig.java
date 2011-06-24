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
	
	static boolean isLoaded = false;
	/**
	 * Sets the running mode of SQLite (pureJava or native), and then loads the driver.
	 */
	private static void LoadSQLiteDriver() {
		System.setProperty("sqlite.purejava", "false");
	  try {
		Class.forName("org.sqlite.JDBC").newInstance();
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
		if (!isLoaded) LoadSQLiteDriver();
		try {
			return DriverManager.getConnection("jdbc:sqlite::memory:");
		} catch (SQLException e) {
			throw new RuntimeException("Error in SQLiteConfig, could not retrieve connection to in memory database.");
		}
	}
}
