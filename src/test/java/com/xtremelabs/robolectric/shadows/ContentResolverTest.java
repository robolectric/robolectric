package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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
    public void query__shouldReturnSpecificCursorsForSpecificUris() throws Exception {
        assertNull(shadowContentResolver.query(uri21, null, null, null, null));
        assertNull(shadowContentResolver.query(uri22, null, null, null, null));

        TestCursor cursor21 = new TestCursor();
        TestCursor cursor22 = new TestCursor();
        shadowContentResolver.setCursor(uri21, cursor21);
        shadowContentResolver.setCursor(uri22, cursor22);

        assertThat((TestCursor) shadowContentResolver.query(uri21, null, null, null, null),
                sameInstance(cursor21));
        assertThat((TestCursor) shadowContentResolver.query(uri22, null, null, null, null),
                sameInstance(cursor22));
    }

    @Test
    public void query__shouldKnowWhatItsParamsWere() throws Exception {
        String[] projection = {};
        String selection = "select";
        String[] selectionArgs = {};
        String sortOrder = "order";

        QueryParamTrackingTestCursor testCursor = new QueryParamTrackingTestCursor();

        shadowContentResolver.setCursor(testCursor);
        Cursor cursor = shadowContentResolver.query(uri21, projection, selection, selectionArgs, sortOrder);
        assertThat((QueryParamTrackingTestCursor)cursor, equalTo(testCursor));
        assertThat(testCursor.uri, equalTo(uri21));
        assertThat(testCursor.projection, equalTo(projection));
        assertThat(testCursor.selection, equalTo(selection));
        assertThat(testCursor.selectionArgs, equalTo(selectionArgs));
        assertThat(testCursor.sortOrder, equalTo(sortOrder));
    }

    @Test
    public void openInputStream_shouldReturnAnInputStream() throws Exception {
        assertThat(contentResolver.openInputStream(uri21), CoreMatchers.instanceOf(InputStream.class));
    }

    @Test
    public void shouldTrackNotifiedUris() throws Exception {
        contentResolver.notifyChange(Uri.parse("foo"), null, true);
        contentResolver.notifyChange(Uri.parse("bar"), null);

        assertThat(shadowContentResolver.getNotifiedUris().size(), equalTo(2));
        ShadowContentResolver.NotifiedUri uri = shadowContentResolver.getNotifiedUris().get(0);

        assertThat(uri.uri.toString(), equalTo("foo"));
        assertTrue(uri.syncToNetwork);
        assertNull(uri.observer);

        uri = shadowContentResolver.getNotifiedUris().get(1);

        assertThat(uri.uri.toString(), equalTo("bar"));
        assertFalse(uri.syncToNetwork);
        assertNull(uri.observer);
    }

    class QueryParamTrackingTestCursor extends TestCursor {
        public Uri uri;
        public String[] projection;
        public String selection;
        public String[] selectionArgs;
        public String sortOrder;

        @Override
        public void setQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            this.uri = uri;
            this.projection = projection;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.sortOrder = sortOrder;
        }
    }
}
