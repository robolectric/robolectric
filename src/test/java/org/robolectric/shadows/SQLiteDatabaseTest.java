package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.SQLiteMap;

import java.io.File;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.shadowOf;

@DatabaseConfig.UsingDatabaseMap(SQLiteMap.class)
@RunWith(TestRunners.WithDefaults.class)
public class SQLiteDatabaseTest extends DatabaseTestBase {
  private static final String ANY_VALID_SQL = "SELECT 1";

  private SQLiteDatabase database;
  private ShadowSQLiteDatabase shDatabase;

  @Before
  public void setUp() throws Exception {
    database = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("path").getPath(), null, 0);
    shDatabase = Robolectric.shadowOf(database);
    database.execSQL("CREATE TABLE table_name (\n" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "  first_column VARCHAR(255),\n" +
            "  second_column BINARY,\n" +
            "  name VARCHAR(255),\n" +
            "  big_int INTEGER\n" +
            ");");

    database.execSQL("CREATE TABLE rawtable (\n" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "  first_column VARCHAR(255),\n" +
            "  second_column BINARY,\n" +
            "  name VARCHAR(255),\n" +
            "  big_int INTEGER\n" +
            ");");

    database.execSQL("CREATE TABLE exectable (\n" +
            "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "  first_column VARCHAR(255),\n" +
            "  second_column BINARY,\n" +
            "  name VARCHAR(255),\n" +
            "  big_int INTEGER\n" +
            ");");

    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[]{1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("rawtable", null, values);
    ////////////////////////////////////////////////
    String stringColumnValue2 = "column_value2";
    byte[] byteColumnValue2 = new byte[]{4, 5, 6};
    ContentValues values2 = new ContentValues();

    values2.put("first_column", stringColumnValue2);
    values2.put("second_column", byteColumnValue2);

    database.insert("rawtable", null, values2);
  }

  @After
  public void tearDown() throws Exception {
    database.close();
  }

  @Test
  public void shouldUseSQLiteDatabaseMap() throws Exception {
    assertThat(DatabaseConfig.getDatabaseMap().getClass().getName()).isEqualTo(SQLiteMap.class.getName());
  }

  @Test
  public void testInsertAndQuery() throws Exception {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[]{1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("table_name", null, values);

    Cursor cursor = database.query("table_name", new String[]{"second_column", "first_column"}, null, null, null, null, null);

    assertThat(cursor.moveToFirst()).isTrue();

    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);

    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
  }

  @Test
  public void testInsertAndRawQuery() throws Exception {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[]{1, 2, 3};

    ContentValues values = new ContentValues();

    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);

    database.insert("table_name", null, values);

    Cursor cursor = database.rawQuery("select second_column, first_column from table_name", null);

    assertThat(cursor.moveToFirst()).isTrue();

    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);

    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
  }

  @Test(expected = android.database.SQLException.class)
  public void testInsertOrThrowWithSimulatedSQLException() {
    shDatabase.setThrowOnInsert(true);
    database.insertOrThrow("table_name", null, new ContentValues());
  }

  @Test(expected = android.database.SQLException.class)
  public void testInsertOrThrowWithSQLException() {
    ContentValues values = new ContentValues();
    values.put("id", 1);

    database.insertOrThrow("table_name", null, values);
    database.insertOrThrow("table_name", null, values);
  }

  @Test
  public void testInsertOrThrow() {
    String stringColumnValue = "column_value";
    byte[] byteColumnValue = new byte[]{1, 2, 3};
    ContentValues values = new ContentValues();
    values.put("first_column", stringColumnValue);
    values.put("second_column", byteColumnValue);
    database.insertOrThrow("table_name", null, values);

    Cursor cursor = database.rawQuery("select second_column, first_column from table_name", null);
    assertThat(cursor.moveToFirst()).isTrue();
    byte[] byteValueFromDatabase = cursor.getBlob(0);
    String stringValueFromDatabase = cursor.getString(1);
    assertThat(stringValueFromDatabase).isEqualTo(stringColumnValue);
    assertThat(byteValueFromDatabase).isEqualTo(byteColumnValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRawQueryThrowsIndex0NullException() throws Exception {
    database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", new String[]{null});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRawQueryThrowsIndex0NullException2() throws Exception {
    database.rawQuery("select second_column, first_column from rawtable", new String[]{null});
  }

  @Test
  public void testRawQueryCount() throws Exception {
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", new String[]{"1"});
    assertThat(cursor.getCount()).isEqualTo(1);
  }

  @Test
  public void testRawQueryCount2() throws Exception {
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable", null);
    assertThat(cursor.getCount()).isEqualTo(2);
  }

  @Test
  public void testRawQueryCount3() throws Exception {
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable", new String[]{});
    assertThat(cursor.getCount()).isEqualTo(2);
  }
  /*
   * Reason why testRawQueryCount4() and testRawQueryCount5() expects exceptions even though exceptions are not found in Android.
   *
   * The code in Android acts inconsistently under API version 2.1_r1 (and perhaps other APIs)..
   * What happens is that rawQuery() remembers the selectionArgs of previous queries,
   * and uses them if no selectionArgs are given in subsequent queries.
   * If they were never given selectionArgs THEN they return empty cursors.
   *
   *
   * if you run {
   *     db.rawQuery("select * from exercise WHERE name = ?",null); //this returns an empty cursor
   *      db.rawQuery("select * from exercise WHERE name = ?",new String[]{}); //this returns an empty cursor
   * }
   *
   * but if you run {
   *    db.rawQuery("select * from exercise WHERE name = ?",new String[]{"Leg Press"}); //this returns 1 exercise named "Leg Press"
   *    db.rawQuery("select * from exercise WHERE name = ?",null); //this too returns 1 exercise named "Leg Press"
   *    db.rawQuery("select * from exercise WHERE name = ?",new String[]{}); //this too returns 1 exercise named "Leg Press"
   * }
   *
   * so SQLite + Android work inconsistently (it maintains state that it should not)
   * whereas H2 just throws an exception for not supplying the selectionArgs
   *
   * So the question is should Robolectric:
   * 1) throw an exception, the way H2 does.
   * 2) return an empty Cursor.
   * 3) mimic Android\SQLite precisely and return inconsistent results based on previous state
   *
   * Returning an empty cursor all the time would be bad
   * because Android doesn't always return an empty cursor.
   * But just mimicking Android would not be helpful,
   * since it would be less than obvious where the problem is coming from.
   * One should just avoid ever calling a statement without selectionArgs (when one has a ? placeholder),
   * so it is best to throw an Exception to let the programmer know that this isn't going to turn out well if they try to run it under Android.
   * Because we are running in the context of a test we do not have to mimic Android precisely (if it is more helpful not to!), we just need to help
   * the testing programmer figure out what is going on.
   */
  @Test(expected = Exception.class)
  public void testRawQueryCount4() throws Exception {
    //Android and SQLite don't normally throw an exception here. See above explanation as to why Robolectric should.
    database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", null);
  }

  @Test(expected = Exception.class)
  public void testRawQueryCount5() throws Exception {
    //Android and SQLite don't normally throw an exception here. See above explanation as to why Robolectric should.
    database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", new String[]{});
  }

  @Test(expected = android.database.sqlite.SQLiteException.class)
  public void testRawQueryCount8() throws Exception {
    database.rawQuery("select second_column, first_column from rawtable", new String[]{"1"});
  }

  @Test
  public void testInsertWithException() {
    ContentValues values = new ContentValues();

    assertEquals(-1, database.insert("table_that_doesnt_exist", null, values));
  }


  @Test
  public void testEmptyTable() throws Exception {
    Cursor cursor = database.query("table_name", new String[]{"second_column", "first_column"}, null, null, null, null, null);

    assertThat(cursor.moveToFirst()).isFalse();
  }

  @Test
  public void testInsertRowIdGeneration() throws Exception {
    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long id = database.insert("table_name", null, values);

    assertThat(id).isNotEqualTo(0L);
  }

  @Test
  public void testInsertKeyGeneration() throws Exception {
    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long key = database.insertWithOnConflict("table_name", null, values, SQLiteDatabase.CONFLICT_IGNORE);

    assertThat(key).isNotEqualTo(0L);
  }

  @Test
  public void testUpdate() throws Exception {
    addChuck();

    assertThat(updateName(1234L, "Buster")).isEqualTo(1);

    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);

    assertIdAndName(cursor, 1234L, "Buster");
  }

  @Test
  public void testUpdateNoMatch() throws Exception {
    addChuck();

    assertThat(updateName(5678L, "Buster")).isEqualTo(0);

    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);

    assertIdAndName(cursor, 1234L, "Chuck");
  }

  @Test
  public void testUpdateAll() throws Exception {
    addChuck();
    addJulie();

    assertThat(updateName("Belvedere")).isEqualTo(2);

    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);

    assertIdAndName(cursor, 1234L, "Belvedere");
    assertThat(cursor.moveToNext()).isTrue();

    assertIdAndName(cursor, 1235L, "Belvedere");
    assertThat(cursor.isLast()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.isAfterLast()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
  }

  @Test
  public void testDelete() throws Exception {
    addChuck();

    int deleted = database.delete("table_name", "id=1234", null);
    assertThat(deleted).isEqualTo(1);

    assertEmptyDatabase();
  }

  @Test
  public void testDeleteNoMatch() throws Exception {
    addChuck();

    int deleted = database.delete("table_name", "id=5678", null);
    assertThat(deleted).isEqualTo(0);

    assertNonEmptyDatabase();
  }

  @Test
  public void testDeleteAll() throws Exception {
    addChuck();
    addJulie();

    int deleted = database.delete("table_name", "1", null);
    assertThat(deleted).isEqualTo(2);

    assertEmptyDatabase();
  }

  @Test
  public void testExecSQL() throws Exception {
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);

    cursor = database.rawQuery("SELECT * FROM table_name", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(1234);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Chuck");
  }

  @Test
  public void testExecSQLParams() throws Exception {
    database.execSQL("CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ", new Object[]{});
    database.execSQL("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)", new Object[]{"Leg Press", 0});
    database.execSQL("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)", new Object[]{"Bench Press", 1});

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM `routine`", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(2);

    cursor = database.rawQuery("SELECT `id`, `name` ,`lastUsed` FROM `routine`", null);
    assertThat(cursor).isNotNull();
    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(1);
    assertThat(cursor.getInt(cursor.getColumnIndex("lastUsed"))).isEqualTo(0);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Leg Press");

    assertThat(cursor.moveToNext()).isTrue();

    assertThat(cursor.getInt(cursor.getColumnIndex("id"))).isEqualTo(2);
    assertThat(cursor.getInt(cursor.getColumnIndex("lastUsed"))).isEqualTo(1);
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Bench Press");
  }

  @Test(expected = android.database.SQLException.class)
  public void testExecSQLException() throws Exception {
    database.execSQL("INSERT INTO table_name;");    // invalid SQL
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecSQLException2() throws Exception {
    database.execSQL("insert into exectable (first_column) values (?);", null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExecSQLException4() throws Exception {
    database.execSQL("insert into exectable (first_column) values ('sdfsfs');", null);
  }

  @Test(expected = Exception.class)
  public void testExecSQLException5() throws Exception {
    //TODO: make this throw android.database.SQLException.class
    database.execSQL("insert into exectable (first_column) values ('kjhk');", new String[]{"xxxx"});
  }

  @Test(expected = Exception.class)
  public void testExecSQLException6() throws Exception {
    //TODO: make this throw android.database.SQLException.class
    database.execSQL("insert into exectable (first_column) values ('kdfd');", new String[]{null});
  }

  @Test
  public void testExecSQL2() throws Exception {
    database.execSQL("insert into exectable (first_column) values ('eff');", new String[]{});
  }

  @Test
  public void testExecSQLInsertNull() throws Exception {
    String name = "nullone";

    database.execSQL("insert into exectable (first_column, name) values (?,?);", new String[]{null, name});

    Cursor cursor = database.rawQuery("select * from exectable WHERE `name` = ?", new String[]{name});
    cursor.moveToFirst();
    int firstIndex = cursor.getColumnIndex("first_column");
    int nameIndex = cursor.getColumnIndex("name");
    assertThat(cursor.getString(nameIndex)).isEqualTo(name);
    assertThat(cursor.getString(firstIndex)).isEqualTo(null);

  }

  @Test(expected = Exception.class)
  public void testExecSQLInsertNullShouldBeException() throws Exception {
    //this inserts null in android, but it when it happens it is likely an error.  H2 throws an exception.  So we'll make Robolectric expect an Exception so that the error can be found.

    database.delete("exectable", null, null);

    Cursor cursor = database.rawQuery("select * from exectable", null);
    cursor.moveToFirst();
    assertThat(cursor.getCount()).isEqualTo(0);

    database.execSQL("insert into exectable (first_column) values (?);", new String[]{});
    Cursor cursor2 = database.rawQuery("select * from exectable", new String[]{null});
    cursor.moveToFirst();
    assertThat(cursor2.getCount()).isEqualTo(1);

  }

  @Test
  public void testExecSQLAutoIncrementSQLite() throws Exception {
    database.execSQL("CREATE TABLE auto_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(255));");

    ContentValues values = new ContentValues();
    values.put("name", "Chuck");

    long key = database.insert("auto_table", null, values);
    assertThat(key).isNotEqualTo(0L);

    long key2 = database.insert("auto_table", null, values);
    assertThat(key2).isNotEqualTo(key);
  }

  @Test(expected = IllegalStateException.class)
  public void testClose() throws Exception {
    database.close();

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
  }

  @Test
  public void testIsOpen() throws Exception {
    assertThat(database.isOpen()).isTrue();
    database.close();
    assertThat(database.isOpen()).isFalse();
  }

  @Test
  public void shouldStoreGreatBigHonkingIntegersCorrectly() throws Exception {
    database.execSQL("INSERT INTO table_name(big_int) VALUES(1234567890123456789);");
    Cursor cursor = database.query("table_name", new String[]{"big_int"}, null, null, null, null, null);
    cursor.moveToFirst();
    assertEquals(1234567890123456789L, cursor.getLong(0));
  }

  @Test
  public void testSuccessTransaction() throws Exception {
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.setTransactionSuccessful();
    assertThat(shDatabase.isTransactionSuccess()).isTrue();
    database.endTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);
  }

  @Test
  public void testFailureTransaction() throws Exception {
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    final String select = "SELECT COUNT(*) FROM table_name";

    Cursor cursor = database.rawQuery(select, null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(1);
    cursor.close();

    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.endTransaction();

    cursor = database.rawQuery(select, null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(0);

    assertThat(shDatabase.isTransactionSuccess()).isFalse();
  }

  @Test
  public void testSuccessNestedTransaction() throws Exception {
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(12345, 'Julie');");
    database.setTransactionSuccessful();
    assertThat(shDatabase.isTransactionSuccess()).isTrue();
    database.endTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.setTransactionSuccessful();
    assertThat(shDatabase.isTransactionSuccess()).isTrue();
    database.endTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(2);
  }

  @Test
  public void testFailureNestedTransaction() throws Exception {
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.execSQL("INSERT INTO table_name (id, name) VALUES(12345, 'Julie');");
    database.endTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.setTransactionSuccessful();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.endTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();

    Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM table_name", null);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getInt(0)).isEqualTo(0);
  }

  @Test
  public void testTransactionAlreadySuccessful() {
    database.beginTransaction();
    database.setTransactionSuccessful();
    try {
      database.setTransactionSuccessful();
      fail("didn't receive the expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertThat(e.getMessage()).isEqualTo("transaction already successfully");
    }
  }

  @Test
  public void testInTransaction() throws Exception {
    assertThat(database.inTransaction()).isFalse();
    database.beginTransaction();
    assertThat(database.inTransaction()).isTrue();
    database.endTransaction();
    assertThat(database.inTransaction()).isFalse();
  }



  @Test
  public void testReplace() throws Exception {
    long id = addChuck();
    assertThat(id).isNotEqualTo(-1L);

    ContentValues values = new ContentValues();
    values.put("id", id);
    values.put("name", "Norris");

    long replaceId = database.replace("table_name", null, values);
    assertThat(replaceId).isEqualTo(id);

    String query = "SELECT name FROM table_name where id = " + id;
    Cursor cursor = executeQuery(query);

    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("name"))).isEqualTo("Norris");
  }

  @Test
  public void testReplaceIsReplacing() throws Exception {
    final String query = "SELECT first_column FROM table_name WHERE id = ";
    String stringValueA = "column_valueA";
    String stringValueB = "column_valueB";
    long id = 1;

    ContentValues valuesA = new ContentValues();
    valuesA.put("id", id);
    valuesA.put("first_column", stringValueA);

    ContentValues valuesB = new ContentValues();
    valuesB.put("id", id);
    valuesB.put("first_column", stringValueB);

    long firstId = database.replaceOrThrow("table_name", null, valuesA);
    Cursor firstCursor = executeQuery(query + firstId);
    assertThat(firstCursor.moveToNext()).isTrue();
    long secondId = database.replaceOrThrow("table_name", null, valuesB);
    Cursor secondCursor = executeQuery(query + secondId);
    assertThat(secondCursor.moveToNext());

    assertThat(firstId).isEqualTo(id);
    assertThat(secondId).isEqualTo(id);
    assertThat(firstCursor.getString(0)).isEqualTo(stringValueA);
    assertThat(secondCursor.getString(0)).isEqualTo(stringValueB);
  }

  @Test
  public void shouldKeepTrackOfOpenCursors() throws Exception {
    Cursor cursor = database.query("table_name", new String[]{"second_column", "first_column"}, null, null, null, null, null);

    assertThat(shadowOf(database).hasOpenCursors()).isTrue();
    cursor.close();
    assertThat(shadowOf(database).hasOpenCursors()).isFalse();

  }

  @Test
  public void shouldBeAbleToAnswerQuerySql() throws Exception {
    try {
      database.query("table_name_1", new String[]{"first_column"}, null, null, null, null, null);
    } catch (Exception e) {
      //ignore
    }
    try {
      database.query("table_name_2", new String[]{"second_column"}, null, null, null, null, null);
    } catch (Exception e) {
      //ignore
    }
    List<String> queries = shadowOf(database).getQuerySql();
    assertThat(queries.size()).isEqualTo(2);
    assertThat(queries.get(0)).isEqualTo("SELECT first_column FROM table_name_1");
    assertThat(queries.get(1)).isEqualTo("SELECT second_column FROM table_name_2");
  }

  @Test
  public void shouldCreateDefaultCursorFactoryWhenNullFactoryPassed() throws Exception {
    database.rawQueryWithFactory(null, ANY_VALID_SQL, null, null);
  }

  @Test
  public void shouldOpenExistingDatabaseFromFileSystemIfFileExists() throws Exception {
      File testDb = new File(getClass().getResource("/test with spaces.sql").toURI().getPath());
      assertThat(testDb.exists()).isTrue();
      SQLiteDatabase db = SQLiteDatabase.openDatabase(testDb.getAbsolutePath(), null, 0);

      Cursor c = db.rawQuery("select * from test", null);
      assertThat(c).isNotNull();
      assertThat(c.getCount()).isEqualTo(2);
      assertThat(db.isOpen()).isTrue();
      db.close();
  }

  @Test
  public void shouldUseInMemoryDatabaseWhenCallingCreate() throws Exception {
    SQLiteDatabase db = SQLiteDatabase.create(null);
    assertThat(db.isOpen()).isTrue();
  }

  @Test
  public void shouldSetAndGetVersion() throws Exception {
    assertThat(database.getVersion()).isEqualTo(0);
    database.setVersion(20);
    assertThat(database.getVersion()).isEqualTo(20);
  }

  @Test
  public void testGetPath() throws Exception {
    assertThat(database.getPath()).isEqualTo(Robolectric.application.getDatabasePath("path").getPath());
  }

  @Test
  public void testShouldReturnTheSameDatabaseIfAlreadyOpened() throws Exception {
    String path1 = Robolectric.application.getDatabasePath("db1").getPath();
    String path2 = Robolectric.application.getDatabasePath("db2").getPath();
    SQLiteDatabase db1 = SQLiteDatabase.openDatabase(path1, null, 0);
    SQLiteDatabase db2 = SQLiteDatabase.openDatabase(path2, null, 0);
    assertThat(SQLiteDatabase.openDatabase(path1, null, 0)).isSameAs(db1);
    assertThat(SQLiteDatabase.openDatabase(path2, null, 0)).isSameAs(db2);
  }

  @Test
  public void testTwoConcurrentDbConnections() throws Exception {
    SQLiteDatabase db1 = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db1").getPath(), null, 0);
    SQLiteDatabase db2 = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db2").getPath(), null, 0);

    db1.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    db2.execSQL("CREATE TABLE bar(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");

    ContentValues d1 = new ContentValues();
    d1.put("data", "d1");

    ContentValues d2 = new ContentValues();
    d2.put("data", "d2");

    db1.insert("foo", null, d1);
    db2.insert("bar", null, d2);

    Cursor c = db1.rawQuery("select * from foo", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d1");

    c = db2.rawQuery("select * from bar", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d2");
  }

  @Test(expected = SQLiteException.class)
  public void testQueryThrowsSQLiteException() throws Exception {
    SQLiteDatabase db1 = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db1").getPath(), null, 0);
    db1.query("FOO", null, null, null, null, null, null);
  }

  @Test
  public void testCreateAndDropTable() throws Exception {
    SQLiteDatabase db = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db1").getPath(), null, 0);
    db.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    Cursor c = db.query("FOO", null, null, null, null, null, null);
    assertThat(c).isNotNull();
    db.execSQL("DROP TABLE IF EXISTS foo;");
  }

  @Test
  public void testDataPersistency() throws Exception {
    SQLiteDatabase db1 = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db1").getPath(), null, 0);

    db1.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");

    ContentValues d1 = new ContentValues();
    d1.put("data", "d1");

    db1.insert("foo", null, d1);
    
    db1.close();
    db1 = SQLiteDatabase.openDatabase(Robolectric.application.getDatabasePath("db1").getPath(), null, 0);

    Cursor c = db1.rawQuery("select * from foo", null);
    assertThat(c).isNotNull();
    assertThat(c.getCount()).isEqualTo(1);
    assertThat(c.moveToNext()).isTrue();
    assertThat(c.getString(c.getColumnIndex("data"))).isEqualTo("d1");
  }

  private Cursor executeQuery(String query) {
    return database.rawQuery(query, null);
  }

  private long addChuck() {
    return addPerson(1234L, "Chuck");
  }

  private long addJulie() {
    return addPerson(1235L, "Julie");
  }

  private long addPerson(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("id", id);
    values.put("name", name);
    return database.insert("table_name", null, values);
  }

  private int updateName(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, "id=" + id, null);
  }

  private int updateName(String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, null, null);
  }

  private void assertIdAndName(Cursor cursor, long id, String name) {
    long idValueFromDatabase;
    String stringValueFromDatabase;

    idValueFromDatabase = cursor.getLong(0);
    stringValueFromDatabase = cursor.getString(1);
    assertThat(idValueFromDatabase).isEqualTo(id);
    assertThat(stringValueFromDatabase).isEqualTo(name);
  }

  private void assertEmptyDatabase() {
    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.isClosed()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
  }

  private void assertNonEmptyDatabase() {
    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isNotEqualTo(0);
  }

  @Test
  public void shouldLockWhenEnabled() throws Exception{
    ShadowSQLiteDatabase shadowDB = shadowOf(database);

    // Test disabled locking
    shadowDB.setLockingEnabled(false);

    assertThat(database.isDbLockedByCurrentThread()).isTrue();
    shadowDB.lock();
    assertThat(database.isDbLockedByCurrentThread()).isTrue();
    shadowDB.unlock();
    assertThat(database.isDbLockedByCurrentThread()).isTrue();

    // Test enabled locking
    shadowDB.setLockingEnabled(true);
    assertThat(database.isDbLockedByCurrentThread()).isFalse();
    shadowDB.lock();
    assertThat(database.isDbLockedByCurrentThread()).isTrue();
    shadowDB.unlock();
    assertThat(database.isDbLockedByCurrentThread()).isFalse();
  }
}
