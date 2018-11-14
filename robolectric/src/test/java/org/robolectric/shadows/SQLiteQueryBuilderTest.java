package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteQueryBuilderTest {

  private static final String TABLE_NAME = "sqlBuilderTest";
  private static final String COL_VALUE = "valueCol";
  private static final String COL_GROUP = "groupCol";
  
  private SQLiteDatabase database;
  private SQLiteQueryBuilder builder;

  private long firstRecordId;

  @Before
  public void setUp() throws Exception {
    database = SQLiteDatabase.create(null);

    database.execSQL("create table " + TABLE_NAME + " ("
        + COL_VALUE + " TEXT, "
        + COL_GROUP + " INTEGER"
        + ")");

    ContentValues values = new ContentValues();
    values.put(COL_VALUE, "record1");
    values.put(COL_GROUP, 1);
    firstRecordId = database.insert(TABLE_NAME, null, values);
    assertThat(firstRecordId).isGreaterThan(0L);

    values.clear();
    values.put(COL_VALUE, "record2");
    values.put(COL_GROUP, 1);
    long secondRecordId = database.insert(TABLE_NAME, null, values);
    assertThat(secondRecordId).isGreaterThan(0L);
    assertThat(secondRecordId).isNotEqualTo(firstRecordId);

    values.clear();
    values.put(COL_VALUE, "won't be selected");
    values.put(COL_GROUP, 2);
    database.insert(TABLE_NAME, null, values);

    builder = new SQLiteQueryBuilder();
    builder.setTables(TABLE_NAME);
    builder.appendWhere(COL_VALUE + " <> ");
    builder.appendWhereEscapeString("won't be selected");
  }

  @After
  public void tearDown() {
    database.close();
  }

  @Test
  public void shouldBeAbleToMakeQueries() {
    Cursor cursor = builder.query(database, new String[] {"rowid"}, null, null, null, null, null);
    assertThat(cursor.getCount()).isEqualTo(2);
  }

  @Test
  public void shouldBeAbleToMakeQueriesWithSelection() {
    Cursor cursor = builder.query(database, new String[] {"rowid"}, COL_VALUE + "=?", new String[] {"record1"}, null, null, null);
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getLong(0)).isEqualTo(firstRecordId);
  }

  @Test
  public void shouldBeAbleToMakeQueriesWithGrouping() {
    Cursor cursor = builder.query(database, new String[] {"rowid"}, null, null, COL_GROUP, null, null);
    assertThat(cursor.getCount()).isEqualTo(1);
  }

}
