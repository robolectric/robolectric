package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowMatrixCursorTest {

  private MatrixCursor singleColumnSingleNullValueMatrixCursor;

  @Before
  public void setUp() throws Exception {
    singleColumnSingleNullValueMatrixCursor = new MatrixCursor(new String[]{"a"});
    singleColumnSingleNullValueMatrixCursor.addRow(new Object[]{null});
    singleColumnSingleNullValueMatrixCursor.moveToFirst();
  }

  @After
  public void tearDown() {
    singleColumnSingleNullValueMatrixCursor.close();
  }

  @Test
  public void shouldAddObjectArraysAsRows() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{"foo", 10L, 0.1f});
    cursor.addRow(new Object[]{"baz", 20L, null});
    assertThat(cursor.getCount()).isEqualTo(2);

    assertTrue(cursor.moveToFirst());

    assertThat(cursor.getString(0)).isEqualTo("foo");
    assertThat(cursor.getLong(1)).isEqualTo(10L);
    assertThat(cursor.getFloat(2)).isEqualTo(0.1f);

    assertTrue(cursor.moveToNext());

    assertThat(cursor.getString(0)).isEqualTo("baz");
    assertThat(cursor.getLong(1)).isEqualTo(20L);
    assertTrue(cursor.isNull(2));

    assertFalse(cursor.moveToNext());
    cursor.close();
  }

  @Test
  public void shouldAddIterablesAsRows() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(Arrays.asList("foo", 10L, 0.1f));
    cursor.addRow(Arrays.asList("baz", 20L, null));
    assertThat(cursor.getCount()).isEqualTo(2);

    assertTrue(cursor.moveToFirst());

    assertThat(cursor.getString(0)).isEqualTo("foo");
    assertThat(cursor.getLong(1)).isEqualTo(10L);
    assertThat(cursor.getFloat(2)).isEqualTo(0.1f);

    assertTrue(cursor.moveToNext());

    assertThat(cursor.getString(0)).isEqualTo("baz");
    assertThat(cursor.getLong(1)).isEqualTo(20L);
    assertTrue(cursor.isNull(2));

    assertFalse(cursor.moveToNext());
    cursor.close();
  }

  @Test
  public void shouldDefineColumnNames() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});

    assertThat(cursor.getColumnCount()).isEqualTo(3);

    assertThat(cursor.getColumnName(0)).isEqualTo("a");
    assertThat(cursor.getColumnName(1)).isEqualTo("b");
    assertThat(cursor.getColumnName(2)).isEqualTo("c");

    assertThat(cursor.getColumnNames()).isEqualTo(new String[]{"a", "b", "c"});

    assertThat(cursor.getColumnIndex("b")).isEqualTo(1);
    assertThat(cursor.getColumnIndex("z")).isEqualTo(-1);
    cursor.close();
  }

  @Test
  public void shouldDefineGetBlob() {
    byte[] blob = {1, 2, 3, 4};

    MatrixCursor cursor = new MatrixCursor(new String[]{"a"});
    cursor.addRow(new Object[]{blob});
    assertTrue(cursor.moveToFirst());

    assertThat(cursor.getBlob(0)).isEqualTo(blob);
  }

  @Test
  public void shouldAllowTypeFlexibility() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{42, 3.3, 'a'});
    assertTrue(cursor.moveToFirst());

    assertThat(cursor.getString(0)).isEqualTo("42");
    assertThat(cursor.getShort(0)).isEqualTo((short) 42);
    assertThat(cursor.getInt(0)).isEqualTo(42);
    assertThat(cursor.getLong(0)).isEqualTo(42L);
    assertThat(cursor.getFloat(0)).isEqualTo(42.0F);
    assertThat(cursor.getDouble(0)).isEqualTo(42.0);

    assertThat(cursor.getString(1)).isEqualTo("3.3");
    assertThat(cursor.getShort(1)).isEqualTo((short) 3);
    assertThat(cursor.getInt(1)).isEqualTo(3);
    assertThat(cursor.getLong(1)).isEqualTo(3L);
    assertThat(cursor.getFloat(1)).isEqualTo(3.3F);
    assertThat(cursor.getDouble(1)).isEqualTo(3.3);

    assertThat(cursor.getString(2)).isEqualTo("a");
    cursor.close();
  }

  @Test
  public void shouldDefineGetColumnNameOrThrow() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    assertThrows(IllegalArgumentException.class, () -> cursor.getColumnIndexOrThrow("z"));
    cursor.close();
  }

  @Test
  public void shouldThrowIndexOutOfBoundsExceptionWithoutData() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    assertThrows(CursorIndexOutOfBoundsException.class, () -> cursor.getString(0));
    cursor.close();
  }

  @Test
  public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumn() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{"foo", 10L, 0.1f});
    assertThrows(CursorIndexOutOfBoundsException.class, () -> cursor.getString(3));
    cursor.close();
  }

  @Test
  public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumnLastRow() {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{"foo", 10L, 0.1f});
    cursor.moveToFirst();
    cursor.moveToNext();
    assertThrows(CursorIndexOutOfBoundsException.class, () -> cursor.getString(0));
    cursor.close();
  }

  @Test
  public void returnsNullWhenGettingStringFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getString(0)).isNull();
  }

  @Test
  public void returnsZeroWhenGettingIntFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getInt(0)).isEqualTo(0);
  }

  @Test
  public void returnsZeroWhenGettingLongFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getLong(0)).isEqualTo(0L);
  }

  @Test
  public void returnsZeroWhenGettingShortFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getShort(0)).isEqualTo((short) 0);
  }

  @Test
  public void returnsZeroWhenGettingFloatFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getFloat(0)).isEqualTo(0.0f);
  }

  @Test
  public void returnsZeroWhenGettingDoubleFromNullColumn() {
    assertThat(singleColumnSingleNullValueMatrixCursor.getDouble(0)).isEqualTo(0.0);
  }
}
