package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class DatabaseUtilsTest {

  @Test
  public void testQuote() {
    assertThat(ShadowDatabaseUtils.sqlEscapeString("foobar")).isEqualTo("'foobar'");
    assertThat(ShadowDatabaseUtils.sqlEscapeString("Rich's")).isEqualTo("'Rich''s'");
  }
}
