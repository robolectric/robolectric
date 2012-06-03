package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;

@RunWith(WithTestDefaultsRunner.class)
public class MatrixCursorTest {

    private MatrixCursor singleColumnSingleNullValueMatrixCursor;

    @Before
    public void setUp() throws Exception {
        singleColumnSingleNullValueMatrixCursor = new MatrixCursor(new String[]{"a"});
        singleColumnSingleNullValueMatrixCursor.addRow(new Object[]{null});
        singleColumnSingleNullValueMatrixCursor.moveToFirst();
    }

    @Test
    public void shouldAddRows() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
        cursor.addRow(new Object[]{"foo", 10L, 0.1f});
        cursor.addRow(new Object[]{"baz", 20L, null});
        assertThat(cursor.getCount(), equalTo(2));

        assertTrue(cursor.moveToFirst());

        assertThat(cursor.getString(0), equalTo("foo"));
        assertThat(cursor.getLong(1), equalTo(10L));
        assertThat(cursor.getFloat(2), equalTo(0.1f));

        assertTrue(cursor.moveToNext());

        assertThat(cursor.getString(0), equalTo("baz"));
        assertThat(cursor.getLong(1), equalTo(20L));
        assertTrue(cursor.isNull(2));

        assertFalse(cursor.moveToNext());
    }

    @Test
    public void shouldDefineColumnNames() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});

        assertThat(cursor.getColumnCount(), equalTo(3));

        assertThat(cursor.getColumnName(0), equalTo("a"));
        assertThat(cursor.getColumnName(1), equalTo("b"));
        assertThat(cursor.getColumnName(2), equalTo("c"));

        assertThat(cursor.getColumnNames(), equalTo(new String[]{"a", "b", "c"}));

        assertThat(cursor.getColumnIndex("b"), equalTo(1));
        assertThat(cursor.getColumnIndex("z"), equalTo(-1));
    }

    @Test
    public void shouldDefineGetBlob() throws Exception {
        byte[] blob = {1, 2, 3, 4};

        MatrixCursor cursor = new MatrixCursor(new String[]{"a"});
        cursor.addRow(new Object[]{blob});
        assertTrue(cursor.moveToFirst());

        assertThat(cursor.getBlob(0), equalTo(blob));
    }

    @Test
    public void shouldAllowTypeFlexibility() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c"});
        cursor.addRow(new Object[]{42, 3.3});
        assertTrue(cursor.moveToFirst());

        assertThat(cursor.getString(0), equalTo("42"));
        assertThat(cursor.getShort(0), equalTo((short) 42));
        assertThat(cursor.getInt(0), equalTo(42));
        assertThat(cursor.getLong(0), equalTo(42L));
        assertThat(cursor.getFloat(0), equalTo(42.0F));
        assertThat(cursor.getDouble(0), equalTo(42.0));

        assertThat(cursor.getString(1), equalTo("3.3"));
        assertThat(cursor.getShort(1), equalTo((short) 3));
        assertThat(cursor.getInt(1), equalTo(3));
        assertThat(cursor.getLong(1), equalTo(3L));
        assertThat(cursor.getFloat(1), equalTo(3.3F));
        assertThat(cursor.getDouble(1), equalTo(3.3));
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
        assertThat(singleColumnSingleNullValueMatrixCursor.getString(0), is(nullValue()));
    }

    @Test
    public void returnsZeroWhenGettingIntFromNullColumn() {
        assertThat(singleColumnSingleNullValueMatrixCursor.getInt(0), is(equalTo(0)));
    }

    @Test
    public void returnsZeroWhenGettingLongFromNullColumn() {
        assertThat(singleColumnSingleNullValueMatrixCursor.getLong(0), is(equalTo(0L)));
    }

    @Test
    public void returnsZeroWhenGettingShortFromNullColumn() {
        assertThat(singleColumnSingleNullValueMatrixCursor.getShort(0), is(equalTo((short) 0)));
    }

    @Test
    public void returnsZeroWhenGettingFloatFromNullColumn() {
        assertThat(singleColumnSingleNullValueMatrixCursor.getFloat(0), is(equalTo(0.0f)));
    }

    @Test
    public void returnsZeroWhenGettingDoubleFromNullColumn() {
        assertThat(singleColumnSingleNullValueMatrixCursor.getDouble(0), is(equalTo(0.0)));
    }
}
