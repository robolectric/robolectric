package org.robolectric.shadows;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.NamedStream;
import org.robolectric.res.ContentProviderData;
import org.robolectric.tester.android.database.TestCursor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@Implements(ContentResolver.class)
public class ShadowContentResolver {
  private int nextDatabaseIdForInserts;
  private int nextDatabaseIdForUpdates;

  private TestCursor cursor;
  private final List<InsertStatement> insertStatements = new ArrayList<InsertStatement>();
  private final List<UpdateStatement> updateStatements = new ArrayList<UpdateStatement>();
  private final List<DeleteStatement> deleteStatements = new ArrayList<DeleteStatement>();
  private List<NotifiedUri> notifiedUris = new ArrayList<NotifiedUri>();
  private HashMap<Uri, TestCursor> uriCursorMap = new HashMap<Uri, TestCursor>();
  private final Map<String, ArrayList<ContentProviderOperation>> contentProviderOperations = new HashMap<String, ArrayList<ContentProviderOperation>>();
  private ContentProviderResult[] contentProviderResults;

  private final Map<Uri, ContentObserver> contentObservers = new HashMap<Uri,ContentObserver>();

  private static final Map<String, Map<Account, Status>>  syncableAccounts =
      new HashMap<String, Map<Account, Status>>();
  private static final Map<String, ContentProvider> providers = new HashMap<String, ContentProvider>();
  private static boolean masterSyncAutomatically;

  public static void reset() {
    syncableAccounts.clear();
    providers.clear();
    masterSyncAutomatically = false;
  }

  public static class NotifiedUri {
    public final Uri uri;
    public final boolean syncToNetwork;
    public final ContentObserver observer;

    public NotifiedUri(Uri uri, ContentObserver observer, boolean syncToNetwork) {
      this.uri = uri;
      this.syncToNetwork = syncToNetwork;
      this.observer = observer;
    }
  }

  public static class Status {
    public int syncRequests;
    public int state = -1;
    public boolean syncAutomatically;
    public Bundle syncExtras;
    public List<PeriodicSync> syncs = new ArrayList<PeriodicSync>();
  }

  @Implementation
  public final InputStream openInputStream(final Uri uri) {

    if (uri != null && ContentResolver.SCHEME_ANDROID_RESOURCE.equals(uri.getScheme())) {
      String path = uri.getPath();
      // check that path is a numerical resource id
      if (path != null && path.matches("/[0-9]+")) {
        int resourceId = Integer.parseInt(path.substring(1));
        return Robolectric.application.getResources().openRawResource(resourceId);
      }
    }

    return new MyInputStream(uri);
  }

  @Implementation
  public final OutputStream openOutputStream(final Uri uri) {
    return new OutputStream() {

      @Override
      public void write(int arg0) throws IOException {
      }

      @Override
      public String toString() {
        return "outputstream for " + uri;
      }
    };
  }

  @Implementation
  public final Uri insert(Uri url, ContentValues values) {
    ContentProvider provider = getProvider(url);
    if (provider != null) {
      return provider.insert(url, values);
    } else {
      InsertStatement insertStatement = new InsertStatement(url, new ContentValues(values));
      insertStatements.add(insertStatement);
      return Uri.parse(url.toString() + "/" + ++nextDatabaseIdForInserts);
    }
  }

  @Implementation
  public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
    ContentProvider provider = getProvider(uri);
    if (provider != null) {
      return provider.update(uri, values, where, selectionArgs);
    } else {
      UpdateStatement updateStatement = new UpdateStatement(uri, new ContentValues(values), where, selectionArgs);
      updateStatements.add(updateStatement);
      return ++nextDatabaseIdForUpdates;
    }
  }

  @Implementation
  public final Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    ContentProvider provider = getProvider(uri);
    if (provider != null) {
      return provider.query(uri, projection, selection, selectionArgs, sortOrder);
    } else {
      TestCursor returnCursor = getCursor(uri);
      if (returnCursor == null) {
        return null;
      }

      returnCursor.setQuery(uri, projection, selection, selectionArgs,
          sortOrder);
      return returnCursor;
    }
  }
  
  @Implementation
  public String getType(Uri uri) {
    ContentProvider provider = getProvider(uri);
    if (provider != null) {
      return provider.getType(uri);
    } else {
      return null;
    }
  }

  @Implementation
  public final IContentProvider acquireUnstableProvider(Uri uri) {
    ContentProvider cp = getProvider(uri);
    if (cp != null) {
      return cp.getIContentProvider();
    }
    return null;
  }

  @Implementation
  public final int delete(Uri url, String where, String[] selectionArgs) {
    ContentProvider provider = getProvider(url);
    if (provider != null) {
      return provider.delete(url, where, selectionArgs);
    } else {
      DeleteStatement deleteStatement = new DeleteStatement(url, where, selectionArgs);
      deleteStatements.add(deleteStatement);
      return 1;
    }
  }

  @Implementation
  public final int bulkInsert(Uri url, ContentValues[] values) {
    ContentProvider provider = getProvider(url);
    if (provider != null) {
      return provider.bulkInsert(url, values);
    } else {
      return 0;
    }
  }

  @Implementation
  public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    notifiedUris.add(new NotifiedUri(uri, observer, syncToNetwork));

    // TODO does not support multiple observers for a URI
    ContentObserver obs = contentObservers.get(uri);
    if ( obs != null && obs != observer  ) {
      obs.dispatchChange( false, uri );
    }
    if ( observer != null && observer.deliverSelfNotifications() ) {
      observer.dispatchChange( true, uri );
    }
  }

  @Implementation
  public void notifyChange(Uri uri, ContentObserver observer) {
    notifyChange(uri, observer, false);
  }

  @Implementation
  public ContentProviderResult[] applyBatch(String authority, ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
    ContentProvider provider = providers.get(authority);
    if (provider != null) {
      return provider.applyBatch(operations);
    } else {
      contentProviderOperations.put(authority, operations);
      return contentProviderResults;
    }
  }

  @Implementation
  public static void requestSync(Account account, String authority, Bundle extras) {
    validateSyncExtrasBundle(extras);
    Status status = getStatus(account, authority, true);
    status.syncRequests++;
    status.syncExtras = extras;
  }

  @Implementation
  public static void setIsSyncable(Account account, String authority, int syncable) {
    getStatus(account, authority, true).state = syncable;
  }

  @Implementation
  public static int getIsSyncable(Account account, String authority) {
    return getStatus(account, authority, true).state;
  }

  @Implementation
  public static boolean getSyncAutomatically(Account account, String authority) {
    return getStatus(account, authority, true).syncAutomatically;
  }

  @Implementation
  public static void setSyncAutomatically(Account account, String authority, boolean sync) {
    getStatus(account, authority, true).syncAutomatically = sync;
  }

  @Implementation
  public static void addPeriodicSync(Account account, String authority, Bundle extras,
                     long pollFrequency) {

    validateSyncExtrasBundle(extras);
    getStatus(account, authority, true).syncs.add(new PeriodicSync(account, authority, extras, pollFrequency));
  }

  @Implementation
  public static void removePeriodicSync(Account account, String authority, Bundle extras) {
    validateSyncExtrasBundle(extras);
    Status status = getStatus(account, authority);
    if (status != null) status.syncs.clear();
  }

  @Implementation
  public static List<PeriodicSync> getPeriodicSyncs(Account account, String authority) {
    return getStatus(account, authority, true).syncs;
  }

  @Implementation
  public static void validateSyncExtrasBundle(Bundle extras) {
    for (String key : extras.keySet()) {
      Object value = extras.get(key);
      if (value == null) continue;
      if (value instanceof Long) continue;
      if (value instanceof Integer) continue;
      if (value instanceof Boolean) continue;
      if (value instanceof Float) continue;
      if (value instanceof Double) continue;
      if (value instanceof String) continue;
      if (value instanceof Account) continue;
      throw new IllegalArgumentException("unexpected value type: "
          + value.getClass().getName());
    }
  }

  @Implementation
  public static void setMasterSyncAutomatically(boolean sync) {
    masterSyncAutomatically = sync;

  }

  @Implementation
  public static boolean getMasterSyncAutomatically() {
    return masterSyncAutomatically;
  }

  public static ContentProvider getProvider(Uri uri) {
    if (uri == null || !ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
      return null;
    }

    if (!providers.containsKey(uri.getAuthority())) {
      AndroidManifest manifest = Robolectric.getShadowApplication().getAppManifest();
      if (manifest != null) {
        for (ContentProviderData providerData : manifest.getContentProviders()) {
          if (providerData.getAuthority().equals(uri.getAuthority())) {
            providers.put(providerData.getAuthority(), createAndInitialize(providerData));
          }
        }
      }
    }
    return providers.get(uri.getAuthority());
  }

  public static void registerProvider(String authority, ContentProvider provider) {
    providers.put(authority, provider);
  }

  public static Status getStatus(Account account, String authority) {
    return getStatus(account, authority, false);
  }

  public static Status getStatus(Account account, String authority, boolean create) {
    Map<Account, Status> map = syncableAccounts.get(authority);
    if (map == null) {
      map = new HashMap<Account, Status>();
      syncableAccounts.put(authority, map);
    }
    Status status = map.get(account);
    if (status == null && create) {
      status = new Status();
      map.put(account, status);
    }
    return status;
  }

  public void setCursor(TestCursor cursor) {
    this.cursor = cursor;
  }

  public void setCursor(Uri uri, TestCursor cursorForUri) {
    this.uriCursorMap.put(uri, cursorForUri);
  }

  public void setNextDatabaseIdForInserts(int nextId) {
    nextDatabaseIdForInserts = nextId;
  }

  public void setNextDatabaseIdForUpdates(int nextId) {
    nextDatabaseIdForUpdates = nextId;
  }

  public List<InsertStatement> getInsertStatements() {
    return insertStatements;
  }

  public List<UpdateStatement> getUpdateStatements() {
    return updateStatements;
  }

  public List<Uri> getDeletedUris() {
    List<Uri> uris = new ArrayList<Uri>();
    for (DeleteStatement deleteStatement : deleteStatements) {
      uris.add(deleteStatement.getUri());
    }
    return uris;
  }

  public List<DeleteStatement> getDeleteStatements() {
    return deleteStatements;
  }

  public List<NotifiedUri> getNotifiedUris() {
    return notifiedUris;
  }

  public ArrayList<ContentProviderOperation> getContentProviderOperations(String authority) {
    ArrayList<ContentProviderOperation> operations = contentProviderOperations.get(authority);
    if (operations == null)
      return new ArrayList<ContentProviderOperation>();
    return operations;
  }

  public void setContentProviderResult(ContentProviderResult[] contentProviderResults) {
    this.contentProviderResults = contentProviderResults;
  }

  @Implementation
  public void registerContentObserver( Uri uri, boolean notifyForDescendents, ContentObserver observer) {
    // TODO does not support multiple observers for a URI
    contentObservers.put( uri, observer );
  }

  @Implementation
  public void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
    registerContentObserver(uri, notifyForDescendents, observer);
  }

  @Implementation
  public void unregisterContentObserver( ContentObserver observer ) {
    if ( observer != null && contentObservers.containsValue( observer ) ) {
      Set<Entry<Uri,ContentObserver>> entries = contentObservers.entrySet();
      for ( Entry<Uri,ContentObserver> e : entries ) {
        ContentObserver other = e.getValue();
        if ( observer == other || observer.equals(other) ) {
              contentObservers.remove( e.getKey() );
              return;
        }
      }
    }
  }

  /**
   * Non-Android accessor.  Clears the list of registered content observers.
   * Commonly used in test case setup.
   */
  public void clearContentObservers() {
    contentObservers.clear();
  }

  /**
   * Non-Android accessor.  Returns the content observer registered with
   * the given URI, or null if none registered.
   * @param uri
   * @return
   */
  public ContentObserver getContentObserver( Uri uri ) {
    return contentObservers.get(uri);
  }

  private static ContentProvider createAndInitialize(ContentProviderData providerData) {
    try {
      ContentProvider provider = (ContentProvider) Class.forName(providerData.getClassName()).newInstance();
      provider.onCreate();
      return provider;
    } catch (InstantiationException e) {
      throw new RuntimeException("Error instantiating class " + providerData.getClassName());
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Error instantiating class " + providerData.getClassName());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Error instantiating class " + providerData.getClassName());
    }
  }

  private TestCursor getCursor(Uri uri) {
    if (uriCursorMap.get(uri) != null) {
      return uriCursorMap.get(uri);
    } else if (cursor != null) {
      return cursor;
    } else {
      return null;
    }
  }

  public static class InsertStatement {
    private final Uri uri;
    private final ContentValues contentValues;

    public InsertStatement(Uri uri, ContentValues contentValues) {
      this.uri = uri;
      this.contentValues = contentValues;
    }

    public Uri getUri() {
      return uri;
    }

    public ContentValues getContentValues() {
      return contentValues;
    }
  }

  public static class UpdateStatement {
    private final Uri uri;
    private final ContentValues values;
    private final String where;
    private final String[] selectionArgs;

    public UpdateStatement(Uri uri, ContentValues values, String where, String[] selectionArgs) {
      this.uri = uri;
      this.values = values;
      this.where = where;
      this.selectionArgs = selectionArgs;
    }

    public Uri getUri() {
      return uri;
    }

    public ContentValues getContentValues() {
      return values;
    }

    public String getWhere() {
      return where;
    }

    public String[] getSelectionArgs() {
      return selectionArgs;
    }
  }

  public static class DeleteStatement {
    private final Uri uri;
    private final String where;
    private final String[] selectionArgs;

    public DeleteStatement(Uri uri, String where, String[] selectionArgs) {
      this.uri = uri;
      this.where = where;
      this.selectionArgs = selectionArgs;
    }

    public Uri getUri() {
      return uri;
    }

    public String getWhere() {
      return where;
    }

    public String[] getSelectionArgs() {
      return selectionArgs;
    }
  }

  private static class MyInputStream extends InputStream implements NamedStream {
    private final Uri uri;

    public MyInputStream(Uri uri) {
      this.uri = uri;
    }

    @Override
    public int read() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "stream for " + uri;
    }
  }
}
