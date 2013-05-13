package org.robolectric.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.sql.SQLException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.SQLite.buildInsertString;

@DatabaseConfig.UsingDatabaseMap(H2Map.class)
@RunWith(TestRunners.WithDefaults.class)
public class H2Test {
  ContentValues values;
  @Before

  public void setUp() throws Exception {
    String byteString = "byte_string";
    byte[] byteData = byteString.getBytes();

    values = new ContentValues();
    values.put("name", "Chuck");
    values.put("int_value", 33);
    values.put("float_value", (float) 1.5);
    values.put("byte_data", byteData);
  }

  @Test
  public void testBuildInsertString() throws SQLException {
    SQLite.SQLStringAndBindings insertString = buildInsertString("table_name", values, SQLiteDatabase.CONFLICT_REPLACE);
    assertThat(insertString.sql).isEqualTo("INSERT INTO table_name (float_value, byte_data, name, int_value) VALUES (?, ?, ?, ?);");
    SQLiteTestHelper.verifyColumnValues(insertString.columnValues);
  }
}
