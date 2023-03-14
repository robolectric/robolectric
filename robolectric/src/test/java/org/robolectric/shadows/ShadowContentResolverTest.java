package org.robolectric.shadows;

import static android.content.ContentResolver.QUERY_ARG_SQL_SELECTION;
import static android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS;
import static android.content.ContentResolver.QUERY_ARG_SQL_SORT_ORDER;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.Config.NONE;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
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
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Iterables;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.BaseCursor;
import org.robolectric.util.NamedStream;

@RunWith(AndroidJUnit4.class)
public class ShadowContentResolverTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private static final String AUTHORITY = "org.robolectric";

  private ContentResolver contentResolver;
  private ShadowContentResolver shadowContentResolver;
  private Uri uri21;
  private Uri uri22;
  private Account a, b;

  @Before
  public void setUp() {
    contentResolver = ApplicationProvider.getApplicationContext().getContentResolver();
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
    ShadowContentResolver.registerProviderInternal(
        AUTHORITY,
        new ContentProvider() {
          @Override
          public boolean onCreate() {
            return false;
          }

          @Override
          public Cursor query(
              Uri uri,
              String[] projection,
              String selection,
              String[] selectionArgs,
              String sortOrder) {
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
          public int update(
              Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return -1;
          }

          @Override
          public String getType(Uri uri) {
            return "mytype";
          }
        });
    final Uri uri = Uri.parse("content://" + AUTHORITY + "/some/path");
    assertThat(contentResolver.getType(uri)).isEqualTo("mytype");
  }

  @Test
  public void insert_shouldTrackInsertStatements() {
    ContentValues contentValues = new ContentValues();
    contentValues.put("foo", "bar");
    contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
    assertThat(shadowContentResolver.getInsertStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getInsertStatements().get(0).getUri())
        .isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(
            shadowContentResolver
                .getInsertStatements()
                .get(0)
                .getContentValues()
                .getAsString("foo"))
        .isEqualTo("bar");

    contentValues = new ContentValues();
    contentValues.put("hello", "world");
    contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues);
    assertThat(shadowContentResolver.getInsertStatements().size()).isEqualTo(2);
    assertThat(
            shadowContentResolver
                .getInsertStatements()
                .get(1)
                .getContentValues()
                .getAsString("hello"))
        .isEqualTo("world");
  }

  @Test
  public void insert_shouldTrackUpdateStatements() {
    ContentValues contentValues = new ContentValues();
    contentValues.put("foo", "bar");
    contentResolver.update(
        EXTERNAL_CONTENT_URI, contentValues, "robolectric", new String[] {"awesome"});
    assertThat(shadowContentResolver.getUpdateStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getUri())
        .isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(
            shadowContentResolver
                .getUpdateStatements()
                .get(0)
                .getContentValues()
                .getAsString("foo"))
        .isEqualTo("bar");
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getWhere())
        .isEqualTo("robolectric");
    assertThat(shadowContentResolver.getUpdateStatements().get(0).getSelectionArgs())
        .isEqualTo(new String[] {"awesome"});

    contentValues = new ContentValues();
    contentValues.put("hello", "world");
    contentResolver.update(EXTERNAL_CONTENT_URI, contentValues, null, null);
    assertThat(shadowContentResolver.getUpdateStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getUpdateStatements().get(1).getUri())
        .isEqualTo(EXTERNAL_CONTENT_URI);
    assertThat(
            shadowContentResolver
                .getUpdateStatements()
                .get(1)
                .getContentValues()
                .getAsString("hello"))
        .isEqualTo("world");
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

    assertThat(contentResolver.delete(uri21, "id", new String[] {"5"})).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getUri()).isEqualTo(uri21);
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getContentProvider()).isNull();
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getWhere()).isEqualTo("id");
    assertThat(shadowContentResolver.getDeleteStatements().get(0).getSelectionArgs()[0])
        .isEqualTo("5");

    assertThat(contentResolver.delete(uri21, "foo", new String[] {"bar"})).isEqualTo(1);
    assertThat(shadowContentResolver.getDeleteStatements().size()).isEqualTo(2);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getUri()).isEqualTo(uri21);
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getWhere()).isEqualTo("foo");
    assertThat(shadowContentResolver.getDeleteStatements().get(1).getSelectionArgs()[0])
        .isEqualTo("bar");
  }

  @Test
  public void whenCursorHasBeenSet_query_shouldReturnTheCursor() {
    assertThat(shadowContentResolver.query(null, null, null, null, null)).isNull();
    BaseCursor cursor = new BaseCursor();
    shadowContentResolver.setCursor(cursor);
    assertThat((BaseCursor) shadowContentResolver.query(null, null, null, null, null))
        .isSameInstanceAs(cursor);
  }

  @Test
  public void whenCursorHasBeenSet_queryWithCancellationSignal_shouldReturnTheCursor() {
    assertThat(shadowContentResolver.query(null, null, null, null, null, new CancellationSignal()))
        .isNull();
    BaseCursor cursor = new BaseCursor();
    shadowContentResolver.setCursor(cursor);
    assertThat(
            (BaseCursor)
                shadowContentResolver.query(null, null, null, null, null, new CancellationSignal()))
        .isSameInstanceAs(cursor);
  }

  @Test
  public void query_shouldReturnSpecificCursorsForSpecificUris() {
    assertThat(shadowContentResolver.query(uri21, null, null, null, null)).isNull();
    assertThat(shadowContentResolver.query(uri22, null, null, null, null)).isNull();

    BaseCursor cursor21 = new BaseCursor();
    BaseCursor cursor22 = new BaseCursor();
    shadowContentResolver.setCursor(uri21, cursor21);
    shadowContentResolver.setCursor(uri22, cursor22);

    assertThat((BaseCursor) shadowContentResolver.query(uri21, null, null, null, null))
        .isSameInstanceAs(cursor21);
    assertThat((BaseCursor) shadowContentResolver.query(uri22, null, null, null, null))
        .isSameInstanceAs(cursor22);
  }

  @Test
  public void query_shouldKnowWhatItsParamsWere() {
    String[] projection = {};
    String selection = "select";
    String[] selectionArgs = {};
    String sortOrder = "order";

    QueryParamTrackingCursor testCursor = new QueryParamTrackingCursor();

    shadowContentResolver.setCursor(testCursor);
    Cursor cursor =
        shadowContentResolver.query(uri21, projection, selection, selectionArgs, sortOrder);
    assertThat((QueryParamTrackingCursor) cursor).isEqualTo(testCursor);
    assertThat(testCursor.uri).isEqualTo(uri21);
    assertThat(testCursor.projection).isEqualTo(projection);
    assertThat(testCursor.selection).isEqualTo(selection);
    assertThat(testCursor.selectionArgs).isEqualTo(selectionArgs);
    assertThat(testCursor.sortOrder).isEqualTo(sortOrder);
  }

  @Test
  @Config(minSdk = O)
  public void query_shouldKnowWhatIsInBundle() {
    String[] projection = {};
    String selection = "select";
    String[] selectionArgs = {};
    String sortOrder = "order";
    Bundle queryArgs = new Bundle();
    queryArgs.putString(QUERY_ARG_SQL_SELECTION, selection);
    queryArgs.putStringArray(QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
    queryArgs.putString(QUERY_ARG_SQL_SORT_ORDER, sortOrder);

    QueryParamTrackingCursor testCursor = new QueryParamTrackingCursor();
    shadowContentResolver.setCursor(testCursor);
    Cursor cursor = shadowContentResolver.query(uri21, projection, queryArgs, null);
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
    assertThat(contentResolver.acquireUnstableProvider(uri))
        .isSameInstanceAs(cp.getIContentProvider());
  }

  @Test
  public void acquireUnstableProvider_shouldReturnWithString() {
    ContentProvider cp = mock(ContentProvider.class);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, cp);
    assertThat(contentResolver.acquireUnstableProvider(AUTHORITY))
        .isSameInstanceAs(cp.getIContentProvider());
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
    mock.attachInfo(ApplicationProvider.getApplicationContext(), providerInfo0);
    mock.onCreate();

    ArgumentCaptor<ProviderInfo> captor = ArgumentCaptor.forClass(ProviderInfo.class);
    verify(mock)
        .attachInfo(
            same((Application) ApplicationProvider.getApplicationContext()), captor.capture());
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
    shadowContentResolver.registerInputStream(
        uri21, new ByteArrayInputStream("ourStream".getBytes(UTF_8)));
    InputStream inputStream = contentResolver.openInputStream(uri21);
    byte[] data = new byte[9];
    inputStream.read(data);
    assertThat(new String(data, UTF_8)).isEqualTo("ourStream");
  }

  @Test
  public void openInputStream_returnsNewStreamEachTimeFromRegisteredSupplier() throws Exception {
    shadowContentResolver.registerInputStreamSupplier(
        uri21, () -> new ByteArrayInputStream("ourStream".getBytes(UTF_8)));
    InputStream inputStream1 = contentResolver.openInputStream(uri21);
    byte[] data1 = new byte[9];
    inputStream1.read(data1);
    inputStream1.close();
    InputStream inputStream2 = contentResolver.openInputStream(uri21);
    byte[] data2 = new byte[9];
    inputStream2.read(data2);
    inputStream2.close();
    assertThat(new String(data1, UTF_8)).isEqualTo("ourStream");
    assertThat(new String(data2, UTF_8)).isEqualTo("ourStream");
  }

  @Test
  public void openInputStream_returnsResourceUriStream() throws Exception {
    InputStream inputStream =
        contentResolver.openInputStream(
            new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(ApplicationProvider.getApplicationContext().getPackageName())
                .appendPath(String.valueOf(R.drawable.an_image))
                .build());
    assertThat(inputStream).isNotNull();
    inputStream.read();
  }

  @SuppressLint("NewApi")
  @Test
  public void openInputStream_returnsFileUriStream() throws Exception {
    File file = temporaryFolder.newFile();
    try (FileOutputStream out = new FileOutputStream(file)) {
      out.write("foo".getBytes(UTF_8));
    }

    InputStream inputStream = contentResolver.openInputStream(Uri.fromFile(file));

    assertThat(inputStream).isNotNull();
    assertThat(new String(inputStream.readAllBytes(), UTF_8)).isEqualTo("foo");
  }

  @Test
  public void openInputStream_returnsProviderInputStream() throws Exception {
    ProviderInfo info = new ProviderInfo();
    info.authority = AUTHORITY;
    ContentProvider myContentProvider = new MyContentProvider();
    myContentProvider.attachInfo(ApplicationProvider.getApplicationContext(), info);
    ShadowContentResolver.registerProviderInternal(AUTHORITY, myContentProvider);

    Uri uri = Uri.parse("content://" + AUTHORITY + "/some/path");
    InputStream actualInputStream = contentResolver.openInputStream(uri);
    // Registered provider does not return named stream
    assertThat(actualInputStream).isNotInstanceOf(NamedStream.class);

    Uri otherUri = Uri.parse("content://otherAuthority/some/path");
    InputStream secondInputStream = contentResolver.openInputStream(otherUri);
    // No registered provider results in named stream
    assertThat(secondInputStream).isInstanceOf(NamedStream.class);

    shadowContentResolver.registerInputStreamSupplier(
        uri, () -> new ByteArrayInputStream("ourStream".getBytes(UTF_8)));
    InputStream registeredInputStream = contentResolver.openInputStream(uri);
    byte[] byteArray = new byte[registeredInputStream.available()];
    registeredInputStream.read(byteArray);
    // Explicitly registered stream takes precedence
    assertThat(byteArray).isEqualTo("ourStream".getBytes(UTF_8));
  }

  @Test
  public void openOutputStream_withNoRealOrRegisteredProvider_doesNotThrow() throws Exception {
    Uri uri = Uri.parse("content://invalidauthority/test/1");
    assertThat(contentResolver.openOutputStream(uri)).isNotNull();
  }

  @Test
  public void openOutputStream_withRealContentProvider_canReadBytesWrittenToOutputStream()
      throws IOException, RemoteException {
    Robolectric.setupContentProvider(MyContentProvider.class, AUTHORITY);
    Uri uri = Uri.parse("content://" + AUTHORITY + "/test/1");

    // Write content through given outputstream
    try (OutputStream outputStream = contentResolver.openOutputStream(uri)) {
      outputStream.write("foo".getBytes(UTF_8));
    }

    // Verify written content can be read back
    InputStream inputStream = contentResolver.openInputStream(uri);
    assertThat(new String(inputStream.readAllBytes(), UTF_8)).isEqualTo("foo");
  }

  @Test
  public void openOutputStream_shouldReturnRegisteredStream() throws Exception {
    final Uri uri = Uri.parse("content://registeredProvider/path");

    AtomicInteger callCount = new AtomicInteger();
    OutputStream outputStream =
        new OutputStream() {

          @Override
          public void write(int arg0) throws IOException {
            callCount.incrementAndGet();
          }

          @Override
          public String toString() {
            return "outputstream for " + uri;
          }
        };

    shadowOf(contentResolver).registerOutputStream(uri, outputStream);

    assertThat(callCount.get()).isEqualTo(0);
    contentResolver.openOutputStream(uri).write(5);
    assertThat(callCount.get()).isEqualTo(1);

    contentResolver.openOutputStream(uri21).write(5);
    assertThat(callCount.get()).isEqualTo(1);
  }

  @Test
  public void openOutputStream_shouldReturnNewStreamFromRegisteredSupplier() throws Exception {
    final Uri uri = Uri.parse("content://registeredProvider/path");

    AtomicInteger streamCreateCount = new AtomicInteger();
    shadowOf(contentResolver)
        .registerOutputStreamSupplier(
            uri,
            () -> {
              streamCreateCount.incrementAndGet();
              AtomicBoolean isClosed = new AtomicBoolean();
              isClosed.set(false);
              OutputStream outputStream =
                  new OutputStream() {
                    @Override
                    public void close() {
                      isClosed.set(true);
                    }

                    @Override
                    public void write(int arg0) throws IOException {
                      if (isClosed.get()) {
                        throw new IOException();
                      }
                    }

                    @Override
                    public String toString() {
                      return "outputstream for " + uri;
                    }
                  };
              return outputStream;
            });

    assertThat(streamCreateCount.get()).isEqualTo(0);
    OutputStream outputStream1 = contentResolver.openOutputStream(uri);
    outputStream1.close();
    assertThat(streamCreateCount.get()).isEqualTo(1);

    contentResolver.openOutputStream(uri).write(5);
    assertThat(streamCreateCount.get()).isEqualTo(2);
  }

  @Test
  public void openOutputStream_withModeWithNoRealOrRegisteredProvider_throws() {
    Uri uri = Uri.parse("content://invalidauthority/test/1");
    assertThrows(FileNotFoundException.class, () -> contentResolver.openOutputStream(uri, "wt"));
  }

  @Test
  public void openOutputStream_withModeWithRealContentProvider_canReadBytesWrittenToOutputStream()
      throws IOException, RemoteException {
    Robolectric.setupContentProvider(MyContentProvider.class, AUTHORITY);
    Uri uri = Uri.parse("content://" + AUTHORITY + "/test/1");

    // Write content through given outputstream
    try (OutputStream outputStream = contentResolver.openOutputStream(uri, "wt")) {
      outputStream.write("foo".getBytes(UTF_8));
    }

    // Verify written content can be read back
    InputStream inputStream = contentResolver.openInputStream(uri);
    assertThat(new String(inputStream.readAllBytes(), UTF_8)).isEqualTo("foo");
  }

  @Test
  public void openOutputStream_withModeShouldReturnRegisteredStream() throws Exception {
    final Uri uri = Uri.parse("content://registeredProvider/path");

    AtomicInteger callCount = new AtomicInteger();
    OutputStream outputStream =
        new OutputStream() {

          @Override
          public void write(int arg0) throws IOException {
            callCount.incrementAndGet();
          }

          @Override
          public String toString() {
            return "outputstream for " + uri;
          }
        };

    shadowOf(contentResolver).registerOutputStream(uri, outputStream);

    assertThat(callCount.get()).isEqualTo(0);
    contentResolver.openOutputStream(uri, "wt").write(5);
    assertThat(callCount.get()).isEqualTo(1);
  }

  @Test
  public void openOutputStream_withModeShouldReturnNewStreamFromRegisteredSupplier()
      throws Exception {
    final Uri uri = Uri.parse("content://registeredProvider/path");

    AtomicInteger streamCreateCount = new AtomicInteger();
    shadowOf(contentResolver)
        .registerOutputStreamSupplier(
            uri,
            () -> {
              streamCreateCount.incrementAndGet();
              AtomicBoolean isClosed = new AtomicBoolean();
              isClosed.set(false);
              OutputStream outputStream =
                  new OutputStream() {
                    @Override
                    public void close() {
                      isClosed.set(true);
                    }

                    @Override
                    public void write(int arg0) throws IOException {
                      if (isClosed.get()) {
                        throw new IOException();
                      }
                    }

                    @Override
                    public String toString() {
                      return "outputstream for " + uri;
                    }
                  };
              return outputStream;
            });

    assertThat(streamCreateCount.get()).isEqualTo(0);
    OutputStream outputStream1 = contentResolver.openOutputStream(uri, "wt");
    outputStream1.close();
    assertThat(streamCreateCount.get()).isEqualTo(1);

    contentResolver.openOutputStream(uri, "wt").write(5);
    assertThat(streamCreateCount.get()).isEqualTo(2);
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
  public void applyBatchForRegisteredProvider()
      throws RemoteException, OperationApplicationException {
    final List<String> operations = new ArrayList<>();
    ShadowContentResolver.registerProviderInternal(
        "registeredProvider",
        new ContentProvider() {
          @Override
          public boolean onCreate() {
            return true;
          }

          @Override
          public Cursor query(
              Uri uri,
              String[] projection,
              String selection,
              String[] selectionArgs,
              String sortOrder) {
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
          public int update(
              Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            operations.add("update");
            return 0;
          }
        });

    final Uri uri = Uri.parse("content://registeredProvider/path");
    List<ContentProviderOperation> contentProviderOperations =
        Arrays.asList(
            ContentProviderOperation.newInsert(uri).withValue("a", "b").build(),
            ContentProviderOperation.newUpdate(uri).withValue("a", "b").build(),
            ContentProviderOperation.newDelete(uri).build(),
            ContentProviderOperation.newAssertQuery(uri).withValue("a", "b").build());
    contentResolver.applyBatch("registeredProvider", new ArrayList<>(contentProviderOperations));

    assertThat(operations).containsExactly("insert", "update", "delete", "query");
  }

  @Test
  public void applyBatchForUnregisteredProvider()
      throws RemoteException, OperationApplicationException {
    List<ContentProviderOperation> resultOperations =
        shadowContentResolver.getContentProviderOperations(AUTHORITY);
    assertThat(resultOperations).isNotNull();
    assertThat(resultOperations.size()).isEqualTo(0);

    ContentProviderResult[] contentProviderResults =
        new ContentProviderResult[] {
          new ContentProviderResult(1), new ContentProviderResult(1),
        };
    shadowContentResolver.setContentProviderResult(contentProviderResults);
    Uri uri = Uri.parse("content://org.robolectric");
    ArrayList<ContentProviderOperation> operations = new ArrayList<>();
    operations.add(
        ContentProviderOperation.newInsert(uri)
            .withValue("column1", "foo")
            .withValue("column2", 5)
            .build());
    operations.add(
        ContentProviderOperation.newUpdate(uri)
            .withSelection("id_column", new String[] {"99"})
            .withValue("column1", "bar")
            .build());
    operations.add(
        ContentProviderOperation.newDelete(uri)
            .withSelection("id_column", new String[] {"11"})
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
  public void shouldGetCurrentSyncs() {
    ContentResolver.requestSync(a, AUTHORITY, new Bundle());
    ContentResolver.requestSync(b, AUTHORITY, new Bundle());

    List<SyncInfo> syncs = ContentResolver.getCurrentSyncs();
    assertThat(syncs.size()).isEqualTo(2);

    SyncInfo syncA = Iterables.find(syncs, s -> s.account.equals(a));
    assertThat(syncA.account).isEqualTo(a);
    assertThat(syncA.authority).isEqualTo(AUTHORITY);

    SyncInfo syncB = Iterables.find(syncs, s -> s.account.equals(b));
    assertThat(syncB.account).isEqualTo(b);
    assertThat(syncB.authority).isEqualTo(AUTHORITY);

    ContentResolver.cancelSync(a, AUTHORITY);
    List<SyncInfo> syncsAgain = ContentResolver.getCurrentSyncs();
    assertThat(syncsAgain.size()).isEqualTo(1);

    SyncInfo firstAgain = syncsAgain.get(0);
    assertThat(firstAgain.account).isEqualTo(b);
    assertThat(firstAgain.authority).isEqualTo(AUTHORITY);

    ContentResolver.cancelSync(b, AUTHORITY);
    List<SyncInfo> s = ContentResolver.getCurrentSyncs();
    assertThat(s.size()).isEqualTo(0);
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
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY))
        .containsExactly(
            new PeriodicSync(a, AUTHORITY, fooBar, 6000L),
            new PeriodicSync(a, AUTHORITY, fooBaz, 6000L));
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY))
        .containsExactly(
            new PeriodicSync(b, AUTHORITY, fooBar, 6000L),
            new PeriodicSync(b, AUTHORITY, fooBaz, 6000L));

    // If same extras, but different time, simply update the time.
    ContentResolver.addPeriodicSync(a, AUTHORITY, fooBar, 42L);
    ContentResolver.addPeriodicSync(b, AUTHORITY, fooBaz, 42L);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY))
        .containsExactly(
            new PeriodicSync(a, AUTHORITY, fooBar, 42L),
            new PeriodicSync(a, AUTHORITY, fooBaz, 6000L));
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY))
        .containsExactly(
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

    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY))
        .containsExactly(
            new PeriodicSync(a, AUTHORITY, fooBar, 6000L),
            new PeriodicSync(a, AUTHORITY, fooBaz, 6000L),
            new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, fooBar);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY))
        .containsExactly(
            new PeriodicSync(a, AUTHORITY, fooBaz, 6000L),
            new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, fooBaz);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY))
        .containsExactly(new PeriodicSync(a, AUTHORITY, foo42, 6000L));

    ContentResolver.removePeriodicSync(a, AUTHORITY, foo42);
    assertThat(ShadowContentResolver.getPeriodicSyncs(a, AUTHORITY)).isEmpty();
    assertThat(ShadowContentResolver.getPeriodicSyncs(b, AUTHORITY))
        .containsExactly(
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
    ShadowContentResolver.registerProviderInternal(
        AUTHORITY,
        new ContentProvider() {
          @Override
          public boolean onCreate() {
            return false;
          }

          @Override
          public Cursor query(
              Uri uri,
              String[] projection,
              String selection,
              String[] selectionArgs,
              String sortOrder) {
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
          public int update(
              Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return -1;
          }

          @Override
          public String getType(Uri uri) {
            return null;
          }
        });
    final Uri uri = Uri.parse("content://" + AUTHORITY + "/some/path");
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
  public void shouldThrowConfiguredExceptionWhenRegisteringContentObservers() {
    ShadowContentResolver scr = shadowOf(contentResolver);
    scr.setRegisterContentProviderException(EXTERNAL_CONTENT_URI, new SecurityException());
    try {
      contentResolver.registerContentObserver(
          EXTERNAL_CONTENT_URI, true, new TestContentObserver(null));
      fail();
    } catch (SecurityException expected) {
    }
  }

  @Test
  public void shouldClearConfiguredExceptionForRegisteringContentObservers() {
    ShadowContentResolver scr = shadowOf(contentResolver);
    scr.setRegisterContentProviderException(EXTERNAL_CONTENT_URI, new SecurityException());
    scr.clearRegisterContentProviderException(EXTERNAL_CONTENT_URI);
    // Should not throw the SecurityException.
    contentResolver.registerContentObserver(
        EXTERNAL_CONTENT_URI, true, new TestContentObserver(null));
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

    contentResolver.unregisterContentObserver(co);
    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).isEmpty();
  }

  @Test
  public void shouldUnregisterContentObservers() {
    TestContentObserver co = new TestContentObserver(null);
    ShadowContentResolver scr = shadowOf(contentResolver);
    contentResolver.registerContentObserver(EXTERNAL_CONTENT_URI, true, co);
    assertThat(scr.getContentObservers(EXTERNAL_CONTENT_URI)).contains(co);

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
  public void getProvider_shouldCreateProviderFromManifest() throws Exception {
    Uri uri = Uri.parse("content://org.robolectric.authority1/shadows");
    ContentProvider provider = ShadowContentResolver.getProvider(uri);
    assertThat(provider).isNotNull();
    assertThat(provider.getReadPermission()).isEqualTo("READ_PERMISSION");
    assertThat(provider.getWritePermission()).isEqualTo("WRITE_PERMISSION");
    assertThat(provider.getPathPermissions()).asList().hasSize(1);

    // unfortunately, there is no direct way of testing if authority is set or not
    // however, it's checked in ContentProvider.Transport method calls (validateIncomingUri), so
    // it's the closest we can test against
    if (RuntimeEnvironment.getApiLevel() <= 28) {
      provider.getIContentProvider().getType(uri); // should not throw
    } else {
      // just call validateIncomingUri directly
      provider.validateIncomingUri(uri);
    }
  }

  @Test
  @Config(manifest = NONE)
  @SuppressWarnings("RobolectricSystemContext") // preexisting when check was enabled
  public void getProvider_shouldNotReturnAnyProviderWhenManifestIsNull() {
    Application application = new Application();
    shadowOf(application).callAttach(RuntimeEnvironment.systemContext);
    assertThat(ShadowContentResolver.getProvider(Uri.parse("content://"))).isNull();
  }

  @Test
  public void openTypedAssetFileDescriptor_shouldOpenDescriptor()
      throws IOException, RemoteException {
    Robolectric.setupContentProvider(MyContentProvider.class, AUTHORITY);

    try (AssetFileDescriptor afd =
        contentResolver.openTypedAssetFileDescriptor(
            Uri.parse("content://" + AUTHORITY + "/whatever"), "*/*", null)) {

      FileDescriptor descriptor = afd.getFileDescriptor();
      assertThat(descriptor).isNotNull();
    }
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
    assertThat(permissions.get(0).getUri()).isSameInstanceAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isTrue();
    assertThat(permissions.get(0).isWritePermission()).isFalse();

    // Take the write permission for the uri.
    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    assertThat(permissions).hasSize(1);
    assertThat(permissions.get(0).getUri()).isSameInstanceAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isTrue();
    assertThat(permissions.get(0).isWritePermission()).isTrue();

    // Release the read permission for the uri.
    contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
    assertThat(permissions).hasSize(1);
    assertThat(permissions.get(0).getUri()).isSameInstanceAs(uri);
    assertThat(permissions.get(0).isReadPermission()).isFalse();
    assertThat(permissions.get(0).isWritePermission()).isTrue();

    // Release the write permission for the uri.
    contentResolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    assertThat(permissions).isEmpty();
  }

  @Test
  public void getSyncAdapterTypes() {
    SyncAdapterType[] syncAdapterTypes =
        new SyncAdapterType[] {
          new SyncAdapterType(
              "authority1", "accountType1", /* userVisible=*/ false, /* supportsUploading=*/ false),
          new SyncAdapterType(
              "authority2", "accountType2", /* userVisible=*/ true, /* supportsUploading=*/ false),
          new SyncAdapterType(
              "authority3", "accountType3", /* userVisible=*/ true, /* supportsUploading=*/ true)
        };

    ShadowContentResolver.setSyncAdapterTypes(syncAdapterTypes);
    assertThat(ContentResolver.getSyncAdapterTypes()).isEqualTo(syncAdapterTypes);
  }

  private static class QueryParamTrackingCursor extends BaseCursor {
    public Uri uri;
    public String[] projection;
    public String selection;
    public String[] selectionArgs;
    public String sortOrder;

    @Override
    public void setQuery(
        Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
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

  /** Provider that opens a temporary file. */
  public static class MyContentProvider extends ContentProvider {
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
      final File file =
          new File(ApplicationProvider.getApplicationContext().getFilesDir(), "test_file");
      try {
        file.createNewFile();
      } catch (IOException e) {
        throw new RuntimeException("error creating new file", e);
      }
      return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE);
    }
  }
}
