package org.robolectric.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig.CannotLoadDatabaseMapDriverException;
import org.robolectric.util.DatabaseConfig.UsingDatabaseMap;

import java.sql.Connection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * the @UsingDatabaseMap integration test
 *
 * @author cvanvranken
 */
@UsingDatabaseMap(NullDatabaseMap.class)
@RunWith(TestRunners.WithDefaults.class)
public class NullDatabaseMapTest {

  @Test
  public void CanChangeDatabaseMapUsingAnnotation() {
    assertTrue(DatabaseConfig.getDatabaseMap().getClass().getName()
            .equals(NullDatabaseMap.class.getName()));
    assertTrue(DatabaseConfig.getDatabaseMap().getMemoryConnectionString() == null);
    assertTrue(DatabaseConfig.getDatabaseMap()
            .getSelectLastInsertIdentity() == null);
    assertThat(DatabaseConfig.getDatabaseMap().getDriverClassName()).isEqualTo(NullDatabaseMap.class.getName());
  }

  @Test
  public void MapIsSetButIsNotLoaded() {
    assertTrue(DatabaseConfig.getDatabaseMap().getClass().getName()
            .equals(NullDatabaseMap.class.getName()));
    // check that the map has been set but not loaded.
    assertFalse(DatabaseConfig.isMapLoaded());
  }

  @Test
  public void MapLoadsButConnectionFails() {
    assertFalse(DatabaseConfig.isMapLoaded());
    Connection connection = null;

    boolean expectedError = false;

    try {
      connection = DatabaseConfig.getMemoryConnection();
      // we should never reach this,
      //since the connection should not actually be made
      assertTrue(false);
    } catch (CannotLoadDatabaseMapDriverException e) {
      //This error is expected.
      expectedError = true;
    }
    assertTrue(expectedError);
    assertTrue(connection == null);
    // driver should have loaded because the class name was valid,
    // even if the memoryConnectionString was invalid
    assertTrue(DatabaseConfig.isMapLoaded());
  }
}
