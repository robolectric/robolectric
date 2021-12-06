package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SQLiteStatementTest {
  private SQLiteDatabase database;

  @Before
  public void setUp() throws Exception {
    final File databasePath = ApplicationProvider.getApplicationContext().getDatabasePath("path");
    databasePath.getParentFile().mkdirs();

    database = SQLiteDatabase.openOrCreateDatabase(databasePath.getPath(), null);
    SQLiteStatement createStatement =
        database.compileStatement(
            "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR ,"
                + " `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement.execute();

    SQLiteStatement createStatement2 =
        database.compileStatement(
            "CREATE TABLE `countme` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR ,"
                + " `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement2.execute();
  }

  @After
  public void tearDown() {
    database.close();
  }

  @Test
  public void testExecuteInsert() {
    SQLiteStatement insertStatement =
        database.compileStatement("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)");
    insertStatement.bindString(1, "Leg Press");
    insertStatement.bindLong(2, 0);
    long pkeyOne = insertStatement.executeInsert();
    insertStatement.clearBindings();
    insertStatement.bindString(1, "Bench Press");
    insertStatement.bindLong(2, 1);
    long pkeyTwo = insertStatement.executeInsert();

    assertThat(pkeyOne).isEqualTo(1L);
    assertThat(pkeyTwo).isEqualTo(2L);

    Cursor dataCursor = database.rawQuery("SELECT COUNT(*) FROM `routine`", null);
    assertThat(dataCursor.moveToFirst()).isTrue();
    assertThat(dataCursor.getInt(0)).isEqualTo(2);
    dataCursor.close();

    dataCursor = database.rawQuery("SELECT `id`, `name` ,`lastUsed` FROM `routine`", null);
    assertThat(dataCursor.moveToNext()).isTrue();
    assertThat(dataCursor.getInt(0)).isEqualTo(1);
    assertThat(dataCursor.getString(1)).isEqualTo("Leg Press");
    assertThat(dataCursor.getInt(2)).isEqualTo(0);
    assertThat(dataCursor.moveToNext()).isTrue();
    assertThat(dataCursor.getLong(0)).isEqualTo(2L);
    assertThat(dataCursor.getString(1)).isEqualTo("Bench Press");
    assertThat(dataCursor.getInt(2)).isEqualTo(1);
    dataCursor.close();
  }

  @Test
  public void testExecuteInsertShouldCloseGeneratedKeysResultSet() {
    // NOTE:
    // As a side-effect we will get "database locked" exception
    // on rollback if generatedKeys wasn't closed
    //
    // Don't know how suitable to use Mockito here, but
    // it will be a little bit simpler to test ShadowSQLiteStatement
    // if actualDBStatement will be mocked
    database.beginTransaction();
    try {
      SQLiteStatement insertStatement =
          database.compileStatement(
              "INSERT INTO `routine` " + "(`name` ,`lastUsed`) VALUES ('test',0)");
      try {
        insertStatement.executeInsert();
      } finally {
        insertStatement.close();
      }
    } finally {
      database.endTransaction();
    }
  }

  @Test
  public void testExecuteUpdateDelete() {

    SQLiteStatement insertStatement =
        database.compileStatement("INSERT INTO `routine` (`name`) VALUES (?)");
    insertStatement.bindString(1, "Hand Press");
    long pkeyOne = insertStatement.executeInsert();
    assertThat(pkeyOne).isEqualTo(1);

    SQLiteStatement updateStatement =
        database.compileStatement("UPDATE `routine` SET `name`=? WHERE `id`=?");
    updateStatement.bindString(1, "Head Press");
    updateStatement.bindLong(2, pkeyOne);
    assertThat(updateStatement.executeUpdateDelete()).isEqualTo(1);

    Cursor dataCursor = database.rawQuery("SELECT `name` FROM `routine`", null);
    assertThat(dataCursor.moveToNext()).isTrue();
    assertThat(dataCursor.getString(0)).isEqualTo("Head Press");
    dataCursor.close();
  }

  @Test
  public void simpleQueryTest() {

    SQLiteStatement stmt = database.compileStatement("SELECT count(*) FROM `countme`");
    assertThat(stmt.simpleQueryForLong()).isEqualTo(0L);
    assertThat(stmt.simpleQueryForString()).isEqualTo("0");

    SQLiteStatement insertStatement =
        database.compileStatement("INSERT INTO `countme` (`name` ,`lastUsed` ) VALUES (?,?)");
    insertStatement.bindString(1, "Leg Press");
    insertStatement.bindLong(2, 0);
    insertStatement.executeInsert();
    assertThat(stmt.simpleQueryForLong()).isEqualTo(1L);
    assertThat(stmt.simpleQueryForString()).isEqualTo("1");
    insertStatement.bindString(1, "Bench Press");
    insertStatement.bindLong(2, 1);
    insertStatement.executeInsert();
    assertThat(stmt.simpleQueryForLong()).isEqualTo(2L);
    assertThat(stmt.simpleQueryForString()).isEqualTo("2");
  }

  @Test
  public void simpleQueryTestWithCommonTableExpression() {
    try (SQLiteStatement statement =
        database.compileStatement(
            "WITH RECURSIVE\n"
                + "  cnt(x) AS (VALUES(1) UNION ALL SELECT x+1 FROM cnt WHERE x<100)\n"
                + "SELECT COUNT(*) FROM cnt;")) {
      assertThat(statement).isNotNull();
      assertThat(statement.simpleQueryForLong()).isEqualTo(100);
    }
  }

  @Test(expected = SQLiteDoneException.class)
  public void simpleQueryForStringThrowsSQLiteDoneExceptionTest() {
    // throw SQLiteDOneException if no rows returned.
    SQLiteStatement stmt =
        database.compileStatement("SELECT * FROM `countme` where `name`= 'cessationoftime'");

    assertThat(stmt.simpleQueryForString()).isEqualTo("0");
  }

  @Test(expected = SQLiteDoneException.class)
  public void simpleQueryForLongThrowsSQLiteDoneExceptionTest() {
    // throw SQLiteDOneException if no rows returned.
    SQLiteStatement stmt =
        database.compileStatement("SELECT * FROM `countme` where `name`= 'cessationoftime'");
    stmt.simpleQueryForLong();
  }

  @Test
  public void testCloseShouldCloseUnderlyingPreparedStatement() {
    SQLiteStatement insertStatement =
        database.compileStatement("INSERT INTO `routine` (`name`) VALUES (?)");
    insertStatement.bindString(1, "Hand Press");
    insertStatement.close();
    try {
      insertStatement.executeInsert();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(IllegalStateException.class);
    }
  }
}
