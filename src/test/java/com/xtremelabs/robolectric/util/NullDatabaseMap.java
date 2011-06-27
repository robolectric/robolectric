package com.xtremelabs.robolectric.util;

import java.sql.SQLException;

import com.xtremelabs.robolectric.util.DatabaseConfig.DatabaseMap;

public class NullDatabaseMap implements DatabaseMap {
		
	@Override
	public String getDriverClassName() {
		return "com.xtremelabs.robolectric.util.NullDatabaseMap";
	}

	@Override
	public String getConnectionString() {
		return null;
	}

	@Override
	public String ScrubSQL(String sql) throws SQLException {
		return null;
	}

	@Override
	public String SelectLastInsertIdentity() {
		return null;
	}
	
}

