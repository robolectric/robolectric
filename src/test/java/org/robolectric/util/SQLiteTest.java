package org.robolectric.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.util.SQLite.*;

@RunWith(TestRunners.WithDefaults.class)
public class SQLiteTest {
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
  public void testBuildInsertString() throws Exception {
    SQLite.SQLStringAndBindings insertString = buildInsertString("table_name", values, SQLiteDatabase.CONFLICT_NONE);
    assertThat(insertString.sql).isEqualTo("INSERT INTO table_name (byte_data, float_value, int_value, name) VALUES (?, ?, ?, ?);");
    verifyColumnValues(insertString.columnValues);
  }

  @Test
  public void testBuildUpdateString() {
    SQLite.SQLStringAndBindings insertString = buildUpdateString("table_name", values, "id=?", new String[]{"1234"});
    assertThat(insertString.sql).isEqualTo("UPDATE table_name SET byte_data=?, float_value=?, int_value=?, name=? WHERE id='1234';");
    verifyColumnValues(insertString.columnValues);
  }

  @Test
  public void testBuildDeleteString() {
    String deleteString = buildDeleteString("table_name", "id=?", new String[]{"1234"});
    assertThat(deleteString).isEqualTo("DELETE FROM table_name WHERE id='1234';");
  }

  @Test
  public void testBuildWhereClause() {
    String whereClause = buildWhereClause("id=? AND name=? AND int_value=?", new String[]{"1234", "Chuck", "33"});
    assertThat(whereClause).isEqualTo("id='1234' AND name='Chuck' AND int_value='33'");
  }

  @Test
  public void testBuildColumnValuesClause() {
    SQLStringAndBindings columnValuesClause = buildColumnValuesClause(values);

    assertThat(columnValuesClause.sql).isEqualTo("(byte_data, float_value, int_value, name) VALUES (?, ?, ?, ?)");
    verifyColumnValues(columnValuesClause.columnValues);
  }

  @Test
  public void testBuildColumnAssignmentsClause() {
    SQLStringAndBindings columnAssignmentsClause = buildColumnAssignmentsClause(values);

    assertThat(columnAssignmentsClause.sql).isEqualTo("byte_data=?, float_value=?, int_value=?, name=?");
    verifyColumnValues(columnAssignmentsClause.columnValues);
  }

  void verifyColumnValues(List<Object> colValues) {
    assertThat(colValues.get(0)).isInstanceOf(byte[].class);
    assertThat(colValues.get(1)).isInstanceOf(Float.class);
    assertThat(colValues.get(2)).isInstanceOf(Integer.class);
    assertThat(colValues.get(3)).isInstanceOf(String.class);
  }
}
