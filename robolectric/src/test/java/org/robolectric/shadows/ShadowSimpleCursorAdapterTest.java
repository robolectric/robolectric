package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorAdapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowSimpleCursorAdapterTest {

  @Test
  public void testChangeCursor() {
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(RuntimeEnvironment.application, 1, null, new String[]{"name"}, new int[]{2}, 0);

    Cursor cursor = setUpDatabase();

    adapter.changeCursor(cursor);

    assertThat(adapter.getCursor()).isSameAs(cursor);
  }

  @Test
  public void testSwapCursor() {
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(RuntimeEnvironment.application, 1, null, new String[]{"name"}, new int[]{2}, 0);

    Cursor cursor = setUpDatabase();

    adapter.swapCursor(cursor);

    assertThat(adapter.getCursor()).isSameAs(cursor);
  }

  @Test
  public void testSwapCursorToNull() {
    SimpleCursorAdapter adapter = new SimpleCursorAdapter(RuntimeEnvironment.application, 1, null, new String[]{"name"}, new int[]{2}, 0);

    Cursor cursor = setUpDatabase();

    adapter.swapCursor(cursor);
    adapter.swapCursor(null);

    assertThat(adapter.getCursor()).isNull();
  }

  private Cursor setUpDatabase() {
    SQLiteDatabase database = SQLiteDatabase.create(null);
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
    return database.rawQuery(sql, null);
  }
}
