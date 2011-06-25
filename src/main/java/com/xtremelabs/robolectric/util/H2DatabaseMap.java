package com.xtremelabs.robolectric.util;

public class H2DatabaseMap implements DBConfig.DatabaseMap {

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
	 * @param sql the original SQL statement
	 * @return the modified SQL statement.
	 */
	@Override
	public String ScrubSQL(String sql) {
		// Map 'autoincrement' (sqlite) to 'auto_increment' (h2).
        String scrubbedSQL = sql.replaceAll("(?i:autoincrement)", "auto_increment");	  	
        // Map 'integer' (sqlite) to 'bigint(19)' (h2).  	
        scrubbedSQL = scrubbedSQL.replaceAll("(?i:integer)", "bigint(19)");
        return scrubbedSQL;
	}

}
