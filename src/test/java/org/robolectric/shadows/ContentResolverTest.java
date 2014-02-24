package org.robolectric.shadows;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.AndroidManifest;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.ContentProviderData;
import org.robolectric.tester.android.database.TestCursor;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ContentResolverTest {
  static final String AUTHORITY = "org.robolectric";

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
    shadowContentResolver.setNextDatabaseIdForInserts(20);

    assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues())).isEqualTo(uri21);
    assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues())).isEqualTo(uri22);
  }
  
  @Test
  public void getType_shouldDefaultToNull() throws Exception {
    assertThat(contentResolver.getType(uri21)).isNull();
  }

  @Test
  public void getType_shouldReturnProviderValue() throws Exception {
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
        return "mytype";
      }
    });
    final Uri uri = Uri.parse("content://"+AUTHORITY+"/some/path");
    assertThat(contentResolver.getType(uri)).isEqualTo("mytype");
  }

  @Test
  public void insert_shouldTrackInsertStatements() throws Exception {
    ContentValues contentValues = new ContentValues();
    contentValues.put("foo", "bar");
    contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
    assertThat(shadowContentResolver.getInsertStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getInsertStatements().get(0).getUri()).isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(shadowContentResolver.getInsertStatements().get(0).getContentValues().getAsString("foo")).isEqualTo("bar");

    contentValues = new ContentValues();
    contentValues.put("hello", "world");
    contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
    assertThat(shadowContentResolver.getInsertStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getInsertStatements().get(1).getContentValues().getAsString("hello")).isEqualTo("world");
  }

  @Test
  public void insert_shouldTrackUpdateStatements() throws Exception {
    ContentValues contentValues = new ContentValues();
    contentValues.put("foo", "bar");
    contentResolver.update(EXTERNAL_CONTENT_URI, contentValues, "robolectric", new String[] { "awesome" });
    assertThat(shadowContentResolver.getUpdateStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getUri()).isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getContentValues().getAsString("foo")).isEqualTo("bar");
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getWhere()).isEqualTo("robolectric");
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getSelectionArgs()).isEqualTo(new String[]{"awesome"});

    contentValues = new ContentValues();
    contentValues.put("hello", "world");
    contentResolver.update(EXTERNAL_CONTENT_URI, contentValues, null, null);
    assertThat(shadowContentResolver.getUpdateStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getUpdateStatements().get(1).getUri()).isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(shadowContentResolver.getUpdateStatements().get(1).getContentValues().getAsString("hello")).isEqualTo("world");
    assertThat(shadowContentResolver.getUpdateStatements().get(1).getWhere()).isNull();
    assertThat(shadowContentResolver.getUpdateStatements().get(1).getSelectionArgs()).isNull();
  }

  @Test
  public void delete_shouldTrackDeletedUris() throws Exception {
    assertThat(shadowContentResolver.getDeletedUris().size()).isEqualTo(0);

    assertThat(contentResolver.delete(uri21, null, null)).isEqualTo(1);
    assertThat(shadowContentResolver.getDeletedUris()).contains(uri21);
    assertThat(shadowContentResolver.getDeletedUris().size()).isEqualTo(1);

    assertThat(contentResolver.delete(uri22, null, null)).isEqualTo(1);
    assertThat(shadowContentResolver.getDeletedUris()).contains(uri22);
    assertThat(shadowContentResolver.getDeletedUris().size()).isEqualTo(2);
  }

  @Test
  public void delete_shouldTrackDeletedStatements() {
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(0);

    assertThat(contentResolver.delete(uri21, "id", new String[]{"5"})).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getUri()).isEqualTo(uri21);
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getWhere()).isEqualTo("id");
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getSelectionArgs()[0]).isEqualTo("5");

    assertThat(contentResolver.delete(uri21, "foo", new String[]{"bar"})).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getUri()).isEqualTo(uri21);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getWhere()).isEqualTo("foo");
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getSelectionArgs()[0]).isEqualTo("bar");
  }

  @Test
  public void query_shouldReturnTheCursorThatWasSet() throws Exception {
    assertNull(shadowContentResolver.query(null, null, null, null, null));
    TestCursor cursor = new TestCursor();
    shadowContentResolver.setCursor(cursor);
    assertThat((TestCursor) shadowContentResolver.query(null, null, null, null, null)).isSameAs(cursor);
  }

  @Test
  public void query_shouldReturnSpecificCursorsForSpecificUris() throws Exception {
    assertNull(shadowContentResolver.query(uri21, null, null, null, null));
    assertNull(shadowContentResolver.query(uri22, null, null, null, null));

    TestCursor cursor21 = new TestCursor();
    TestCursor cursor22 = new TestCursor();
    shadowContentResolver.setCursor(uri21, cursor21);
    shadowContentResolver.setCursor(uri22, cursor22);

    assertThat((TestCursor) shadowContentResolver.query(uri21, null, null, null, null)).isSameAs(cursor21);
    assertThat((TestCursor) shadowContentResolver.query(uri22, null, null, null, null)).isSameAs(cursor22);
  }

  @Test
  public void query_shouldKnowWhatItsParamsWere() throws Exception {
    String[] projection = {};
    String selection = "select";
    String[] selectionArgs = {};
    String sortOrder = "order";

    QueryParamTrackingTestCursor testCursor = new QueryParamTrackingTestCursor();

    shadowContentResolver.setCursor(testCursor);
    Cursor cursor = shadowContentResolver.query(uri21, projection, selection, selectionArgs, sortOrder);
    assertThat((QueryParamTrackingTestCursor) cursor).isEqualTo(testCursor);
    assertThat(testCursor.uri).isEqualTo(uri21);
    assertThat(testCursor.projection).isEqualTo(projection);
    assertThat(testCursor.selection).isEqualTo(selection);
    assertThat(testCursor.selectionArgs).isEqualTo(selectionArgs);
    assertThat(testCursor.sortOrder).isEqualTo(sortOrder);
  }
  
  @Test
  public void acquireUnstableProvider_shouldDefaultToNull() throws Exception {
    assertThat(contentResolver.acquireUnstableProvider(uri21)).isNull();
  }

  @Test
  public void acquireUnstableProvider_shouldReturn() throws Exception {
    ContentProvider cp = new ContentProvider() {
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
    };
    ShadowContentResolver.registerProvider(AUTHORITY, cp);
    final Uri uri = Uri.parse("content://" + AUTHORITY);
    assertThat(contentResolver.acquireUnstableProvider(uri)).isSameAs(cp.getIContentProvider());
  }
  
  @Test
  public void openInputStream_shouldReturnAnInputStream() throws Exception {
    assertThat(contentResolver.openInputStream(uri21)).isInstanceOf(InputStream.class);
  }

  @Test
  public void openOutputStream_shouldReturnAnOutputStream() throws Exception {
    assertThat(contentResolver.openOutputStream(uri21)).isInstanceOf(OutputStream.class);
  }

  @Test
  public void shouldTrackNotifiedUris() throws Exception {
    contentResolver.notifyChange(Uri.parse("foo"), null, true);
    contentResolver.notifyChange(Uri.parse("bar"), null);

    assertThat(shadowContentResolver.getNotifiedUris().size()).isEqualTo(2);
    ShadowContentResolver.NotifiedUri uri = shadowContentResolver.getNotifiedUris().get(0);

    assertThat(uri.uri.toString()).isEqualTo("foo");
    assertTrue(uri.syncToNetwork);
    assertNull(uri.observer);

    uri = shadowContentResolver.getNotifiedUris().get(1);

    assertThat(uri.uri.toString()).isEqualTo("bar");
    assertFalse(uri.syncToNetwork);
    assertNull(uri.observer);
  }

  @SuppressWarnings("serial")
  @Test
  public void applyBatchForRegisteredProvider() throws RemoteException, OperationApplicationException {
    final ArrayList<String> operations = new ArrayList<String>();
    ShadowContentResolver.registerProvider("registeredProvider", new ContentProvider() {
      @Override
      public boolean onCreate() { return true; }
      @Override
      public Cursor query(Uri uri, String[] projection, String selection,
          String[] selectionArgs, String sortOrder) {
        operations.add("query");
        MatrixCursor cursor = new MatrixCursor(new String[] {"a"});
        cursor.addRow(new Object[] {"b"});
        return cursor;
      }

      @Override
      public String getType(Uri uri) {
        return null;
      }

      @Override
      public Uri insert(Uri uri, ContentValues values) {
        operations.add("insert");
        return ContentUris.withAppendedId(uri, 1);
      }

      @Override
      public int delete(Uri uri, String selection, String[] selectionArgs) {
        operations.add("delete");
        return 0;
      }

      @Override
      public int update(Uri uri, ContentValues values, String selection,
          String[] selectionArgs) {
        operations.add("update");
        return 0;
      }

    });

    final Uri uri = Uri.parse("content://registeredProvider/path");
    contentResolver.applyBatch("registeredProvider", new ArrayList<ContentProviderOperation>() {
      {
        add(ContentProviderOperation.newInsert(uri).withValue("a", "b").build());
        add(ContentProviderOperation.newUpdate(uri).withValue("a", "b").build());
        add(ContentProviderOperation.newDelete(uri).build());
        add(ContentProviderOperation.newAssertQuery(uri).withValue("a", "b").build());
      }
    });

    assertThat(operations).containsExactly("insert", "update", "delete", "query");
  }

  @Test
  public void applyBatchForUnregisteredProvider() throws RemoteException, OperationApplicationException {
    ArrayList<ContentProviderOperation> resultOperations = shadowContentResolver.getContentProviderOperations(AUTHORITY);
    assertThat(resultOperations).isNotNull();
    assertThat(resultOperations.size()).isEqualTo(0);

    ContentProviderResult[] contentProviderResults = new ContentProviderResult[] {
        new ContentProviderResult(1),
        new ContentProviderResult(1),
    };
    shadowContentResolver.setContentProviderResult(contentProviderResults);
    Uri uri = Uri.parse("content://org.robolectric");
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
    assertThat(resultOperations).isEqualTo(operations);
    assertThat(result).isEqualTo(contentProviderResults);
  }

  @Test
  public void shouldKeepTrackOfSyncRequests() throws Exception {
    ShadowContentResolver.Status status = ShadowContentResolver.getStatus(a, AUTHORITY, true);
    assertNotNull(status);
    assertThat(status.syncRequests).isEqualTo(0);
    ContentResolver.requestSync(a, AUTHORITY, new Bundle());
    assertThat(status.syncRequests).isEqualTo(1);
    assertNotNull(status.syncExtras);
  }

  @Test
  public void shouldSetIsSyncable() throws Exception {
    assertThat(ContentResolver.getIsSyncable(a, AUTHORITY)).isEqualTo(-1);
    assertThat(ContentResolver.getIsSyncable(b, AUTHORITY)).isEqualTo(-1);
    ContentResolver.setIsSyncable(a, AUTHORITY, 1);
    ContentResolver.setIsSyncable(b, AUTHORITY, 2);
    assertThat(ContentResolver.getIsSyncable(a, AUTHORITY)).isEqualTo(1);
    assertThat(ContentResolver.getIsSyncable(b, AUTHORITY)).isEqualTo(2);
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
    assertThat(status.syncs.size()).isEqualTo(1);
    assertThat(status.syncs.get(0).period).isEqualTo(6000l);
    assertNotNull(status.syncs.get(0).extras);
  }

  @Test
  public void shouldRemovePeriodSync() throws Exception {
    ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000l);
    ContentResolver.removePeriodicSync(a, AUTHORITY, new Bundle());
    assertThat(ShadowContentResolver.getStatus(a, AUTHORITY).syncs.size()).isEqualTo(0);
  }

  @Test
  public void shouldGetPeriodSyncs() throws Exception {
    assertThat(ContentResolver.getPeriodicSyncs(a, AUTHORITY).size()).isEqualTo(0);
    ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000l);

    List<PeriodicSync> syncs = ContentResolver.getPeriodicSyncs(a, AUTHORITY);
    assertThat(syncs.size()).isEqualTo(1);

    PeriodicSync first = syncs.get(0);
    assertThat(first.account).isEqualTo(a);
    assertThat(first.authority).isEqualTo(AUTHORITY);
    assertThat(first.period).isEqualTo(6000l);
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
    assertThat(contentResolver.delete(uri, null, null)).isEqualTo(-1);
    assertThat(contentResolver.update(uri, new ContentValues(), null, null)).isEqualTo(-1);

    assertNull(contentResolver.query(unrelated, null, null, null, null));
    assertNotNull(contentResolver.insert(unrelated, new ContentValues()));
    assertThat(contentResolver.delete(unrelated, null, null)).isEqualTo(1);
    assertThat(contentResolver.update(unrelated, new ContentValues(), null, null)).isEqualTo(1);
  }

  @Test
  public void shouldRegisterContentObservers() throws Exception {
    TestContentObserver co = new TestContentObserver(null);
    ShadowContentResolver scr = Robolectric.shadowOf(contentResolver);

    assertThat(scr.getContentObserver(EXTERNAL_CONTENT_URI)).isNull();

    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co);

    assertThat(scr.getContentObserver(EXTERNAL_CONTENT_URI)).isSameAs((ContentObserver) co);

    assertThat(co.changed).isFalse();
    contentResolver.notifyChange(EXTERNAL_CONTENT_URI, null);
    assertThat(co.changed).isTrue();

    scr.clearContentObservers();
    assertThat(scr.getContentObserver(EXTERNAL_CONTENT_URI)).isNull();
  }

  @Test
  public void shouldUnregisterContentObservers() throws Exception {
    TestContentObserver co = new TestContentObserver(null);
    ShadowContentResolver scr = Robolectric.shadowOf(contentResolver);
    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co);
    assertThat(scr.getContentObserver(EXTERNAL_CONTENT_URI)).isSameAs((ContentObserver) co);

    contentResolver.unregisterContentObserver(co);
    assertThat(scr.getContentObserver(EXTERNAL_CONTENT_URI)).isNull();

    assertThat(co.changed).isFalse();
    contentResolver.notifyChange(EXTERNAL_CONTENT_URI, null);
    assertThat(co.changed).isFalse();
  }
  
  @Test
  public void getProvider_shouldCreateProviderFromManifest() {
    AndroidManifest manifest = Robolectric.getShadowApplication().getAppManifest();
    manifest.getContentProviders().add(new ContentProviderData("org.robolectric.shadows.ContentResolverTest$TestContentProvider", AUTHORITY));
    assertThat(ShadowContentResolver.getProvider(Uri.parse("content://" + AUTHORITY + "/shadows"))).isNotNull();
  }

  @Test
  public void getProvider_shouldNotReturnAnyProviderWhenManifestIsNull() {
    Robolectric.application = new DefaultTestLifecycle().createApplication(null, null);
    assertThat(ShadowContentResolver.getProvider(Uri.parse("content://"))).isNull();
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

  private class TestContentObserver extends ContentObserver {
    public TestContentObserver(Handler handler) {
      super(handler);
    }

    public boolean changed = false;

    @Override
    public void onChange(boolean selfChange) {
      changed = true;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      changed = true;
    }
  }

  public static class TestContentProvider extends ContentProvider {
    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
      return 0;
    }

    @Override
    public String getType(Uri arg0) {
      return null;
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
      return null;
    }

    @Override
    public boolean onCreate() {
      return false;
    }

    @Override
    public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3, String arg4) {
      return null;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
      return 0;
    }
  }
}
