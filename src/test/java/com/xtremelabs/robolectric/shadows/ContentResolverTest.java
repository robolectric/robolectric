package com.xtremelabs.robolectric.shadows;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.database.TestCursor;

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
    public void insert_shouldTrackInsertStatements() throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("foo", "bar");
        contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
        assertThat(shadowContentResolver.getInsertStatements().size(), is(1));
        assertThat(shadowContentResolver.getInsertStatements().get(0).getUri(), equalTo(EXTERNAL_CONTENT_URI));
        assertThat(shadowContentResolver.getInsertStatements().get(0).getContentValues().getAsString("foo"), equalTo("bar"));
        
        contentValues = new ContentValues();
        contentValues.put("hello", "world");
        contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
        assertThat(shadowContentResolver.getInsertStatements().size(), is(2));
        assertThat(shadowContentResolver.getInsertStatements().get(1).getContentValues().getAsString("hello"), equalTo("world"));
    }
    
    @Test
    public void insert_shouldTrackUpdateStatements() throws Exception {
        ContentValues contentValues = new ContentValues();
        contentValues.put("foo", "bar");
        contentResolver.update(EXTERNAL_CONTENT_URI, contentValues, "robolectric", new String[] { "awesome" });
        assertThat(shadowContentResolver.getUpdateStatements().size(), is(1));
        assertThat(shadowContentResolver.getUpdateStatements().get(0).getUri(), equalTo(EXTERNAL_CONTENT_URI));
        assertThat(shadowContentResolver.getUpdateStatements().get(0).getContentValues().getAsString("foo"), equalTo("bar"));
        assertThat(shadowContentResolver.getUpdateStatements().get(0).getWhere(), equalTo("robolectric"));
        assertThat(shadowContentResolver.getUpdateStatements().get(0).getSelectionArgs(), equalTo(new String[] { "awesome" }));
        
        contentValues = new ContentValues();
        contentValues.put("hello", "world");
        contentResolver.update(EXTERNAL_CONTENT_URI, contentValues, null, null);
        assertThat(shadowContentResolver.getUpdateStatements().size(), is(2));
        assertThat(shadowContentResolver.getUpdateStatements().get(1).getUri(), equalTo(EXTERNAL_CONTENT_URI));
        assertThat(shadowContentResolver.getUpdateStatements().get(1).getContentValues().getAsString("hello"), equalTo("world"));
        assertThat(shadowContentResolver.getUpdateStatements().get(1).getWhere(), nullValue());
        assertThat(shadowContentResolver.getUpdateStatements().get(1).getSelectionArgs(), nullValue());
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
    public void delete_shouldTrackDeletedStatements() {
        assertThat(shadowContentResolver.getDeleteStatements().size(), equalTo(0));

        assertThat(contentResolver.delete(uri21, "id", new String[] { "5" }), equalTo(1));
        assertThat(shadowContentResolver.getDeleteStatements().size(), equalTo(1));
        assertThat(shadowContentResolver.getDeleteStatements().get(0).getUri(), equalTo(uri21));
        assertThat(shadowContentResolver.getDeleteStatements().get(0).getWhere(), equalTo("id"));
        assertThat(shadowContentResolver.getDeleteStatements().get(0).getSelectionArgs()[0], equalTo("5"));

        assertThat(contentResolver.delete(uri21, "foo", new String[] { "bar" }), equalTo(1));
        assertThat(shadowContentResolver.getDeleteStatements().size(), equalTo(2));
        assertThat(shadowContentResolver.getDeleteStatements().get(1).getUri(), equalTo(uri21));
        assertThat(shadowContentResolver.getDeleteStatements().get(1).getWhere(), equalTo("foo"));
        assertThat(shadowContentResolver.getDeleteStatements().get(1).getSelectionArgs()[0], equalTo("bar"));
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
    
    @Test
    public void applyBatch() throws RemoteException, OperationApplicationException {
        String authority = "com.xtremelabs.robolectric";
        ArrayList<ContentProviderOperation> resultOperations = shadowContentResolver.getContentProviderOperations(authority);
        assertThat(resultOperations, notNullValue());
        assertThat(resultOperations.size(), is(0));
        
        ContentProviderResult[] contentProviderResults = new ContentProviderResult[] {
                new ContentProviderResult(1),
                new ContentProviderResult(1),
        };
        shadowContentResolver.setContentProviderResult(contentProviderResults);
        Uri uri = Uri.parse("content://com.xtremelabs.robolectric");
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(uri)
                .withValue("column1", "foo")
                .withValue("column2", 5)
                .build());
        operations.add(ContentProviderOperation.newUpdate(uri)
                .withSelection("id_column", new String[] { "99" })
                .withValue("column1", "bar")
                .build());
        operations.add(ContentProviderOperation.newDelete(uri)
                .withSelection("id_column", new String[] { "11" })
                .build());
        ContentProviderResult[] result = contentResolver.applyBatch(authority, operations);
        
        resultOperations = shadowContentResolver.getContentProviderOperations(authority);
        assertThat(resultOperations, equalTo(operations));
        assertThat(result, equalTo(contentProviderResults));
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
