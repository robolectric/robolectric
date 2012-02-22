package com.xtremelabs.robolectric.shadows;

import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class MatrixCursorTest {
    @Test
    public void shouldAddRows() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        cursor.addRow(new Object[] { "foo", 10L, 0.1f });
        cursor.addRow(new Object[] { "baz", 20L, null });
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
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        
        assertThat(cursor.getColumnCount(), equalTo(3));
        
        assertThat(cursor.getColumnName(0), equalTo("a"));
        assertThat(cursor.getColumnName(1), equalTo("b"));
        assertThat(cursor.getColumnName(2), equalTo("c"));
        
        assertThat(cursor.getColumnNames(), equalTo(new String[] { "a", "b", "c"} ));

        assertThat(cursor.getColumnIndex("b"), equalTo(1));
        assertThat(cursor.getColumnIndex("z"), equalTo(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldDefineGetColumnNameOrThrow() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        cursor.getColumnIndexOrThrow("z");
    }

    @Test(expected = CursorIndexOutOfBoundsException.class)
    public void shouldThrowIndexOutOfBoundsExceptionWithoutData() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        cursor.getString(0);
    }

    @Test(expected = CursorIndexOutOfBoundsException.class)
    public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumn() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        cursor.addRow(new Object[] { "foo", 10L, 0.1f });
        cursor.getString(3);
    }

    @Test(expected = CursorIndexOutOfBoundsException.class)
    public void shouldThrowIndexOutOfBoundsExceptionForInvalidColumnLastRow() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[] { "a", "b", "c"});
        cursor.addRow(new Object[] { "foo", 10L, 0.1f });
        cursor.moveToFirst();
        cursor.moveToNext();
        cursor.getString(0);
    }
}
