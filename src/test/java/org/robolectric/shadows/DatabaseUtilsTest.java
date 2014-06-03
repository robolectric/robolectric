package org.robolectric.shadows;

import android.database.DatabaseUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class DatabaseUtilsTest {

  @Test
  public void testQuote() {
    assertThat(DatabaseUtils.sqlEscapeString("foobar")).isEqualTo("'foobar'");
    assertThat(DatabaseUtils.sqlEscapeString("Rich's")).isEqualTo("'Rich''s'");
  }

  @Test
  public void testQuoteWithBuilder() {
    StringBuilder builder = new StringBuilder();
    DatabaseUtils.appendEscapedSQLString(builder, "foobar");
    assertThat(builder.toString()).isEqualTo("'foobar'");

    builder = new StringBuilder();
    DatabaseUtils.appendEscapedSQLString(builder, "Blundell's");
    assertThat(builder.toString()).isEqualTo("'Blundell''s'");
  }
}
