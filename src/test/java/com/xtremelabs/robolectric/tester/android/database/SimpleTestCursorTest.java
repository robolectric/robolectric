package com.xtremelabs.robolectric.tester.android.database;

import android.content.ContentResolver;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowContentResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SimpleTestCursorTest {
    private Uri uri;
    private SimpleTestCursor cursor;
    private ContentResolver contentResolver;

    @Before
    public void setup() throws Exception {
        contentResolver = Robolectric.application.getContentResolver();
        ShadowContentResolver shadowContentResolver = shadowOf(contentResolver);
        uri = Uri.parse("http://foo");
        cursor = new SimpleTestCursor();
        shadowContentResolver.setCursor(uri, cursor);
        ArrayList<String> columnNames = new ArrayList<String>();
        columnNames.add("stringColumn");
        columnNames.add("longColumn");
        cursor.setColumnNames(columnNames);
    }

    @Test
    public void doingQueryShouldMakeQueryParamsAvailable() throws Exception {
        contentResolver.query(uri, new String[]{"projection"}, "selection", new String[]{"selection"}, "sortOrder");
        assertThat(cursor.uri, equalTo(uri));
        assertThat(cursor.projection[0], equalTo("projection"));
        assertThat(cursor.selection, equalTo("selection"));
        assertThat(cursor.selectionArgs[0], equalTo("selection"));
        assertThat(cursor.sortOrder, equalTo("sortOrder"));
    }

    @Test
    public void canGetStringsAndLongs() throws Exception {
        cursor.setResults(new Object[][]{new Object[]{"aString", 1234L}});
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getString(cursor.getColumnIndex("stringColumn")), equalTo("aString"));
        assertThat(cursor.getLong(cursor.getColumnIndex("longColumn")), equalTo(1234L));
    }

    @Test
    public void moveToNextAdvancesToNextRow() throws Exception {
        cursor.setResults(new Object[][] { new Object[] { "aString", 1234L }, new Object[] { "anotherString", 5678L }});
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getString(cursor.getColumnIndex("stringColumn")), equalTo("anotherString"));
        assertThat(cursor.getLong(cursor.getColumnIndex("longColumn")), equalTo(5678L));
    }

    @Test
    public void closeIsRemembered() throws Exception {
        cursor.close();
        assertThat(cursor.getCloseWasCalled(), equalTo(true));
    }
}
