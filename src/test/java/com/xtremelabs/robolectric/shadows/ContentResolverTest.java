package com.xtremelabs.robolectric.shadows;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.content.*;
import android.os.Bundle;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.database.TestCursor;

@RunWith(WithTestDefaultsRunner.class)
public class ContentResolverTest {
    static final String AUTHORITY = "com.xtremelabs.robolectric";

    private ContentResolver contentResolver;
    private ShadowContentResolver shadowContentResolver;
    private Uri uri21;
    private Uri uri22;
    private Account a, b;

    @Before
    public void setUp() throws Exception {
        contentResolver = new Activity().getContentResolver();
        shadowContentResolver = shadowOf(contentResolver);
        uri21 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/21");
        uri22 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/22");

        a = new Account("a", "type");
        b = new Account("b", "type");
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
    public void openOutputStream_shouldReturnAnOutputStream() throws Exception {
        assertThat(contentResolver.openOutputStream(uri21), CoreMatchers.instanceOf(OutputStream.class));
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
        ArrayList<ContentProviderOperation> resultOperations = shadowContentResolver.getContentProviderOperations(AUTHORITY);
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
        ContentProviderResult[] result = contentResolver.applyBatch(AUTHORITY, operations);

        resultOperations = shadowContentResolver.getContentProviderOperations(AUTHORITY);
        assertThat(resultOperations, equalTo(operations));
        assertThat(result, equalTo(contentProviderResults));
    }

    @Test
    public void shouldKeepTrackOfSyncRequests() throws Exception {
        ShadowContentResolver.Status status = ShadowContentResolver.getStatus(a, AUTHORITY, true);
        assertNotNull(status);
        assertThat(status.syncRequests, equalTo(0));
        ContentResolver.requestSync(a, AUTHORITY, new Bundle());
        assertThat(status.syncRequests, equalTo(1));
        assertNotNull(status.syncExtras);
    }

    @Test
    public void shouldSetIsSyncable() throws Exception {
        assertThat(ContentResolver.getIsSyncable(a, AUTHORITY), equalTo(-1));
        assertThat(ContentResolver.getIsSyncable(b, AUTHORITY), equalTo(-1));
        ContentResolver.setIsSyncable(a, AUTHORITY, 1);
        ContentResolver.setIsSyncable(b, AUTHORITY, 2);
        assertThat(ContentResolver.getIsSyncable(a, AUTHORITY), equalTo(1));
        assertThat(ContentResolver.getIsSyncable(b, AUTHORITY), equalTo(2));
    }

    @Test
    public void shouldSetSyncAutomatically() throws Exception {
        assertFalse(ContentResolver.getSyncAutomatically(a, AUTHORITY));
        ContentResolver.setSyncAutomatically(a, AUTHORITY, true);
        assertTrue(ContentResolver.getSyncAutomatically(a, AUTHORITY));
    }

    @Test
    public void shouldAddPeriodicSync() throws Exception {
        ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000l);
        ShadowContentResolver.Status status = ShadowContentResolver.getStatus(a, AUTHORITY);
        assertNotNull(status);
        assertThat(status.syncs.size(), is(1));
        assertThat(status.syncs.get(0).period, is(6000l));
        assertNotNull(status.syncs.get(0).extras);
    }

    @Test
    public void shouldRemovePeriodSync() throws Exception {
        ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000l);
        ContentResolver.removePeriodicSync(a, AUTHORITY, new Bundle());
        assertThat(ShadowContentResolver.getStatus(a, AUTHORITY).syncs.size(), is(0));
    }

    @Test
    public void shouldGetPeriodSyncs() throws Exception {
        assertThat(ContentResolver.getPeriodicSyncs(a, AUTHORITY).size(), is(0));
        ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000l);

        List<PeriodicSync> syncs = ContentResolver.getPeriodicSyncs(a, AUTHORITY);
        assertThat(syncs.size(), is(1));

        PeriodicSync first = syncs.get(0);
        assertThat(first.account, equalTo(a));
        assertThat(first.authority, equalTo(AUTHORITY));
        assertThat(first.period, equalTo(6000l));
        assertNotNull(first.extras);
    }

    @Test
    public void shouldValidateSyncExtras() throws Exception {
        Bundle bundle = new Bundle();
        bundle.putString("foo", "strings");
        bundle.putLong("long", 10l);
        bundle.putDouble("double", 10.0d);
        bundle.putFloat("float", 10.0f);
        bundle.putInt("int", 10);
        bundle.putParcelable("account", a);
        ContentResolver.validateSyncExtrasBundle(bundle);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateSyncExtrasAndThrow() throws Exception {
        Bundle bundle = new Bundle();
        bundle.putParcelable("intent", new Intent());
        ContentResolver.validateSyncExtrasBundle(bundle);
    }

    @Test
    public void shouldSetMasterSyncAutomatically() throws Exception {
        assertFalse(ContentResolver.getMasterSyncAutomatically());
        ContentResolver.setMasterSyncAutomatically(true);
        assertTrue(ContentResolver.getMasterSyncAutomatically());
    }

    @Test
    public void shouldDelegateCallsToRegisteredProvider() throws Exception {
        ShadowContentResolver.registerProvider(AUTHORITY, new ContentProvider() {
            @Override public boolean onCreate() {
                return false;
            }
            @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
                return new TestCursor();
            }
            @Override public Uri insert(Uri uri, ContentValues values) {
                return null;
            }
            @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
                return -1;
            }
            @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
                return -1;
            }
            @Override public String getType(Uri uri) {
                return null;
            }
        });
        final Uri uri = Uri.parse("content://"+AUTHORITY+"/some/path");
        final Uri unrelated = Uri.parse("content://unrelated/some/path");

        assertNotNull(contentResolver.query(uri, null, null, null, null));
        assertNull(contentResolver.insert(uri, new ContentValues()));
        assertThat(contentResolver.delete(uri, null, null), is(-1));
        assertThat(contentResolver.update(uri, new ContentValues(), null, null), is(-1));

        assertNull(contentResolver.query(unrelated, null, null, null, null));
        assertNotNull(contentResolver.insert(unrelated, new ContentValues()));
        assertThat(contentResolver.delete(unrelated, null, null), is(1));
        assertThat(contentResolver.update(unrelated, new ContentValues(), null, null), is(0));
    }

    static class QueryParamTrackingTestCursor extends TestCursor {
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
