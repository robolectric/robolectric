package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.H2Map;

import static org.fest.assertions.api.Assertions.assertThat;

@DatabaseConfig.UsingDatabaseMap(H2Map.class)
@RunWith(TestRunners.WithDefaults.class)
public class H2DatabaseTest extends DatabaseTestBase {
  @Test
  public void shouldUseH2DatabaseMap() throws Exception {
    assertThat(DatabaseConfig.getDatabaseMap().getClass().getName()).isEqualTo(H2Map.class.getName());
  }
}
