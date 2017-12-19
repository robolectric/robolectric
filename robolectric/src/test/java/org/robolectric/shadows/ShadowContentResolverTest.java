package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.accounts.Account;
import android.app.Activity;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.UriPermission;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.BaseCursor;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class ShadowContentResolverTest {
  private static final String AUTHORITY = "org.robolectric";

  private ContentResolver contentResolver;
  private ShadowContentResolver shadowContentResolver;
  private Uri uri21;
  private Uri uri22;
  private Account a, b;

  @Before
  public void setUp() {
    contentResolver = Robolectric.setupActivity(Activity.class).getContentResolver();
    shadowContentResolver = shadowOf(contentResolver);
    uri21 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/21");
    uri22 = Uri.parse(EXTERNAL_CONTENT_URI.toString() + "/22");

    a = new Account("a", "type");
    b = new Account("b", "type");
  }

  @Test
  public void insert_shouldReturnIncreasingUris() {
    shadowContentResolver.setNextDatabaseIdForInserts(20);

    assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues())).isEqualTo(uri21);
    assertThat(contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues())).isEqualTo(uri22);
  }

  @Test
  public void getType_shouldDefaultToNull() {
    assertThat(contentResolver.getType(uri21)).isNull();
  }

  @Test
  public void getType_shouldReturnProviderValue() {
    ShadowContentResolver.registerProviderInternal(AUTHORITY, new ContentProvider() {
      @Override public boolean onCreate() {
        return false;
      }
      @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return new BaseCursor();
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
  public void insert_shouldTrackInsertStatements() {
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
  public void insert_shouldTrackUpdateStatements() {
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
  public void insert_supportsNullContentValues() {
    contentResolver.insert(EXTERNAL_CONTENT_URI, null);
    assertThat(shadowContentResolver.getInsertStatements().get(0).getContentValues()).isNull();
  }

  @Test
  public void update_supportsNullContentValues() {
    contentResolver.update(EXTERNAL_CONTENT_URI, null, null, null);
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getContentValues()).isNull();
  }

  @Test
  public void delete_shouldTrackDeletedUris() {
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
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getContentProvider()).isNull();
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getWhere()).isEqualTo("id");
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getSelectionArgs()[0]).isEqualTo("5");

    assertThat(contentResolver.delete(uri21, "foo", new String[]{"bar"})).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getUri()).isEqualTo(uri21);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getWhere()).isEqualTo("foo");
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getSelectionArgs()[0]).isEqualTo("bar");
  }

  @Test
  public void whenCursorHasBeenSet_query_shouldReturnTheCursor() {
    assertThat(shadowContentResolver.query(null, null, null, null, null)).isNull();
    BaseCursor cursor = new BaseCursor();
    shadowContentResolver.setCursor(cursor);
    assertThat((BaseCursor) shadowContentResolver.query(null, null, null, null, null)).isSameAs(cursor);
  }

  @Test
  public void whenCursorHasBeenSet_queryWithCancellationSignal_shouldReturnTheCursor() {
    assertThat(shadowContentResolver.query(null, null, null, null, null, new CancellationSignal())).isNull();
    BaseCursor cursor = new BaseCursor();
    shadowContentResolver.setCursor(cursor);
    assertThat((BaseCursor) shadowContentResolver.query(null, null, null, null, null, new CancellationSignal())).isSameAs(cursor);
  }

  @Test
  public void query_shouldReturnSpecificCursorsForSpecificUris() {
    assertThat(shadowContentResolver.query(uri21, null, null, null, null)).isNull();
    assertThat(shadowContentResolver.query(uri22, null, null, null, null)).isNull();

    BaseCursor cursor21 = new BaseCursor();
    BaseCursor cursor22 = new BaseCursor();
    shadowContentResolver.setCursor(uri21, cursor21);
    shadowContentResolver.setCursor(uri22, cursor22);

    assertThat((BaseCursor) shadowContentResolver.query(uri21, null, null, null, null)).isSameAs(cursor21);
    assertThat((BaseCursor) shadowContentResolver.query(uri22, null, null, null, null)).isSameAs(cursor22);
  }

  @Test
  public void query_shouldKnowWhatItsParamsWere() {
    String[] projection = {};
    String selection = "select";
    String[] selectionArgs = {};
    String sortOrder = "order";

    QueryParamTrackingCursor testCursor = new QueryParamTrackingCursor();

    shadowContentResolver.setCursor(testCursor);
    Cursor cursor = shadowContentResolver.query(uri21, projection, selection, selectionArgs, sortOrder);
    assertThat((QueryParamTrackingCursor) cursor).isEqualTo(testCursor);
    assertThat(testCursor.uri).isEqualTo(uri21);
    assertThat(testCursor.projection).isEqualTo(projection);
    assertThat(testCursor.selection).isEqualTo(selection);
    assertThat(testCursor.selectionArgs).isEqualTo(selectionArgs);
    assertThat(testCursor.sortOrder).isEqualTo(sortOrder);
  }

  @Test
  public void acquireUnstableProvider_shouldDefaultToNull() {
    assertThat(contentResolver.acquireUnstableProvider(uri21)).isNull();
  }

  @Test
  public void acquireUnstableProvider_shouldReturnWithUri() {
    ContentProvider cp = mock(ContentProvider.class);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, cp);
    final Uri uri = Uri.parse("content://" + AUTHORITY);
    assertThat(contentResolver.acquireUnstableProvider(uri)).isSameAs(cp.getIContentProvider());
  }

  @Test
  public void acquireUnstableProvider_shouldReturnWithString() {
    ContentProvider cp = mock(ContentProvider.class);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, cp);
    assertThat(contentResolver.acquireUnstableProvider(AUTHORITY)).isSameAs(cp.getIContentProvider());
  }

  @Test
  public void call_shouldCallProvider() {
    final String METHOD = "method";
    final String ARG = "arg";
    final Bundle EXTRAS = new Bundle();
    final Uri uri = Uri.parse("content://" + AUTHORITY);

    ContentProvider provider = mock(ContentProvider.class);
    doReturn(null).when(provider).call(METHOD, ARG, EXTRAS);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, provider);

    contentResolver.call(uri, METHOD, ARG, EXTRAS);
    verify(provider).call(METHOD, ARG, EXTRAS);
  }

  @Test
  public void registerProvider_shouldAttachProviderInfo() {
    ContentProvider mock = mock(ContentProvider.class);

    ProviderInfo providerInfo0 = new ProviderInfo();
    providerInfo0.authority = "the-authority"; // todo: support multiple authorities
    providerInfo0.grantUriPermissions = true;
    mock.attachInfo(RuntimeEnvironment.application, providerInfo0);
    mock.onCreate();

    ArgumentCaptor<ProviderInfo> captor = ArgumentCaptor.forClass(ProviderInfo.class);
    verify(mock).attachInfo(same(RuntimeEnvironment.application), captor.capture());
    ProviderInfo providerInfo = captor.getValue();

    assertThat(providerInfo.authority).isEqualTo("the-authority");
    assertThat(providerInfo.grantUriPermissions).isEqualTo(true);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void openInputStream_shouldReturnAnInputStreamThatExceptionsOnRead() throws Exception {
    InputStream inputStream = contentResolver.openInputStream(uri21);
    inputStream.read();
  }

  @Test
  public void openInputStream_returnsPreRegisteredStream() throws Exception {
    shadowContentResolver.registerInputStream(uri21, new ByteArrayInputStream("ourStream".getBytes(UTF_8)));
    InputStream inputStream = contentResolver.openInputStream(uri21);
    byte[] data = new byte[9];
    inputStream.read(data);
    assertThat(new String(data, UTF_8)).isEqualTo("ourStream");
  }

  @Test
  public void openOutputStream_shouldReturnAnOutputStream() throws Exception {
    assertThat(contentResolver.openOutputStream(uri21)).isInstanceOf(OutputStream.class);
  }

  @Test
  public void shouldTrackNotifiedUris() {
    contentResolver.notifyChange(Uri.parse("foo"), null, true);
    contentResolver.notifyChange(Uri.parse("bar"), null);

    assertThat(shadowContentResolver.getNotifiedUris().size()).isEqualTo(2);
    ShadowContentResolver.NotifiedUri uri = shadowContentResolver.getNotifiedUris().get(0);

    assertThat(uri.uri.toString()).isEqualTo("foo");
    assertThat(uri.syncToNetwork).isTrue();
    assertThat(uri.observer).isNull();

    uri = shadowContentResolver.getNotifiedUris().get(1);

    assertThat(uri.uri.toString()).isEqualTo("bar");
    assertThat(uri.syncToNetwork).isFalse();
    assertThat(uri.observer).isNull();
  }

  @SuppressWarnings("serial")
  @Test
  public void applyBatchForRegisteredProvider() throws RemoteException, OperationApplicationException {
    final List<String> operations = new ArrayList<>();
    ShadowContentResolver.registerProviderInternal("registeredProvider", new ContentProvider() {
      @Override
      public boolean onCreate() {
        return true;
      }

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
    List<ContentProviderOperation> resultOperations = shadowContentResolver.getContentProviderOperations(AUTHORITY);
    assertThat(resultOperations).isNotNull();
    assertThat(resultOperations.size()).isEqualTo(0);

    ContentProviderResult[] contentProviderResults = new ContentProviderResult[] {
        new ContentProviderResult(1),
        new ContentProviderResult(1),
    };
    shadowContentResolver.setContentProviderResult(contentProviderResults);
    Uri uri = Uri.parse("content://org.robolectric");
    ArrayList<ContentProviderOperation> operations = new ArrayList<>();
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
  public void shouldKeepTrackOfSyncRequests() {
    ShadowContentResolver.Status status = ShadowContentResolver.getStatus(a, AUTHORITY, true);
    assertThat(status).isNotNull();
    assertThat(status.syncRequests).isEqualTo(0);
    ContentResolver.requestSync(a, AUTHORITY, new Bundle());
    assertThat(status.syncRequests).isEqualTo(1);
    assertThat(status.syncExtras).isNotNull();
  }

  @Test
  public void shouldKnowIfSyncIsActive() {
    assertThat(ContentResolver.isSyncActive(a, AUTHORITY)).isFalse();
    ContentResolver.requestSync(a, AUTHORITY, new Bundle());
    assertThat(ContentResolver.isSyncActive(a, AUTHORITY)).isTrue();
  }

  @Test
  public void shouldCancelSync() {
    ContentResolver.requestSync(a, AUTHORITY, new Bundle());
    ContentResolver.requestSync(b, AUTHORITY, new Bundle());
    assertThat(ContentResolver.isSyncActive(a, AUTHORITY)).isTrue();
    assertThat(ContentResolver.isSyncActive(b, AUTHORITY)).isTrue();

    ContentResolver.cancelSync(a, AUTHORITY);
    assertThat(ContentResolver.isSyncActive(a, AUTHORITY)).isFalse();
    assertThat(ContentResolver.isSyncActive(b, AUTHORITY)).isTrue();
  }

  @Test
  public void shouldSetIsSyncable() {
    assertThat(ContentResolver.getIsSyncable(a, AUTHORITY)).isEqualTo(-1);
    assertThat(ContentResolver.getIsSyncable(b, AUTHORITY)).isEqualTo(-1);
    ContentResolver.setIsSyncable(a, AUTHORITY, 1);
    ContentResolver.setIsSyncable(b, AUTHORITY, 2);
    assertThat(ContentResolver.getIsSyncable(a, AUTHORITY)).isEqualTo(1);
    assertThat(ContentResolver.getIsSyncable(b, AUTHORITY)).isEqualTo(2);
  }

  @Test
  public void shouldSetSyncAutomatically() {
    assertThat(ContentResolver.getSyncAutomatically(a, AUTHORITY)).isFalse();
    ContentResolver.setSyncAutomatically(a, AUTHORITY, true);
    assertThat(ContentResolver.getSyncAutomatically(a, AUTHORITY)).isTrue();
  }

  @Test
  public void shouldAddPeriodicSync() {
    Bundle fooBar = new Bundle();
    fooBar.putString("foo", "bar");
    Bundle fooBaz = new Bundle();
    fooBaz.putString("foo", "baz");

    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBar, 6000L);
    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBaz, 6000L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBar, 6000L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBaz, 6000L);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).containsOnly(
        new PeriodicSync(a, AUTHORITY, fooBar, 6000L),
        new PeriodicSync(a, AUTHORITY, fooBaz, 6000L));
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY)).containsOnly(
        new PeriodicSync(b, AUTHORITY, fooBar, 6000L),
        new PeriodicSync(b, AUTHORITY, fooBaz, 6000L));

    // If same extras, but different time, simply update the time.
    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBar, 42L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBaz, 42L);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).containsOnly(
        new PeriodicSync(a, AUTHORITY, fooBar, 42L),
        new PeriodicSync(a, AUTHORITY, fooBaz, 6000L));
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY)).containsOnly(
        new PeriodicSync(b, AUTHORITY, fooBar, 6000L),
        new PeriodicSync(b, AUTHORITY, fooBaz, 42L));
  }

  @Test
  public void shouldRemovePeriodSync() {
    Bundle fooBar = new Bundle();
    fooBar.putString("foo", "bar");
    Bundle fooBaz = new Bundle();
    fooBaz.putString("foo", "baz");
    Bundle foo42 = new Bundle();
    foo42.putInt("foo", 42);
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY)).isEmpty();
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).isEmpty();

    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBar, 6000L);
    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBaz, 6000L);
    ContentResolver.addPeriodicSync(a, AUTHORITY, foo42, 6000L);

    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBar, 6000L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBaz, 6000L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, foo42, 6000L);

    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).containsOnly(
        new PeriodicSync(a, AUTHORITY, fooBar, 6000L),
        new PeriodicSync(a, AUTHORITY, fooBaz, 6000L),
        new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, fooBar);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).containsOnly(
        new PeriodicSync(a, AUTHORITY, fooBaz, 6000L),
        new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, fooBaz);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).containsOnly(
        new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, foo42);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).isEmpty();
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY)).containsOnly(
        new PeriodicSync(b, AUTHORITY, fooBar, 6000L),
        new PeriodicSync(b, AUTHORITY, fooBaz, 6000L),
        new PeriodicSync(b, AUTHORITY, foo42, 6000L));
  }

  @Test
  public void shouldGetPeriodSyncs() {
    assertThat(ContentResolver.getPeriodicSyncs(a, AUTHORITY).size()).isEqualTo(0);
    ContentResolver.addPeriodicSync(a, AUTHORITY, new Bundle(), 6000L);

    List<PeriodicSync> syncs = ContentResolver.getPeriodicSyncs(a, AUTHORITY);
    assertThat(syncs.size()).isEqualTo(1);

    PeriodicSync first = syncs.get(0);
    assertThat(first.account).isEqualTo(a);
    assertThat(first.authority).isEqualTo(AUTHORITY);
    assertThat(first.period).isEqualTo(6000L);
    assertThat(first.extras).isNotNull();
  }

  @Test
  public void shouldValidateSyncExtras() {
    Bundle bundle = new Bundle();
    bundle.putString("foo", "strings");
    bundle.putLong("long", 10L);
    bundle.putDouble("double", 10.0d);
    bundle.putFloat("float", 10.0f);
    bundle.putInt("int", 10);
    bundle.putParcelable("account", a);
    ContentResolver.validateSyncExtrasBundle(bundle);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldValidateSyncExtrasAndThrow() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("intent", new Intent());
    ContentResolver.validateSyncExtrasBundle(bundle);
  }

  @Test
  public void shouldSetMasterSyncAutomatically() {
    assertThat(ContentResolver.getMasterSyncAutomatically()).isFalse();
    ContentResolver.setMasterSyncAutomatically(true);
    assertThat(ContentResolver.getMasterSyncAutomatically()).isTrue();
  }

  @Test
  public void shouldDelegateCallsToRegisteredProvider() {
    ShadowContentResolver.registerProviderInternal(AUTHORITY, new ContentProvider() {
      @Override
      public boolean onCreate() {
        return false;
      }

      @Override
      public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return new BaseCursor();
      }

      @Override
      public Uri insert(Uri uri, ContentValues values) {
        return null;
      }

      @Override
      public int delete(Uri uri, String selection, String[] selectionArgs) {
        return -1;
      }

      @Override
      public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return -1;
      }

      @Override
      public String getType(Uri uri) {
        return null;
      }
    });
    final Uri uri = Uri.parse("content://"+AUTHORITY+"/some/path");
    final Uri unrelated = Uri.parse("content://unrelated/some/path");

    assertThat(contentResolver.query(uri, null, null, null, null)).isNotNull();
    assertThat(contentResolver.insert(uri, new ContentValues())).isNull();

    assertThat(contentResolver.delete(uri, null, null)).isEqualTo(-1);
    assertThat(contentResolver.update(uri, new ContentValues(), null, null)).isEqualTo(-1);

    assertThat(contentResolver.query(unrelated, null, null, null, null)).isNull();
    assertThat(contentResolver.insert(unrelated, new ContentValues())).isNotNull();
    assertThat(contentResolver.delete(unrelated, null, null)).isEqualTo(1);
    assertThat(contentResolver.update(unrelated, new ContentValues(), null, null)).isEqualTo(1);
  }

  @Test
  public void shouldRegisterContentObservers() {
    TestContentObserver co = new TestContentObserver(null);
    ShadowContentResolver scr = shadowOf(contentResolver);

    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).isEmpty();

    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co);

    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).containsExactly((ContentObserver) co);

    assertThat(co.changed).isFalse();
    contentResolver.notifyChange(EXTERNAL_CONTENT_URI, null);
    assertThat(co.changed).isTrue();

    scr.clearContentObservers();
    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).isEmpty();
  }

  @Test
  public void shouldUnregisterContentObservers() {
    TestContentObserver co = new TestContentObserver(null);
    ShadowContentResolver scr = shadowOf(contentResolver);
    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co);
    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI))
        .containsExactlyInAnyOrder((ContentObserver) co);

    contentResolver.unregisterContentObserver(co);
    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).isEmpty();

    assertThat(co.changed).isFalse();
    contentResolver.notifyChange(EXTERNAL_CONTENT_URI, null);
    assertThat(co.changed).isFalse();
  }

  @Test
  public void shouldNotifyChildContentObservers() throws Exception {
    TestContentObserver co1 = new TestContentObserver(null);
    TestContentObserver co2 = new TestContentObserver(null);

    Uri childUri = EXTERNAL_CONTENT_URI.buildUpon().appendPath("path").build();

    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co1);
    contentResolver.registerContentObserver(childUri, false, co2);

    co1.changed = co2.changed = false;
    contentResolver.notifyChange(childUri, null);
    assertThat(co1.changed).isTrue();
    assertThat(co2.changed).isTrue();

    co1.changed = co2.changed = false;
    contentResolver.notifyChange(EXTERNAL_CONTENT_URI, null);
    assertThat(co1.changed).isTrue();
    assertThat(co2.changed).isFalse();

    co1.changed = co2.changed = false;
    contentResolver.notifyChange(childUri.buildUpon().appendPath("extra").build(), null);
    assertThat(co1.changed).isTrue();
    assertThat(co2.changed).isFalse();
  }

  @Test
  public void getProvider_shouldCreateProviderFromManifest() {
    AndroidManifest manifest = ShadowApplication.getInstance().getAppManifest();
    ContentProviderData testProviderData = new ContentProviderData("org.robolectric.shadows.ShadowContentResolverTest$TestContentProvider", null, AUTHORITY, null, null, null);
    try {
      manifest.getContentProviders().add(testProviderData);
      assertThat(ShadowContentResolver.getProvider(Uri.parse("content://" + AUTHORITY + "/shadows"))).isNotNull();
    } finally {
      manifest.getContentProviders().remove(testProviderData);
    }
  }

  @Test
  public void getProvider_shouldNotReturnAnyProviderWhenManifestIsNull() {
    Application application = new DefaultTestLifecycle().createApplication(null, null, null);
    ReflectionHelpers.callInstanceMethod(application, "attach", ReflectionHelpers.ClassParameter.from(Context.class, RuntimeEnvironment.application.getBaseContext()));
    assertThat(ShadowContentResolver.getProvider(Uri.parse("content://"))).isNull();
  }

  @Test
  public void getProvider_shouldSetAuthority() throws RemoteException {
    AndroidManifest manifest = ShadowApplication.getInstance().getAppManifest();
    ContentProviderData testProviderData = new ContentProviderData("org.robolectric.shadows.ShadowContentResolverTest$TestContentProvider", null, AUTHORITY, null, null, null);
    try {
      manifest.getContentProviders().add(testProviderData);
      Uri uri = Uri.parse("content://" + AUTHORITY + "/shadows");
      ContentProvider provider = ShadowContentResolver.getProvider(uri);
      // unfortunately, there is no direct way of testing if authority is set or not
      // however, it's checked in ContentProvider.Transport method calls (validateIncomingUri), so it's the closest we can test against
      provider.getIContentProvider().getType(uri); // should not throw
    } finally {
      manifest.getContentProviders().remove(testProviderData);
    }
  }

  @Test
  public void openTypedAssetFileDescriptor_shouldOpenDescriptor() throws IOException, RemoteException {
    final File file = new File(RuntimeEnvironment.application.getFilesDir(), "test_file");
    file.createNewFile();

    ShadowContentResolver.registerProviderInternal(AUTHORITY, new ContentProvider() {
      @Override
      public boolean onCreate() {
        return true;
      }

      @Override
      public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
      }

      @Override
      public String getType(Uri uri) {
        return null;
      }

      @Override
      public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
      }

      @Override
      public int delete(Uri uri, String s, String[] strings) {
        return 0;
      }

      @Override
      public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
      }

      @Override
      public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
      }
    });

    AssetFileDescriptor afd = contentResolver.openTypedAssetFileDescriptor(Uri.parse("content://" + AUTHORITY + "/whatever"), "*/*", null);

    FileDescriptor descriptor = afd.getFileDescriptor();
    assertThat(descriptor).isNotNull();
  }

  @Test
  @Config(minSdk = KITKAT)
  public void takeAndReleasePersistableUriPermissions() {
    List<UriPermission> permissions = contentResolver.getPersistedUriPermissions();
    assertThat(permissions).isEmpty();

    // Take the read permission for the uri.
    Uri uri = Uri.parse("content://" + AUTHORITY + "/whatever");
    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    assertThat(permissions).hasSize(1);
    assertThat(permissions.get(0).getUri()).isSameAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isTrue();
    assertThat(permissions.get(0).isWritePermission()).isFalse();

    // Take the write permission for the uri.
    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    assertThat(permissions).hasSize(1);
    assertThat(permissions.get(0).getUri()).isSameAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isTrue();
    assertThat(permissions.get(0).isWritePermission()).isTrue();

    // Release the read permission for the uri.
    contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    assertThat(permissions).hasSize(1);
    assertThat(permissions.get(0).getUri()).isSameAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isFalse();
    assertThat(permissions.get(0).isWritePermission()).isTrue();

    // Release the write permission for the uri.
    contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    assertThat(permissions).isEmpty();
  }

  private static class QueryParamTrackingCursor extends BaseCursor {
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

  private static class TestContentObserver extends ContentObserver {
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
