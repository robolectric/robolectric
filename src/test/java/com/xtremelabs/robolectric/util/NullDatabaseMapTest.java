package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig.CannotLoadDatabaseMapDriverException;
import com.xtremelabs.robolectric.util.DatabaseConfig.UsingDatabaseMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;

/**
 * the @UsingDatabaseMap integration test
 * @author cvanvranken
 *
 */
@UsingDatabaseMap(NullDatabaseMap.class)
@RunWith(WithTestDefaultsRunner.class)
public class NullDatabaseMapTest {

	@Test
	public void CanChangeDatabaseMapUsingAnnotation() {
		Assert.assertTrue(DatabaseConfig.getDatabaseMap().getClass().getName()
				.equals(NullDatabaseMap.class.getName()));
		Assert.assertTrue(DatabaseConfig.getDatabaseMap().getConnectionString() == null);
		Assert.assertTrue(DatabaseConfig.getDatabaseMap()
				.getSelectLastInsertIdentity() == null);
		Assert.assertTrue(DatabaseConfig.getDatabaseMap().getDriverClassName() == "com.xtremelabs.robolectric.util.NullDatabaseMap");
	}

	@Test
	public void MapIsSetButIsNotLoaded() {
		Assert.assertTrue(DatabaseConfig.getDatabaseMap().getClass().getName()
				.equals(NullDatabaseMap.class.getName()));
		// check that the map has been set but not loaded.
		Assert.assertFalse(DatabaseConfig.isMapLoaded());
	}

	@Test
	public void MapLoadsButConnectionFails() {
		Assert.assertFalse(DatabaseConfig.isMapLoaded());
		Connection connection = null;
		
		boolean expectedError = false;
		
		try {
			connection = DatabaseConfig.getMemoryConnection();
			// we should never reach this,
			//since the connection should not actually be made
			Assert.assertTrue(false);
		} catch (CannotLoadDatabaseMapDriverException e) {
			//This error is expected.
			expectedError = true;
		}
		Assert.assertTrue(expectedError);
		Assert.assertTrue(connection == null);
		// driver should have loaded because the class name was valid,
		// even if the memoryConnectionString was invalid
		Assert.assertTrue(DatabaseConfig.isMapLoaded());

	}
}
