package org.robolectric.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig.DatabaseMap;
import org.robolectric.util.DatabaseConfig.NullDatabaseMapException;

import java.sql.ResultSet;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class DatabaseConfigTest {

  @Before
  public void setup() {
    DatabaseConfig.setDatabaseMap(new H2Map());
  }

  @Test
  public void testSettingDatabaseMapLoadsCorrectly() throws Exception {
    assertThat(DatabaseConfig.getDatabaseMap().getClass().getName()).isEqualTo(H2Map.class.getName());
    assertThat(DatabaseConfig.isMapLoaded()).isFalse();
    DatabaseConfig.getMemoryConnection(); //load map
    assertThat(DatabaseConfig.isMapLoaded()).isTrue();
    assertThat(DatabaseConfig.getResultSetType()).isEqualTo(ResultSet.TYPE_SCROLL_INSENSITIVE);

    H2Map_TypeForwardOnly ForwardOnlyMap = new H2Map_TypeForwardOnly();
    DatabaseConfig.setDatabaseMap(ForwardOnlyMap);
    assertThat(DatabaseConfig.isMapLoaded()).isFalse();
    assertThat(DatabaseConfig.getDatabaseMap()).isEqualTo((DatabaseMap) ForwardOnlyMap);
    DatabaseConfig.getMemoryConnection(); //load map
    assertThat(DatabaseConfig.isMapLoaded()).isTrue();
    assertThat(DatabaseConfig.getDatabaseMap().getClass().getName()).isEqualTo(H2Map_TypeForwardOnly.class.getName());
    assertThat(DatabaseConfig.getResultSetType()).isEqualTo(ResultSet.TYPE_FORWARD_ONLY);
  }

  @Test
  public void testMapIsNotNull() throws Exception {
    assertThat(DatabaseConfig.isMapNull()).isFalse();
  }

  private void setDatabaseMapNull() {
    DatabaseConfig.setDatabaseMap(null);
    assertThat(DatabaseConfig.isMapNull()).isTrue();
    assertThat(DatabaseConfig.isMapLoaded()).isFalse();
  }

  @Test(expected = NullDatabaseMapException.class)
  public void testLoadingNullMapThrowsException() throws Exception {
    setDatabaseMapNull();
    DatabaseConfig.getMemoryConnection(); //attempt to load driver for map and throw exception
  }

  @Test(expected = NullDatabaseMapException.class)
  public void testGetScrubSQLThrowsExceptionWithNullMap() throws Exception {
    setDatabaseMapNull();
    DatabaseConfig.getScrubSQL("");
  }

  @Test(expected = NullDatabaseMapException.class)
  public void testGetSelectLastInsertIdentityThrowsExceptionWithNullMap() throws Exception {
    setDatabaseMapNull();
    DatabaseConfig.getSelectLastInsertIdentity();
  }

  @Test(expected = NullDatabaseMapException.class)
  public void testGetResultSetTypeThrowsExceptionWithNullMap() throws Exception {
    setDatabaseMapNull();
    DatabaseConfig.getResultSetType();
  }

  @Test
  public void testGetScrubSQL() throws Exception {
    assertThat(DatabaseConfig.getScrubSQL("autoincrement")).isEqualTo(DatabaseConfig.getDatabaseMap().getScrubSQL("autoincrement"));
  }

  @Test
  public void testGetSelectLastInsertIdentity() throws Exception {
    assertThat(DatabaseConfig.getSelectLastInsertIdentity()).isEqualTo(DatabaseConfig.getDatabaseMap().getSelectLastInsertIdentity());
  }

  @Test
  public void testGetResultSetType() throws Exception {
    assertThat(DatabaseConfig.getResultSetType()).isEqualTo(DatabaseConfig.getDatabaseMap().getResultSetType());
  }
}
