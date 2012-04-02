package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CursorLoaderTest {
    @Test
    public void testGetters() {
        Uri uri = Uri.parse("http://robolectric.org");
        String[] projection = new String[] { "_id", "TestColumn" };
        String selection = "_id = ?";
        String[] selectionArgs = new String[] { "5" };
        String sortOrder = "_id";
        CursorLoader cursorLoader = new CursorLoader(new Activity(),
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder);
        
        assertThat(cursorLoader.getUri(), equalTo(uri));
        assertThat(cursorLoader.getProjection(), equalTo(projection));
        assertThat(cursorLoader.getSelection(), equalTo(selection));
        assertThat(cursorLoader.getSelectionArgs(), equalTo(selectionArgs));
        assertThat(cursorLoader.getSortOrder(), equalTo(sortOrder));
    }
    
    @Test
    public void testSetters() {
        Uri uri = Uri.parse("http://robolectric.org");
        String[] projection = new String[] { "_id", "TestColumn" };
        String selection = "_id = ?";
        String[] selectionArgs = new String[] { "5" };
        String sortOrder = "_id";
        CursorLoader cursorLoader = new CursorLoader(new Activity());
        cursorLoader.setUri(uri);
        cursorLoader.setProjection(projection);
        cursorLoader.setSelection(selection);
        cursorLoader.setSelectionArgs(selectionArgs);
        cursorLoader.setSortOrder(sortOrder);
        
        assertThat(cursorLoader.getUri(), equalTo(uri));
        assertThat(cursorLoader.getProjection(), equalTo(projection));
        assertThat(cursorLoader.getSelection(), equalTo(selection));
        assertThat(cursorLoader.getSelectionArgs(), equalTo(selectionArgs));
        assertThat(cursorLoader.getSortOrder(), equalTo(sortOrder));
    }
}
