package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.util.DatabaseConfig.DatabaseMap;

import java.sql.ResultSet;
import java.sql.SQLException;

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
	public String getScrubSQL(String sql) throws SQLException {
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
