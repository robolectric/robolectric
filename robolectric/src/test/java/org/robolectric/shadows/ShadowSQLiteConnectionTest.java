package org.robolectric.shadows;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.shadows.ShadowSQLiteConnection.convertSQLWithLocalizedUnicodeCollator;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowSQLiteConnectionTest {
  private SQLiteDatabase database;

  @Before
  public void setUp() throws Exception {
    final File databasePath = RuntimeEnvironment.application.getDatabasePath("database.db");
    databasePath.getParentFile().mkdirs();

    database = SQLiteDatabase.openOrCreateDatabase(databasePath.getPath(), null);
    SQLiteStatement createStatement = database.compileStatement(
        "CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
    createStatement.execute();
  }

  @After
  public void tearDown() throws Exception {
    database.close();
  }

  @Test
  public void testSqlConversion() {
    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine`"))
        .isEqualTo("select * from `routine`");

    assertThat(convertSQLWithLocalizedUnicodeCollator(
        "select * from `routine` order by name \n\r \f collate\f\n\tunicode"
        + "\n, id \n\n\t collate\n\t \n\flocalized"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE\n"
            + ", id COLLATE NOCASE");

    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine` order by name collate localized"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");

    assertThat(convertSQLWithLocalizedUnicodeCollator("select * from `routine` order by name collate unicode"))
        .isEqualTo("select * from `routine` order by name COLLATE NOCASE");
  }

  @Test
  public void testSQLWithLocalizedOrUnicodeCollatorShouldBeSortedAsNoCase() throws Exception {
    database.execSQL("insert into routine(name) values ('الصحافة اليدوية')");
    database.execSQL("insert into routine(name) values ('Hand press 1')");
    database.execSQL("insert into routine(name) values ('hand press 2')");
    database.execSQL("insert into routine(name) values ('Hand press 3')");

    List<String> expected = Arrays.asList("Hand press 1", "hand press 2", "Hand press 3", "الصحافة اليدوية" );
    String sqlLocalized = "SELECT `name` FROM `routine` ORDER BY `name` collate localized";
    String sqlUnicode = "SELECT `name` FROM `routine` ORDER BY `name` collate unicode";

    assertThat(simpleQueryForList(database, sqlLocalized)).isEqualTo(expected);
    assertThat(simpleQueryForList(database, sqlUnicode)).isEqualTo(expected);
  }

  private List<String> simpleQueryForList(SQLiteDatabase db, String sql) {
    Cursor cursor = db.rawQuery(sql, new String[0]);
    List<String> result = new ArrayList<>();
    while (cursor.moveToNext()) {
      result.add(cursor.getString(0));
    }
    cursor.close();
    return result;
  }
}
