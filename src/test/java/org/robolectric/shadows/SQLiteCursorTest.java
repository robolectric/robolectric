package org.robolectric.shadows;


import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class SQLiteCursorTest {

  private Connection connection;
  private ResultSet resultSet;
  private SQLiteCursor cursor;

  @Before
  public void setUp() throws Exception {
    connection = DatabaseConfig.getMemoryConnection();

    Statement statement = connection.createStatement();
    statement.execute("CREATE TABLE table_name(" +
        "id INTEGER PRIMARY KEY, name VARCHAR(255), long_value BIGINT," +
        "float_value REAL, double_value DOUBLE, blob_value BINARY, clob_value CLOB );");

    addPeople();
    setupCursor();
  }

  @After
  public void tearDown() throws Exception {
    connection.close();
  }

  @Test
  public void testGetColumnNames() throws Exception {
    String[] columnNames = cursor.getColumnNames();

    assertColumnNames(columnNames);
  }

  @Test
  public void testGetColumnNamesEmpty() throws Exception {
    setupEmptyResult();
    String[] columnNames = cursor.getColumnNames();

    // Column names are present even with an empty result.
    assertThat(columnNames).isNotNull();
    assertColumnNames(columnNames);
  }

  @Test
  public void testGetColumnIndex() throws Exception {
    assertThat(cursor.getColumnIndex("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndex("name")).isEqualTo(1);
  }

  @Test
  public void testGetColumnIndexNotFound() throws Exception {
    assertThat(cursor.getColumnIndex("Fred")).isEqualTo(-1);
  }

  @Test
  public void testGetColumnIndexEmpty() throws Exception {
    setupEmptyResult();

    assertThat(cursor.getColumnIndex("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndex("name")).isEqualTo(1);
  }

  @Test
  public void testGetColumnIndexOrThrow() throws Exception {
    assertThat(cursor.getColumnIndexOrThrow("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndexOrThrow("name")).isEqualTo(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetColumnIndexOrThrowNotFound() throws Exception {
    cursor.getColumnIndexOrThrow("Fred");
  }

  @Test
  public void testGetColumnIndexOrThrowEmpty() throws Exception {
    setupEmptyResult();

    assertThat(cursor.getColumnIndexOrThrow("name")).isEqualTo(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetColumnIndexOrThrowNotFoundEmpty() throws Exception {
    setupEmptyResult();

    cursor.getColumnIndexOrThrow("Fred");
  }

  @Test
  public void testMoveToFirst() throws Exception {
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1234);
    assertThat(cursor.getString(1)).isEqualTo("Chuck");
  }

  @Test
  public void testMoveToFirstEmpty() throws Exception {
    setupEmptyResult();

    assertThat(cursor.moveToFirst()).isFalse();
  }

  @Test
  public void testMoveToNext() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1235);
    assertThat(cursor.getString(1)).isEqualTo("Julie");
  }

  @Test
  public void testMoveToNextPastEnd() throws Exception {
    cursor.moveToFirst();

    cursor.moveToNext();
    cursor.moveToNext();
    cursor.moveToNext();

    assertThat(cursor.moveToNext()).isFalse();
  }

  @Test
  public void testMoveBackwards() throws Exception {
    assertThat(cursor.getPosition()).isEqualTo(-1);

    cursor.moveToFirst();
    assertThat(cursor.getPosition()).isEqualTo(0);
    cursor.moveToNext();
    assertThat(cursor.getPosition()).isEqualTo(1);
    cursor.moveToNext();
    assertThat(cursor.getPosition()).isEqualTo(2);

    cursor.moveToFirst();
    assertThat(cursor.getPosition()).isEqualTo(0);
    cursor.moveToNext();
    assertThat(cursor.getPosition()).isEqualTo(1);
    cursor.moveToNext();
    assertThat(cursor.getPosition()).isEqualTo(2);

    cursor.moveToPosition(1);
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testMoveToNextEmpty() throws Exception {
    setupEmptyResult();

    cursor.moveToFirst();
    assertThat(cursor.moveToNext()).isFalse();
  }

  @Test
  public void testMoveToPrevious() throws Exception {
    cursor.moveToFirst();
    cursor.moveToNext();

    assertThat(cursor.moveToPrevious()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1234);
    assertThat(cursor.getString(1)).isEqualTo("Chuck");
  }

  @Test
  public void testMoveToPreviousPastStart() throws Exception {
    cursor.moveToFirst();

    // Possible to move cursor before the first item
    assertThat(cursor.moveToPrevious()).isTrue();
    // After that, attempts to move cursor back return false
    assertThat(cursor.moveToPrevious()).isFalse();
  }

  @Test
  public void testMoveToPreviousEmpty() throws Exception {
    setupEmptyResult();
    cursor.moveToFirst();

    assertThat(cursor.moveToPrevious()).isFalse();
  }

  @Test
  public void testGetPosition() throws Exception {
    cursor.moveToFirst();
    assertThat(cursor.getPosition()).isEqualTo(0);

    cursor.moveToNext();
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testGetBlob() throws Exception {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes();

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setObject(1, byteData);
    statement.executeUpdate();

    setupCursor();
    cursor.moveToFirst();

    byte[] retrievedByteData = cursor.getBlob(5);
    assertThat(byteData.length).isEqualTo(retrievedByteData.length);

    for (int i = 0; i < byteData.length; i++) {
      assertThat(byteData[i]).isEqualTo(retrievedByteData[i]);
    }
  }

  @Test
  public void testGetClob() throws Exception {
    String sql = "UPDATE table_name set clob_value=? where id=1234";
    String s = "Don't CLOBber my data, please. Thank you.";

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setObject(1, s);
    statement.executeUpdate();

    setupCursor();
    cursor.moveToFirst();

    String actual = cursor.getString(6);
    assertThat(s).isEqualTo(actual);
  }

  @Test
  public void testGetString() throws Exception {
    cursor.moveToFirst();

    String[] data = {"Chuck", "Julie", "Chris"};

    for (String aData : data) {
      assertThat(cursor.getString(1)).isEqualTo(aData);
      cursor.moveToNext();
    }
  }

  @Test
  public void testGetStringWhenInteger() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getString(0)).isEqualTo("1234");
  }

  @Test
  public void testGetStringWhenLong() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getString(2)).isEqualTo("3463");
  }

  @Test
  public void testGetStringWhenFloat() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getString(3)).isEqualTo("1.5");
  }

  @Test
  public void testGetStringWhenDouble() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getString(4)).isEqualTo("3.14159");
  }

  @Test(expected = SQLiteException.class)
  public void testGetStringWhenBlob() throws Exception {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes();

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setObject(1, byteData);
    statement.executeUpdate();

    setupCursor();
    cursor.moveToFirst();

    cursor.getString(5);
  }

  @Test
  public void testGetStringWhenNull() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getString(5)).isNull();
  }

  @Test
  public void testGetInt() throws Exception {
    cursor.moveToFirst();

    int[] data = {1234, 1235, 1236};

    for (int aData : data) {
      assertThat(cursor.getInt(0)).isEqualTo(aData);
      cursor.moveToNext();
    }
  }

  @Test
  public void testGetLong() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getLong(2)).isEqualTo(3463L);
  }

  @Test
  public void testGetFloat() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getFloat(3)).isEqualTo((float) 1.5);
  }

  @Test
  public void testGetDouble() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getDouble(4)).isEqualTo(3.14159);
  }

  @Test
  public void testClose() throws Exception {
    assertThat(cursor.isClosed()).isFalse();
    cursor.close();
    assertThat(cursor.isClosed()).isTrue();
  }

  @Test
  public void testIsNullWhenNull() throws Exception {
    cursor.moveToFirst();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.isNull(cursor.getColumnIndex("id"))).isFalse();
    assertThat(cursor.isNull(cursor.getColumnIndex("name"))).isFalse();

    assertThat(cursor.isNull(cursor.getColumnIndex("long_value"))).isTrue();
    assertThat(cursor.isNull(cursor.getColumnIndex("float_value"))).isTrue();
    assertThat(cursor.isNull(cursor.getColumnIndex("double_value"))).isTrue();
  }

  @Test
  public void testIsNullWhenNotNull() throws Exception {
    cursor.moveToFirst();

    for (int i = 0; i < 5; i++) {
      assertThat(cursor.isNull(i)).isFalse();
    }
  }

  @Test
  public void testIsNullWhenIndexOutOfBounds() throws Exception {
    cursor.moveToFirst();

    // column index 5 is out-of-bounds
    assertThat(cursor.isNull(5)).isTrue();
  }

  @Test
  public void testGetTypeWhenInteger() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
  }

  @Test
  public void testGetTypeWhenString() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_STRING);
  }

  @Test
  public void testGetTypeWhenLong() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(2)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
  }

  @Test
  public void testGetTypeWhenFloat() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(3)).isEqualTo(Cursor.FIELD_TYPE_FLOAT);
  }

  @Test
  public void testGetTypeWhenDouble() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(4)).isEqualTo(Cursor.FIELD_TYPE_FLOAT);
  }

  @Test
  public void testGetTypeWhenBlob() throws Exception {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes();

    PreparedStatement statement = connection.prepareStatement(sql);
    statement.setObject(1, byteData);
    statement.executeUpdate();

    setupCursor();
    cursor.moveToFirst();

    assertThat(cursor.getType(5)).isEqualTo(Cursor.FIELD_TYPE_BLOB);
  }

  @Test
  public void testGetTypeWhenNull() throws Exception {
    cursor.moveToFirst();

    assertThat(cursor.getType(5)).isEqualTo(Cursor.FIELD_TYPE_NULL);
  }

  private void addPeople() throws Exception {
    String[] inserts = {
        "INSERT INTO table_name (id, name, long_value, float_value, double_value) VALUES(1234, 'Chuck', 3463, 1.5, 3.14159);",
        "INSERT INTO table_name (id, name) VALUES(1235, 'Julie');",
        "INSERT INTO table_name (id, name) VALUES(1236, 'Chris');"
    };

    for (String insert : inserts) {
      connection.createStatement().executeUpdate(insert);
    }
  }

  private void setupCursor() throws Exception {
    Statement statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
    String sql ="SELECT * FROM table_name;";
    resultSet = statement.executeQuery("SELECT * FROM table_name;");
    cursor = new SQLiteCursor(null, null, null, null);
    Robolectric.shadowOf(cursor).setResultSet(resultSet, sql);
  }

  private void setupEmptyResult() throws Exception {
    Statement statement = connection.createStatement();
    statement.executeUpdate("DELETE FROM table_name;");

    setupCursor();
  }

  private void assertColumnNames(String[] columnNames) {
    assertThat(columnNames.length).isEqualTo(7);
    assertThat(columnNames[0]).isEqualTo("id");
    assertThat(columnNames[1]).isEqualTo("name");
    assertThat(columnNames[2]).isEqualTo("long_value");
    assertThat(columnNames[3]).isEqualTo("float_value");
    assertThat(columnNames[4]).isEqualTo("double_value");
    assertThat(columnNames[5]).isEqualTo("blob_value");
    assertThat(columnNames[6]).isEqualTo("clob_value");
  }

}
