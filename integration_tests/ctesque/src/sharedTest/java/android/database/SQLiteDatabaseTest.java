package android.database;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertThrows;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.Suppress;
import com.google.common.base.Ascii;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Compatibility test for {@link android.database.sqlite.SQLiteDatabase} */
@RunWith(AndroidJUnit4.class)
public class SQLiteDatabaseTest {

  private SQLiteDatabase database;
  private File databasePath;

  @Before
  public void setUp() {
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
  public void tearDown() {
    database.close();
    assertThat(databasePath.delete()).isTrue();
  }

  @Test
  public void shouldGetBlobFromString() {
    String s = "this is a string";
    ContentValues values = new ContentValues();
    values.put("first_column", s);

    database.insert("table_name", null, values);

    try (Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null)) {
      assertThat(data.getCount()).isEqualTo(1);
      data.moveToFirst();
      byte[] columnBytes = data.getBlob(0);
      byte[] expected = Arrays.copyOf(s.getBytes(), s.length() + 1); // include zero-terminal
      assertThat(columnBytes).isEqualTo(expected);
    }
  }

  @Test
  public void shouldGetBlobFromNullString() {
    ContentValues values = new ContentValues();
    values.put("first_column", (String) null);
    database.insert("table_name", null, values);

    try (Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null)) {
      assertThat(data.getCount()).isEqualTo(1);
      data.moveToFirst();
      assertThat(data.getBlob(0)).isEqualTo(null);
    }
  }

  @Test
  public void shouldGetBlobFromEmptyString() {
    ContentValues values = new ContentValues();
    values.put("first_column", "");
    database.insert("table_name", null, values);

    try (Cursor data =
        database.query("table_name", new String[] {"first_column"}, null, null, null, null, null)) {
      assertThat(data.getCount()).isEqualTo(1);
      data.moveToFirst();
      assertThat(data.getBlob(0)).isEqualTo(new byte[] {0});
    }
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
      "test", Character.toString('\\'),
    };
    assertThat(database.delete("table_name", select, selectArgs)).isEqualTo(1);
  }

  @Test
  public void shouldThrowsExceptionWhenQueryingUsingExecSQL() {
    SQLiteException e = assertThrows(SQLiteException.class, () -> database.execSQL("select 1"));
    assertThat(e)
        .hasMessageThat()
        .contains("Queries can be performed using SQLiteDatabase query or rawQuery methods only.");
  }

  @Test
  public void close_withExclusiveLockingMode() {
    database.rawQuery("PRAGMA locking_mode = EXCLUSIVE", new String[0]).close();
    ContentValues values = new ContentValues();
    values.put("first_column", "");
    database.insert("table_name", null, values);
    database.close();

    database = SQLiteDatabase.openOrCreateDatabase(databasePath, null);
    database.insert("table_name", null, values);
  }

  static class MyCursorWindow extends CursorWindow {
    public MyCursorWindow(String name) {
      super(name);
    }

    /** Make the finalize method public for testing purposes. */
    @SuppressWarnings("Finalize")
    @Override
    public void finalize() throws Throwable {
      super.finalize();
    }
  }

  // TODO(hoisie): This test crashes in emulators, enable when it is fixed in Android.
  // Use Suppress here to stop it from running on emulators, but not on Robolectric
  @Suppress
  @Test
  public void cursorWindow_finalize_concurrentStressTest() throws Throwable {
    final PrintStream originalErr = System.err;
    // discard stderr output for this test to prevent CloseGuard logspam.
    System.setErr(new PrintStream(ByteStreams.nullOutputStream()));
    try {
      ExecutorService executor = Executors.newFixedThreadPool(4);
      for (int i = 0; i < 1000; i++) {
        final MyCursorWindow cursorWindow = new MyCursorWindow(String.valueOf(i));
        for (int j = 0; j < 4; j++) {
          executor.execute(
              () -> {
                try {
                  cursorWindow.finalize();
                } catch (Throwable e) {
                  throw new AssertionError(e);
                }
              });
        }
      }
      executor.shutdown();
      executor.awaitTermination(100, SECONDS);
    } finally {
      System.setErr(originalErr);
    }
  }

  @Test
  public void collate_unicode() {
    String[] names = new String[] {"aaa", "abc", "ABC", "bbb"};
    for (String name : names) {
      ContentValues values = new ContentValues();
      values.put("name", name);
      database.insert("table_name", null, values);
    }
    Cursor c =
        database.rawQuery("SELECT name from table_name ORDER BY name COLLATE UNICODE ASC", null);
    c.moveToFirst();
    ArrayList<String> sorted = new ArrayList<>();
    while (!c.isAfterLast()) {
      sorted.add(c.getString(0));
      c.moveToNext();
    }
    c.close();
    assertThat(sorted).containsExactly("aaa", "abc", "ABC", "bbb").inOrder();
  }

  @Test
  public void regex_selection() {
    ContentValues values = new ContentValues();
    values.put("first_column", "test");
    database.insert("table_name", null, values);
    String select = "first_column regexp ?";
    String[] selectArgs = {
      "test",
    };
    assertThat(database.delete("table_name", select, selectArgs)).isEqualTo(1);
  }

  @Test
  @SdkSuppress(minSdkVersion = M) // This test fails on emulators for SDKs 21 and 22
  public void fts4() {
    database.execSQL(
        "CREATE VIRTUAL TABLE documents USING fts4 ("
            + "id INTEGER PRIMARY KEY, "
            + "title TEXT, "
            + "content TEXT"
            + ")");
    ContentValues values = new ContentValues();
    values.put("title", "Title1");
    values.put("content", "Hello World");
    database.insert("documents", null, values);

    String[] columns = {"id"};
    Cursor results =
        database.query(
            "documents",
            columns,
            "documents MATCH 'content:*Wor* OR title:*Wor*'",
            null,
            null,
            null,
            null);
    assertThat(results.getCount()).isEqualTo(1);
    results.close();

    // Android does not support parenthesis in MATCH clauses
    // See https://github.com/robolectric/robolectric/issues/8495
    results =
        database.query(
            "documents",
            columns,
            "documents MATCH '(content:*Wor* OR title:*Wor*)'",
            null,
            null,
            null,
            null);
    assertThat(results.getCount()).isEqualTo(0);
    results.close();
  }

  @Test
  public void uniqueConstraintViolation_errorMessage() {
    database.execSQL(
        "CREATE TABLE my_table(\n"
            + "  _id INTEGER PRIMARY KEY AUTOINCREMENT, \n"
            + "  unique_column TEXT UNIQUE\n"
            + ");\n");
    ContentValues values = new ContentValues();
    values.put("unique_column", "test");
    database.insertOrThrow("my_table", null, values);
    SQLiteConstraintException exception =
        assertThrows(
            SQLiteConstraintException.class,
            () -> database.insertOrThrow("my_table", null, values));
    assertThat(exception)
        .hasMessageThat()
        .startsWith("UNIQUE constraint failed: my_table.unique_column");
    assertThat(exception).hasMessageThat().contains("code 2067");
  }
}
