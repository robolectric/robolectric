package org.robolectric.shadows;

import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class MatrixCursorTest {

  private MatrixCursor singleColumnSingleNullValueMatrixCursor;

  @Before
  public void setUp() throws Exception {
    singleColumnSingleNullValueMatrixCursor = new MatrixCursor(new String[]{"a"});
    singleColumnSingleNullValueMatrixCursor.addRow(new Object[]{null});
    singleColumnSingleNullValueMatrixCursor.moveToFirst();
  }

  @Test
  public void shouldAddObjectArraysAsRows() throws Exception {
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
  }

  @Test
  public void shouldAddIterablesAsRows() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(Arrays.asList(new Object[]{"foo", 10L, 0.1f}));
    cursor.addRow(Arrays.asList(new Object[]{"baz", 20L, null}));
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
  }

  @Test
  public void shouldDefineColumnNames() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});

    assertThat(cursor.getColumnCount()).isEqualTo(3);

    assertThat(cursor.getColumnName(0)).isEqualTo("a");
    assertThat(cursor.getColumnName(1)).isEqualTo("b");
    assertThat(cursor.getColumnName(2)).isEqualTo("c");

    assertThat(cursor.getColumnNames()).isEqualTo(new String[]{"a", "b", "c"});

    assertThat(cursor.getColumnIndex("b")).isEqualTo(1);
    assertThat(cursor.getColumnIndex("z")).isEqualTo(-1);
  }

  @Test
  public void shouldDefineGetBlob() throws Exception {
    byte[] blob = {1, 2, 3, 4};

    MatrixCursor cursor = new MatrixCursor(new String[]{"a"});
    cursor.addRow(new Object[]{blob});
    assertTrue(cursor.moveToFirst());

    assertThat(cursor.getBlob(0)).isEqualTo(blob);
  }

  @Test
  public void shouldAllowTypeFlexibility() throws Exception {
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
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldDefineGetColumnNameOrThrow() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.getColumnIndexOrThrow("z");
  }

  @Test(expected = CursorIndexOutOfBoundsException.class)
  public void shouldThrowIndexOutOfBoundsExceptionWithoutData() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.getString(0);
  }

  @Test(expected = CursorIndexOutOfBoundsException.class)
  public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumn() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{"foo", 10L, 0.1f});
    cursor.getString(3);
  }

  @Test(expected = CursorIndexOutOfBoundsException.class)
  public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumnLastRow() throws Exception {
    MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
    cursor.addRow(new Object[]{"foo", 10L, 0.1f});
    cursor.moveToFirst();
    cursor.moveToNext();
    cursor.getString(0);
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
