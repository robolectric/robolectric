package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowSimpleCursorAdapterTest {

  private Application context;
  private SQLiteDatabase database;
  private Cursor cursor;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();

    database = SQLiteDatabase.create(null);
    database.execSQL("CREATE TABLE table_name(_id INT PRIMARY KEY, name VARCHAR(255));");
    String[] inserts = {
      "INSERT INTO table_name (_id, name) VALUES(1234, 'Chuck');",
      "INSERT INTO table_name (_id, name) VALUES(1235, 'Julie');",
      "INSERT INTO table_name (_id, name) VALUES(1236, 'Chris');",
      "INSERT INTO table_name (_id, name) VALUES(1237, 'Brenda');",
      "INSERT INTO table_name (_id, name) VALUES(1238, 'Jane');"
    };

    for (String insert : inserts) {
      database.execSQL(insert);
    }

    String sql = "SELECT * FROM table_name;";
    cursor = database.rawQuery(sql, null);
  }

  @After
  public void tearDown() {
    database.close();
    cursor.close();
  }

  @Test
  public void testChangeCursor() {
    SimpleCursorAdapter adapter =
        new SimpleCursorAdapter(context, 1, null, new String[] {"name"}, new int[] {2}, 0);

    adapter.changeCursor(cursor);

    assertThat(adapter.getCursor()).isSameInstanceAs(cursor);
  }

  @Test
  public void testSwapCursor() {
    SimpleCursorAdapter adapter =
        new SimpleCursorAdapter(context, 1, null, new String[] {"name"}, new int[] {2}, 0);

    adapter.swapCursor(cursor);

    assertThat(adapter.getCursor()).isSameInstanceAs(cursor);
  }

  @Test
  public void testSwapCursorToNull() {
    SimpleCursorAdapter adapter =
        new SimpleCursorAdapter(context, 1, null, new String[] {"name"}, new int[] {2}, 0);

    adapter.swapCursor(cursor);
    adapter.swapCursor(null);

    assertThat(adapter.getCursor()).isNull();
  }
}
