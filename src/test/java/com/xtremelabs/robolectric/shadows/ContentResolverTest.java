package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.database.TestCursor;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ContentResolverTest {

    private ContentResolver contentResolver;
    private ShadowContentResolver shadowContentResolver;
    private Uri uri21;
    private Uri uri22;

    @Before
    public void setUp() throws Exception {
        contentResolver = new Activity().getContentResolver();
        shadowContentResolver = shadowOf(contentResolver);
        uri21 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/21");
        uri22 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/22");
    }

    @Test
    public void insert_shouldReturnIncreasingUris() throws Exception {
        shadowContentResolver.setNextDatabaseIdForInserts(21);

        assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues()), equalTo(uri21));
        assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues()), equalTo(uri22));
    }

    @Test
    public void delete_shouldTrackDeletedUris() throws Exception {
        assertThat(shadowContentResolver.getDeletedUris().size(), equalTo(0));

        assertThat(contentResolver.delete(uri21, null, null), equalTo(1));
        assertThat(shadowContentResolver.getDeletedUris(), hasItem(uri21));
        assertThat(shadowContentResolver.getDeletedUris().size(), equalTo(1));

        assertThat(contentResolver.delete(uri22, null, null), equalTo(1));
        assertThat(shadowContentResolver.getDeletedUris(), hasItem(uri22));
        assertThat(shadowContentResolver.getDeletedUris().size(), equalTo(2));
    }

    @Test
    public void query_shouldReturnTheCursorThatWasSet() throws Exception {
        assertNull(shadowContentResolver.query(null, null, null, null, null));
        TestCursor cursor = new TestCursor();
        shadowContentResolver.setCursor(cursor);
        assertThat((TestCursor) shadowContentResolver.query(null, null, null, null, null),
                sameInstance(cursor));
    }

    @Test
    public void openInputStream_shouldReturnAnInputStream() throws Exception {
        assertThat(contentResolver.openInputStream(uri21), CoreMatchers.instanceOf(InputStream.class));
    }

}
