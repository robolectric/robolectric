package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.shadowOf;


public abstract class DatabaseTestBase {
  protected SQLiteDatabase database;
  protected ShadowSQLiteDatabase shDatabase;

  @Before
  public void setUp() throws Exception {
    database = SQLiteDatabase.openDatabase("path", null, 0);
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
   * But just mimicing Android would not be helpful,
   * since it would be less than obvious where the problem is coming from.
   * One should just avoid ever calling a statement without selectionArgs (when one has a ? placeholder),
   * so it is best to throw an Exception to let the programmer know that this isn't going to turn out well if they try to run it under Android.
   * Because we are running in the context of a test we do not have to mimic Android precisely (if it is more helpful not to!), we just need to help
   * the testing programmer figure out what is going on.
   */

  @Test(expected = Exception.class)
  public void testRawQueryCount4() throws Exception {
    //Android and SQLite don't normally throw an exception here. See above explanation as to why Robolectric should.
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", null);
  }

  @Test(expected = Exception.class)
  public void testRawQueryCount5() throws Exception {
    //Android and SQLite don't normally throw an exception here. See above explanation as to why Robolectric should.
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable WHERE `id` = ?", new String[]{});
  }

  @Test(expected = android.database.sqlite.SQLiteException.class)
  public void testRawQueryCount8() throws Exception {
    Cursor cursor = database.rawQuery("select second_column, first_column from rawtable", new String[]{"1"});
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
    Statement statement;
    ResultSet resultSet;

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    statement = shadowOf(database).getConnection().createStatement();
    resultSet = statement.executeQuery("SELECT COUNT(*) FROM table_name");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(1);

    statement = shadowOf(database).getConnection().createStatement();
    resultSet = statement.executeQuery("SELECT * FROM table_name");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(1234);
    assertThat(resultSet.getString(4)).isEqualTo("Chuck");
  }

  @Test
  public void testExecSQLParams() throws Exception {
    Statement statement;
    ResultSet resultSet;

    database.execSQL("CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ", new Object[]{});
    database.execSQL("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)", new Object[]{"Leg Press", 0});
    database.execSQL("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)", new Object[]{"Bench Press", 1});

    statement = shadowOf(database).getConnection().createStatement();
    resultSet = statement.executeQuery("SELECT COUNT(*) FROM `routine`");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(2);

    statement = shadowOf(database).getConnection().createStatement();
    resultSet = statement.executeQuery("SELECT `id`, `name` ,`lastUsed` FROM `routine`");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(1);
    assertThat(resultSet.getString(2)).isEqualTo("Leg Press");
    assertThat(resultSet.getInt(3)).isEqualTo(0);
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getLong(1)).isEqualTo(2L);
    assertThat(resultSet.getString(2)).isEqualTo("Bench Press");
    assertThat(resultSet.getInt(3)).isEqualTo(1);
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
    assertThat(cursor.getString(firstIndex)).isEqualTo((String) null);

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
  public void shouldStoreGreatBigHonkinIntegersCorrectly() throws Exception {
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

    Statement statement = shadowOf(database).getConnection().createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM table_name");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(1);
  }

  @Test
  public void testFailureTransaction() throws Exception {
    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.beginTransaction();
    assertThat(shDatabase.isTransactionSuccess()).isFalse();

    database.execSQL("INSERT INTO table_name (id, name) VALUES(1234, 'Chuck');");

    Statement statement = shadowOf(database).getConnection().createStatement();
    final String select = "SELECT COUNT(*) FROM table_name";

    ResultSet rs = statement.executeQuery(select);
    assertThat(rs.next()).isTrue();
    assertThat(rs.getInt(1)).isEqualTo(1);
    rs.close();

    assertThat(shDatabase.isTransactionSuccess()).isFalse();
    database.endTransaction();

    statement = shadowOf(database).getConnection().createStatement();
    rs = statement.executeQuery(select);
    assertThat(rs.next()).isTrue();
    assertThat(rs.getInt(1)).isEqualTo(0);

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
    Statement statement = shadowOf(database).getConnection().createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM table_name");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(2);
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
    Statement statement = shadowOf(database).getConnection().createStatement();
    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM table_name");
    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getInt(1)).isEqualTo(0);
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

  protected long addChuck() {
    return addPerson(1234L, "Chuck");
  }

  protected long addJulie() {
    return addPerson(1235L, "Julie");
  }

  protected long addPerson(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("id", id);
    values.put("name", name);
    return database.insert("table_name", null, values);
  }

  protected int updateName(long id, String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, "id=" + id, null);
  }

  protected int updateName(String name) {
    ContentValues values = new ContentValues();
    values.put("name", name);
    return database.update("table_name", values, null, null);
  }

  protected void assertIdAndName(Cursor cursor, long id, String name) {
    long idValueFromDatabase;
    String stringValueFromDatabase;

    idValueFromDatabase = cursor.getLong(0);
    stringValueFromDatabase = cursor.getString(1);
    assertThat(idValueFromDatabase).isEqualTo(id);
    assertThat(stringValueFromDatabase).isEqualTo(name);
  }

  protected void assertEmptyDatabase() {
    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.isClosed()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
  }

  protected void assertNonEmptyDatabase() {
    Cursor cursor = database.query("table_name", new String[]{"id", "name"}, null, null, null, null, null);
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isNotEqualTo(0);
  }
}
