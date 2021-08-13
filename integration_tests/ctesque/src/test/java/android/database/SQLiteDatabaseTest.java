package android.database;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Ascii;
import com.google.common.base.Throwables;
import java.io.File;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Compatibility test for {@link android.database.sqlite.SQLiteDatabase} */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class SQLiteDatabaseTest {

  private SQLiteDatabase database;
  private File databasePath;

  @Before
  public void setUp() throws Exception {
    databasePath = ApplicationProvider.getApplicationContext().getDatabasePath("database.db");
    databasePath.getParentFile().mkdirs();

    database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
    database.execSQL(
        "CREATE TABLE table_name (\n"
            + "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
            + "  first_column VARCHAR(255),\n"
            + "  second_column BINARY,\n"
            + "  name VARCHAR(255),\n"
            + "  big_int INTEGER\n"
            + ");");
  }

  @After
  public void tearDown() throws Exception {
    database.close();
    assertThat(databasePath.delete()).isTrue();
  }

  @Test
  public void shouldGetBlobFromString() {
    String s = "this is a string";
    ContentValues values = new ContentValues();
    values.put("first_column", s);

    database.insert("table_name", null, values);

    Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null);
    assertThat(data.getCount()).isEqualTo(1);
    data.moveToFirst();
    byte[] columnBytes = data.getBlob(0);
    byte[] expected = Arrays.copyOf(s.getBytes(), s.length() + 1); // include zero-terminal
    assertThat(columnBytes).isEqualTo(expected);
  }

  @Test
  public void shouldGetBlobFromNullString() {
    ContentValues values = new ContentValues();
    values.put("first_column", (String) null);
    database.insert("table_name", null, values);

    Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null);
    assertThat(data.getCount()).isEqualTo(1);
    data.moveToFirst();
    assertThat(data.getBlob(0)).isEqualTo(null);
  }

  @Test
  public void shouldGetBlobFromEmptyString() {
    ContentValues values = new ContentValues();
    values.put("first_column", "");
    database.insert("table_name", null, values);

    Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null);
    assertThat(data.getCount()).isEqualTo(1);
    data.moveToFirst();
    assertThat(data.getBlob(0)).isEqualTo(new byte[] {0});
  }

  @Test
  public void shouldThrowWhenForeignKeysConstraintIsViolated() {
    database.execSQL(
        "CREATE TABLE artist(\n"
            + "  artistid    INTEGER PRIMARY KEY, \n"
            + "  artistname  TEXT\n"
            + ");\n");

    database.execSQL(
        "CREATE TABLE track(\n"
            + "  trackid     INTEGER, \n"
            + "  trackname   TEXT, \n"
            + "  trackartist INTEGER,\n"
            + "  FOREIGN KEY(trackartist) REFERENCES artist(artistid)\n"
            + ");");

    database.execSQL("PRAGMA foreign_keys=ON");
    database.execSQL("INSERT into artist (artistid, artistname) VALUES (1, 'Kanye')");
    database.execSQL(
        "INSERT into track (trackid, trackname, trackartist) VALUES (1, 'Good Life', 1)");
    SQLiteConstraintException ex =
        assertThrows(SQLiteConstraintException.class, () -> database.execSQL("delete from artist"));
    assertThat(Ascii.toLowerCase(Throwables.getStackTraceAsString(ex))).contains("foreign key");
  }

  @Test
  public void shouldDeleteWithLikeEscape() {
    ContentValues values = new ContentValues();
    values.put("first_column", "test");
    database.insert("table_name", null, values);
    String select = "first_column LIKE ? ESCAPE ?";
    String[] selectArgs = {
        "test",
        Character.toString('\\'),
    };
    assertThat(database.delete("table_name", select, selectArgs)).isEqualTo(1);
  }
}
