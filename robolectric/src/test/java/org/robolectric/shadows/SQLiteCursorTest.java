package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteCursorTest {

  private SQLiteDatabase database;
  private Cursor cursor;

  @Before
  public void setUp() throws Exception {
    database = SQLiteDatabase.create(null);

    database.execSQL("CREATE TABLE table_name(" +
        "id INTEGER PRIMARY KEY, " +
        "name VARCHAR(255), " +
        "long_value BIGINT," +
        "float_value REAL," +
        "double_value DOUBLE, " +
        "blob_value BINARY, " +
        "clob_value CLOB );");

    addPeople();
    cursor = createCursor();
  }

  @After
  public void tearDown() {
    database.close();
    cursor.close();
  }

  @Test
  public void testGetColumnNames() {
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
  public void testGetColumnIndex() {
    assertThat(cursor.getColumnIndex("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndex("name")).isEqualTo(1);
  }

  @Test
  public void testGetColumnIndexNotFound() {
    assertThat(cursor.getColumnIndex("Fred")).isEqualTo(-1);
  }

  @Test
  public void testGetColumnIndexEmpty() throws Exception {
    setupEmptyResult();

    assertThat(cursor.getColumnIndex("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndex("name")).isEqualTo(1);
  }

  @Test
  public void testGetColumnIndexOrThrow() {
    assertThat(cursor.getColumnIndexOrThrow("id")).isEqualTo(0);
    assertThat(cursor.getColumnIndexOrThrow("name")).isEqualTo(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetColumnIndexOrThrowNotFound() {
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
  public void testMoveToFirst() {
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
  public void testMoveToNext() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1235);
    assertThat(cursor.getString(1)).isEqualTo("Julie");
  }

  @Test
  public void testMoveToNextPastEnd() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
  }

  @Test
  public void testMoveBackwards() {
    assertThat(cursor.getPosition()).isEqualTo(-1);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(0);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(2);

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(0);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(2);

    assertThat(cursor.moveToPosition(1)).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testMoveToNextEmpty() throws Exception {
    setupEmptyResult();

    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.moveToNext()).isFalse();
  }

  @Test
  public void testMoveToPrevious() {
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.moveToPrevious()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1234);
    assertThat(cursor.getString(1)).isEqualTo("Chuck");
  }

  @Test
  public void testMoveToPreviousPastStart() {
    assertThat(cursor.moveToFirst()).isTrue();

    // Impossible to move cursor before the first item
    assertThat(cursor.moveToPrevious()).isFalse();
  }

  @Test
  public void testMoveToPreviousEmpty() throws Exception {
    setupEmptyResult();
    assertThat(cursor.moveToFirst()).isFalse();

    assertThat(cursor.moveToPrevious()).isFalse();
  }

  @Test
  public void testGetPosition() {
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(0);

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testGetBlob() {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes(UTF_8);

    database.execSQL(sql, new Object[]{byteData});

    assertThat(cursor.moveToFirst()).isTrue();

    byte[] retrievedByteData = cursor.getBlob(5);
    assertThat(byteData.length).isEqualTo(retrievedByteData.length);

    for (int i = 0; i < byteData.length; i++) {
      assertThat(byteData[i]).isEqualTo(retrievedByteData[i]);
    }
  }

  @Test
  public void testGetClob() {
    String sql = "UPDATE table_name set clob_value=? where id=1234";
    String s = "Don't CLOBber my data, please. Thank you.";

    database.execSQL(sql, new Object[]{s});

    assertThat(cursor.moveToFirst()).isTrue();

    String actual = cursor.getString(6);
    assertThat(s).isEqualTo(actual);
  }

  @Test
  public void testGetString() {
    assertThat(cursor.moveToFirst()).isTrue();

    String[] data = {"Chuck", "Julie", "Chris"};

    for (String aData : data) {
      assertThat(cursor.getString(1)).isEqualTo(aData);
      cursor.moveToNext();
    }
  }

  @Test
  public void testGetStringWhenInteger() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getString(0)).isEqualTo("1234");
  }

  @Test
  public void testGetStringWhenLong() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getString(2)).isEqualTo("3463");
  }

  @Test
  public void testGetStringWhenFloat() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getString(3)).isEqualTo("1.5");
  }

  @Test
  public void testGetStringWhenDouble() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getString(4)).isEqualTo("3.14159");
  }

  @Test(expected = SQLiteException.class)
  public void testGetStringWhenBlob() {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes(UTF_8);

    database.execSQL(sql, new Object[]{byteData});

    assertThat(cursor.moveToFirst()).isTrue();

    cursor.getString(5);
  }

  @Test(expected = SQLiteException.class)
  public void testGetIntWhenBlob() {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes(UTF_8);

    database.execSQL(sql, new Object[]{byteData});

    assertThat(cursor.moveToFirst()).isTrue();

    cursor.getInt(5);
  }

  @Test
  public void testGetStringWhenNull() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getString(5)).isNull();
  }

  @Test
  public void testGetInt() {
    assertThat(cursor.moveToFirst()).isTrue();

    int[] data = {1234, 1235, 1236};

    for (int aData : data) {
      assertThat(cursor.getInt(0)).isEqualTo(aData);
      cursor.moveToNext();
    }
  }

  @Test
  public void testGetNumbersFromStringField() {
    database.execSQL("update table_name set name = '1.2'");
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getInt(1)).isEqualTo(1);
    assertThat(cursor.getDouble(1)).isEqualTo(1.2d);
    assertThat(cursor.getFloat(1)).isEqualTo(1.2f);
  }

  @Test
  public void testGetNumbersFromBlobField() {
    database.execSQL("update table_name set name = '1.2'");
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getInt(1)).isEqualTo(1);
    assertThat(cursor.getDouble(1)).isEqualTo(1.2d);
    assertThat(cursor.getFloat(1)).isEqualTo(1.2f);
  }

  @Test
  public void testGetLong() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getLong(2)).isEqualTo(3463L);
  }

  @Test
  public void testGetFloat() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getFloat(3)).isEqualTo((float) 1.5);
  }

  @Test
  public void testGetDouble() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getDouble(4)).isEqualTo(3.14159);
  }

  @Test
  public void testClose() {
    assertThat(cursor.isClosed()).isFalse();
    cursor.close();
    assertThat(cursor.isClosed()).isTrue();
  }

  @Test
  public void testIsNullWhenNull() {
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.isNull(cursor.getColumnIndex("id"))).isFalse();
    assertThat(cursor.isNull(cursor.getColumnIndex("name"))).isFalse();

    assertThat(cursor.isNull(cursor.getColumnIndex("long_value"))).isTrue();
    assertThat(cursor.isNull(cursor.getColumnIndex("float_value"))).isTrue();
    assertThat(cursor.isNull(cursor.getColumnIndex("double_value"))).isTrue();
  }

  @Test
  public void testIsNullWhenNotNull() {
    assertThat(cursor.moveToFirst()).isTrue();

    for (int i = 0; i < 5; i++) {
      assertThat(cursor.isNull(i)).isFalse();
    }
  }

  @Test
  public void testIsNullWhenIndexOutOfBounds() {
    assertThat(cursor.moveToFirst()).isTrue();

    // column index 5 is out-of-bounds
    assertThat(cursor.isNull(5)).isTrue();
  }

  @Test
  public void testGetTypeWhenInteger() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
  }

  @Test
  public void testGetTypeWhenString() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_STRING);
  }

  @Test
  public void testGetTypeWhenLong() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(2)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
  }

  @Test
  public void testGetTypeWhenFloat() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(3)).isEqualTo(Cursor.FIELD_TYPE_FLOAT);
  }

  @Test
  public void testGetTypeWhenDouble() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(4)).isEqualTo(Cursor.FIELD_TYPE_FLOAT);
  }

  @Test
  public void testGetTypeWhenBlob() {
    String sql = "UPDATE table_name set blob_value=? where id=1234";
    byte[] byteData = sql.getBytes(UTF_8);

    database.execSQL(sql, new Object[]{byteData});

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getType(5)).isEqualTo(Cursor.FIELD_TYPE_BLOB);
  }

  @Test
  public void testGetTypeWhenNull() {
    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(5)).isEqualTo(Cursor.FIELD_TYPE_NULL);
  }

  @Test
  public void testGetNullNumberValues() {
    String sql = "UPDATE table_name set long_value=NULL, float_value=NULL, double_value=NULL";
    database.execSQL(sql);

    assertThat(cursor.moveToFirst()).isTrue();

    assertThat(cursor.getType(2)).isEqualTo(Cursor.FIELD_TYPE_NULL);
    assertThat(cursor.getLong(2)).isEqualTo(0);

    assertThat(cursor.getType(3)).isEqualTo(Cursor.FIELD_TYPE_NULL);
    assertThat(cursor.getFloat(3)).isEqualTo(0f);

    assertThat(cursor.getType(4)).isEqualTo(Cursor.FIELD_TYPE_NULL);
    assertThat(cursor.getDouble(4)).isEqualTo(0d);
  }

  private void addPeople() {
    String[] inserts = {
      "INSERT INTO table_name (id, name, long_value, float_value, double_value) VALUES(1234,"
          + " 'Chuck', 3463, 1.5, 3.14159);",
      "INSERT INTO table_name (id, name) VALUES(1235, 'Julie');",
      "INSERT INTO table_name (id, name) VALUES(1236, 'Chris');"
    };

    for (String insert : inserts) {
      database.execSQL(insert);
    }
  }

  private Cursor createCursor() {
    String sql ="SELECT * FROM table_name;";
    Cursor cursor = database.rawQuery(sql, null);
    assertThat(cursor).isInstanceOf(SQLiteCursor.class);
    return cursor;
  }

  private void setupEmptyResult() {
    database.execSQL("DELETE FROM table_name;");
    cursor.close();
    cursor = createCursor();
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
