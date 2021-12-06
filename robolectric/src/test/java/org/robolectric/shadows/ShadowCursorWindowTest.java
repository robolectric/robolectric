package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.database.CursorWindow;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowCursorWindowTest {

  @Test
  public void shouldCreateWindowWithName() {
    CursorWindow window = new CursorWindow("name");
    assertThat(window.getName()).isEqualTo("name");
    window.close();
  }

  @Test
  public void shouldFillWindowWithCursor() {
    CursorWindow window = new CursorWindow("name");
    MatrixCursor testCursor = new MatrixCursor(new String[] { "a", "b", "c", "d"});
    testCursor.addRow(
        new Object[] {
          12, "hello", null, new byte[] {(byte) 0xba, (byte) 0xdc, (byte) 0xaf, (byte) 0xfe}
        });
    testCursor.addRow(new Object[] { 34, "baz",   1.2,  null  });
    testCursor.addRow(new Object[] { 46, "foo",   2.4,  new byte[]{}  });

    DatabaseUtils.cursorFillWindow(testCursor, 0, window);

    assertThat(window.getNumRows()).isEqualTo(3);

    assertThat(window.getInt(0, 0)).isEqualTo(12);
    assertThat(window.getString(0, 1)).isEqualTo("hello");
    assertThat(window.getString(0, 2)).isNull();
    assertThat(window.getBlob(0, 3)).isEqualTo(new byte[] {(byte) 0xba, (byte) 0xdc, (byte) 0xaf, (byte) 0xfe});

    assertThat(window.getInt(1, 0)).isEqualTo(34);
    assertThat(window.getString(1, 1)).isEqualTo("baz");
    assertThat(window.getFloat(1, 2)).isEqualTo(1.2f);
    assertThat(window.getBlob(1, 3)).isEqualTo(null);

    assertThat(window.getBlob(2, 3)).isEqualTo(new byte[]{});
    testCursor.close();
    window.close();
  }

  /** Real Android will crash in native code if putBlob is called with a null value. */
  @Test
  public void putBlobNullValueThrowsNPE() {
    CursorWindow cursorWindow = new CursorWindow("test");
    cursorWindow.allocRow();
    assertThrows(NullPointerException.class, () -> cursorWindow.putBlob(null, 0, 0));
    cursorWindow.close();
  }

  /** Real Android will crash in native code if putString is called with a null value. */
  @Test
  public void putStringNullValueThrowsNPE() {
    CursorWindow cursorWindow = new CursorWindow("test");
    cursorWindow.allocRow();
    assertThrows(NullPointerException.class, () -> cursorWindow.putString(null, 0, 0));
    cursorWindow.close();
  }
}
