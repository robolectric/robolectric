package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.googlecode.catchexception.CatchException;
import org.fest.assertions.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.SQLiteMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static com.googlecode.catchexception.CatchException.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@DatabaseConfig.UsingDatabaseMap(SQLiteMap.class)
@RunWith(TestRunners.WithDefaults.class)
public class SQLiteDatabaseTest extends DatabaseTestBase {

  private static final String ANY_VALID_SQL = "SELECT 1";

  @Test
  public void shouldUseSQLiteDatabaseMap() throws Exception {
    assertThat(DatabaseConfig.getDatabaseMap().getClass().getName()).isEqualTo(SQLiteMap.class.getName());
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
    ResultSet resultSet = executeQuery(query);

    assertThat(resultSet.next()).isTrue();
    assertThat(resultSet.getString(1)).isEqualTo("Norris");
  }

  @Test
  public void testReplaceIsReplacing() throws SQLException {
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
    ResultSet firstResultSet = executeQuery(query + firstId);
    firstResultSet.next();
    long secondId = database.replaceOrThrow("table_name", null, valuesB);
    ResultSet secondResultSet = executeQuery(query + secondId);
    secondResultSet.next();

    assertThat(firstId).isEqualTo(id);
    assertThat(secondId).isEqualTo(id);
    assertThat(firstResultSet.getString(1)).isEqualTo(stringValueA);
    assertThat(secondResultSet.getString(1)).isEqualTo(stringValueB);
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
    //given
    SQLiteDatabase.CursorFactory nullCursorFactory = null;

    //when
    catchException(database).rawQueryWithFactory(nullCursorFactory, ANY_VALID_SQL, null, null);

    //then
    Assertions.assertThat(caughtException()).as("Null cursor factory should be overridden by default implementation").isNull();
  }

  private ResultSet executeQuery(String query) throws SQLException {
    Statement statement = shadowOf(database).getConnection().createStatement();
    return statement.executeQuery(query);
  }
}
