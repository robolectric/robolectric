package org.robolectric.shadows;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class CursorWindowTest {

  @Test
  public void shouldCreateWindowWithName() throws Exception {
    CursorWindow window = new CursorWindow("name");
    assertThat(window.getName()).isEqualTo("name");
  }

  @Test
  public void shouldFillWindowWithCursor() throws Exception {
    CursorWindow window = new CursorWindow("name");
    MatrixCursor testCursor = new MatrixCursor(new String[] { "a", "b", "c", "d"});
    testCursor.addRow(new Object[] { 12, "hello", null, new byte[] {(byte) 0xba, (byte) 0xdc, (byte) 0xaf, (byte) 0xfe} });
    testCursor.addRow(new Object[] { 34, "baz",   1.2,  null  });

    DatabaseUtils.cursorFillWindow(testCursor, 0, window);

    assertThat(window.getNumRows()).isEqualTo(2);

    assertThat(window.getString(0, 1)).isEqualTo("hello");
    assertThat(window.getInt(0, 0)).isEqualTo(12);
    assertThat(window.getString(0, 2)).isNull();
    assertThat(window.getBlob(0, 3)).isEqualTo(new byte[] {(byte) 0xba, (byte) 0xdc, (byte) 0xaf, (byte) 0xfe});

    assertThat(window.getString(1, 1)).isEqualTo("baz");
    assertThat(window.getInt(1, 0)).isEqualTo(34);
    assertThat(window.getFloat(1, 2)).isEqualTo(1.2f);
  }
}
