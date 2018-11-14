package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.database.DatabaseUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowDatabaseUtilsTest {

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
