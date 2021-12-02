package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import dalvik.system.CloseGuard;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowMergeCursorTest {

  private SQLiteDatabase database;
  private MergeCursor cursor;
  private SQLiteCursor dbCursor1;
  private SQLiteCursor dbCursor2;

  private static String[] TABLE_1_INSERTS = {
    "INSERT INTO table_1 (id, name_1, value_1, float_value_1, double_value_1) VALUES(1234,"
        + " 'Chuck', 3463, 1.5, 3.14159);",
    "INSERT INTO table_1 (id, name_1) VALUES(1235, 'Julie');",
    "INSERT INTO table_1 (id, name_1) VALUES(1236, 'Chris');"
  };

  private static String[] TABLE_2_INSERTS = {
    "INSERT INTO table_2 (id, name_2, value_2, float_value_2, double_value_2) VALUES(4321, 'Mary',"
        + " 3245, 5.4, 2.7818);",
    "INSERT INTO table_2 (id, name_2) VALUES(4322, 'Elizabeth');",
    "INSERT INTO table_2 (id, name_2) VALUES(4323, 'Chester');"
  };

  @Before
  public void setUp() throws Exception {
    database = SQLiteDatabase.create(null);
    dbCursor1 =
        setupTable(
            "CREATE TABLE table_1(id INTEGER PRIMARY KEY, name_1 VARCHAR(255), value_1"
                + " INTEGER,float_value_1 REAL, double_value_1 DOUBLE, blob_value_1 BINARY,"
                + " clob_value_1 CLOB );",
            TABLE_1_INSERTS,
            "SELECT * FROM table_1;");
    dbCursor2 =
        setupTable(
            "CREATE TABLE table_2(id INTEGER PRIMARY KEY, name_2 VARCHAR(255), value_2"
                + " INTEGER,float_value_2 REAL, double_value_2 DOUBLE, blob_value_2 BINARY,"
                + " clob_value_2 CLOB );",
            TABLE_2_INSERTS,
            "SELECT * FROM table_2;");
  }

  @After
  public void tearDown() throws Exception {
    database.close();
    if (cursor != null) {
      cursor.close();
    }
    dbCursor1.close();
    dbCursor2.close();
  }

  private SQLiteCursor setupTable(final String createSql, final String[] insertions, final String selectSql) {
    database.execSQL(createSql);

    for (String insert : insertions) {
      database.execSQL(insert);
    }

    Cursor cursor = database.rawQuery(selectSql, null);
    assertThat(cursor).isInstanceOf(SQLiteCursor.class);

    return (SQLiteCursor) cursor;
  }

  @Test
  public void shouldThrowIfConstructorArgumentIsNull() {
    CloseGuard.Reporter originalReporter = CloseGuard.getReporter();
    try {
      // squelch spurious CloseGuard error
      CloseGuard.setReporter((s, throwable) -> {});
      assertThrows(NullPointerException.class, () -> new MergeCursor(null));
    } finally {
      CloseGuard.setReporter(originalReporter);
    }
  }

  @Test
  public void testEmptyCursors() {
    // cursor list with null contents
    cursor = new MergeCursor( new Cursor[1] );
    assertThat(cursor.getCount()).isEqualTo(0);
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.getColumnNames()).isNotNull();
    cursor.close();

    // cursor list with partially null contents
    Cursor[] cursors = new Cursor[2];
    cursors[0] = null;
    cursors[1] = dbCursor1;
    cursor = new MergeCursor( cursors );
    assertThat(cursor.getCount()).isEqualTo(TABLE_1_INSERTS.length);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getColumnNames()).isNotNull();
  }

  @Test
  public void testMoveToPositionEmptyCursor() {
    Cursor[] cursors = new Cursor[2];
    cursors[0] = null;
    cursors[1] = null;

    cursor = new MergeCursor( cursors );
    assertThat(cursor.getCount()).isEqualTo(0);
    assertThat(cursor.getColumnNames()).isNotNull();

    cursor.moveToPosition(0);

    assertThat(cursor.getColumnNames()).isNotNull();
  }

  @Test
  public void testBoundsSingleCursor() {
    Cursor[] cursors = new Cursor[1];
    cursors[0] = dbCursor1;

    assertBounds( cursors, TABLE_1_INSERTS.length );
  }

  @Test
  public void testBoundsMultipleCursor() {
    Cursor[] cursors = new Cursor[2];
    cursors[0] = dbCursor1;
    cursors[1] = dbCursor2;

    assertBounds( cursors, TABLE_1_INSERTS.length + TABLE_2_INSERTS.length );
  }

  private void assertBounds( Cursor[] cursors, int expectedLength ) {
    cursor = new MergeCursor( cursors );

    assertThat(cursor.getCount()).isEqualTo(expectedLength);
    assertThat(cursor.moveToFirst()).isTrue();

    for ( int i = 0; i < expectedLength; i++ ) {
      assertThat(cursor.moveToPosition(i)).isTrue();
      assertThat(cursor.isAfterLast()).isFalse();
    }
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.isAfterLast()).isTrue();
    assertThat(cursor.moveToPosition(expectedLength)).isFalse();
  }

  @Test
  public void testGetDataSingleCursor() throws Exception {
    Cursor[] cursors = new Cursor[1];
    cursors[0] = dbCursor1;
    cursor = new MergeCursor( cursors );

    cursor.moveToFirst();
    assertDataCursor1();
  }

  @Test
  public void testGetDataMultipleCursor() throws Exception {
    Cursor[] cursors = new Cursor[2];
    cursors[0] = dbCursor1;
    cursors[1] = dbCursor2;
    cursor = new MergeCursor( cursors );

    cursor.moveToFirst();
    assertDataCursor1();
    cursor.moveToNext();
    assertDataCursor2();
  }

  private void assertDataCursor1() {
    assertThat(cursor.getInt(0)).isEqualTo(1234);
    assertThat(cursor.getString(1)).isEqualTo("Chuck");
    assertThat(cursor.getInt(2)).isEqualTo(3463);
    assertThat(cursor.getFloat(3)).isEqualTo(1.5f);
    assertThat(cursor.getDouble(4)).isEqualTo(3.14159);

    cursor.moveToNext();
    assertThat(cursor.getInt(0)).isEqualTo(1235);
    assertThat(cursor.getString(1)).isEqualTo("Julie");

    cursor.moveToNext();
    assertThat(cursor.getInt(0)).isEqualTo(1236);
    assertThat(cursor.getString(1)).isEqualTo("Chris");
  }

  private void assertDataCursor2() {
    assertThat(cursor.getInt(0)).isEqualTo(4321);
    assertThat(cursor.getString(1)).isEqualTo("Mary");
    assertThat(cursor.getInt(2)).isEqualTo(3245);
    assertThat(cursor.getFloat(3)).isEqualTo(5.4f);
    assertThat(cursor.getDouble(4)).isEqualTo(2.7818);

    cursor.moveToNext();
    assertThat(cursor.getInt(0)).isEqualTo(4322);
    assertThat(cursor.getString(1)).isEqualTo("Elizabeth");

    cursor.moveToNext();
    assertThat(cursor.getInt(0)).isEqualTo(4323);
    assertThat(cursor.getString(1)).isEqualTo("Chester");
  }

  @Test
  public void testColumnNamesSingleCursor() {
    Cursor[] cursors = new Cursor[1];
    cursors[0] = dbCursor1;
    cursor = new MergeCursor( cursors );

    for ( int i = 0; i < TABLE_1_INSERTS.length; i++ ) {
      cursor.moveToPosition(i);
      String[] columnNames = cursor.getColumnNames();
      assertColumnNamesCursor1(columnNames);
    }
  }

  @Test
  public void testColumnNamesMultipleCursors() {
    Cursor[] cursors = new Cursor[2];
    cursors[0] = dbCursor1;
    cursors[1] = dbCursor2;
    cursor = new MergeCursor( cursors );

    for ( int i = 0; i < TABLE_1_INSERTS.length; i++ ) {
      cursor.moveToPosition(i);
      String[] columnNames = cursor.getColumnNames();
      assertColumnNamesCursor1(columnNames);
    }

    for ( int i = 0; i < TABLE_2_INSERTS.length; i++ ) {
      cursor.moveToPosition(i + TABLE_1_INSERTS.length);
      String[] columnNames = cursor.getColumnNames();
      assertColumnNamesCursor2(columnNames);
    }
  }

  private void assertColumnNamesCursor1(String[] columnNames) {
    assertThat(columnNames.length).isEqualTo(7);
    assertThat(columnNames[0]).isEqualTo("id");
    assertThat(columnNames[1]).isEqualTo("name_1");
    assertThat(columnNames[2]).isEqualTo("value_1");
    assertThat(columnNames[3]).isEqualTo("float_value_1");
    assertThat(columnNames[4]).isEqualTo("double_value_1");
    assertThat(columnNames[5]).isEqualTo("blob_value_1");
    assertThat(columnNames[6]).isEqualTo("clob_value_1");
  }

  private void assertColumnNamesCursor2(String[] columnNames) {
    assertThat(columnNames.length).isEqualTo(7);
    assertThat(columnNames[0]).isEqualTo("id");
    assertThat(columnNames[1]).isEqualTo("name_2");
    assertThat(columnNames[2]).isEqualTo("value_2");
    assertThat(columnNames[3]).isEqualTo("float_value_2");
    assertThat(columnNames[4]).isEqualTo("double_value_2");
    assertThat(columnNames[5]).isEqualTo("blob_value_2");
    assertThat(columnNames[6]).isEqualTo("clob_value_2");
  }

  @Test
  public void testCloseCursors() {
    Cursor[] cursors = new Cursor[2];
    cursors[0] = dbCursor1;
    cursors[1] = dbCursor2;
    cursor = new MergeCursor( cursors );

    assertThat(cursor.isClosed()).isFalse();
    assertThat(dbCursor1.isClosed()).isFalse();
    assertThat(dbCursor2.isClosed()).isFalse();

    cursor.close();

    assertThat(cursor.isClosed()).isTrue();
    assertThat(dbCursor1.isClosed()).isTrue();
    assertThat(dbCursor2.isClosed()).isTrue();
  }

}
