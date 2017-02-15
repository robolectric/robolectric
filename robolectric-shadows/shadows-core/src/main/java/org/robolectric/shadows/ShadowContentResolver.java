package org.robolectric.shadows;

import android.accounts.Account;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.BaseCursor;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.util.NamedStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.content.ContentResolver}.
 */
@Implements(ContentResolver.class)
public class ShadowContentResolver {
  private int nextDatabaseIdForInserts;
  private int nextDatabaseIdForUpdates;

  @RealObject ContentResolver realContentResolver;

  private BaseCursor cursor;
  private final List<InsertStatement> insertStatements = new ArrayList<>();
  private final List<UpdateStatement> updateStatements = new ArrayList<>();
  private final List<DeleteStatement> deleteStatements = new ArrayList<>();
  private List<NotifiedUri> notifiedUris = new ArrayList<>();
  private Map<Uri, BaseCursor> uriCursorMap = new HashMap<>();
  private Map<Uri, InputStream> inputStreamMap = new HashMap<>();
  private final Map<String, List<android.content.ContentProviderOperation>> contentProviderOperations = new HashMap<>();
  private ContentProviderResult[] contentProviderResults;

  private final Map<Uri, CopyOnWriteArraySet<ContentObserver>> contentObservers = new HashMap<>();

  private static final Map<String, Map<Account, Status>>  syncableAccounts =
      new HashMap<>();
  private static final Map<String, ContentProvider> providers = new HashMap<>();
  private static boolean masterSyncAutomatically;

  @Resetter
  synchronized public static void reset() {
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
    public List<PeriodicSync> syncs = new ArrayList<>();
  }

  public void registerInputStream(Uri uri, InputStream inputStream) {
    inputStreamMap.put(uri, inputStream);
  }

  @Implementation
  public final InputStream openInputStream(final Uri uri) {
    InputStream inputStream = inputStreamMap.get(uri);
    if (inputStream != null) {
      return inputStream;
    } else {
      return new UnregisteredInputStream(uri);
    }
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
      BaseCursor returnCursor = getCursor(uri);
      if (returnCursor == null) {
        return null;
      }

      returnCursor.setQuery(uri, projection, selection, selectionArgs, sortOrder);
      return returnCursor;
    }
  }

  @Implementation
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal) {
    ContentProvider provider = getProvider(uri);
    if (provider != null) {
      return provider.query(uri, projection, selection, selectionArgs, sortOrder, cancellationSignal);
    } else {
      BaseCursor returnCursor = getCursor(uri);
      if (returnCursor == null) {
        return null;
      }

      returnCursor.setQuery(uri, projection, selection, selectionArgs, sortOrder);
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
  public Bundle call(Uri uri, String method, String arg, Bundle extras) {
    ContentProvider cp = getProvider(uri);
    if (cp != null) {
      return cp.call(method, arg, extras);
    } else {
      return null;
    }
  }

  @Implementation
  public final ContentProviderClient acquireContentProviderClient(String name) {
    ContentProvider provider = getProvider(name);
    if (provider == null) return null;
    return getContentProviderClient(provider, true);
  }

  @Implementation
  public final ContentProviderClient acquireContentProviderClient(Uri uri) {
    ContentProvider provider = getProvider(uri);
    if (provider == null) return null;
    return getContentProviderClient(provider, true);
  }

  @Implementation
  public final ContentProviderClient acquireUnstableContentProviderClient(String name) {
    ContentProvider provider = getProvider(name);
    if (provider == null) return null;
    return getContentProviderClient(provider, false);
  }

  @Implementation
  public final ContentProviderClient acquireUnstableContentProviderClient(Uri uri) {
    ContentProvider provider = getProvider(uri);
    if (provider == null) return null;
    return getContentProviderClient(provider, false);
  }

  private ContentProviderClient getContentProviderClient(ContentProvider provider, boolean stable) {
    ContentProviderClient client =
        Shadow.newInstance(ContentProviderClient.class,
            new Class[]{ContentResolver.class, IContentProvider.class, boolean.class},
            new Object[]{realContentResolver, provider.getIContentProvider(), stable});
    shadowOf(client).setContentProvider(provider);
    return client;
  }

  @Implementation
  public final IContentProvider acquireProvider(String name) {
    return acquireUnstableProvider(name);
  }

  @Implementation
  public final IContentProvider acquireProvider(Uri uri) {
    return acquireUnstableProvider(uri);
  }

  @Implementation
  public final IContentProvider acquireUnstableProvider(String name) {
    ContentProvider cp = getProvider(name);
    if (cp != null) {
      return cp.getIContentProvider();
    }
    return null;
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

    CopyOnWriteArraySet<ContentObserver> observers;
    synchronized (this) {
      observers = contentObservers.get(uri);
    }
    if (observers != null) {
      for (ContentObserver obs : observers) {
        if ( obs != null && obs != observer  ) {
          obs.dispatchChange( false, uri );
        }
      }
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
    ContentProvider provider = getProvider(authority);
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
  public static void cancelSync(Account account, String authority) {
    Status status = getStatus(account, authority);
    if (status != null) {
      status.syncRequests = 0;
      if (status.syncExtras != null) {
        status.syncExtras.clear();
      }
      // This may be too much, as the above should be sufficient.
      if (status.syncs != null) {
        status.syncs.clear();
      }
    }
  }

  @Implementation
  public static boolean isSyncActive(Account account, String authority) {
    ShadowContentResolver.Status status = getStatus(account, authority);
    // TODO: this means a sync is *perpetually* active after one request
    return status != null && status.syncRequests > 0;
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
  public static void addPeriodicSync(Account account, String authority, Bundle extras, long pollFrequency) {
    validateSyncExtrasBundle(extras);
    removePeriodicSync(account, authority, extras);
    getStatus(account, authority, true).syncs.add(new PeriodicSync(account, authority, extras, pollFrequency));
  }

  @Implementation
  public static void removePeriodicSync(Account account, String authority, Bundle extras) {
    validateSyncExtrasBundle(extras);
    Status status = getStatus(account, authority);
    if (status != null) {
      for (int i = 0; i < status.syncs.size(); ++i) {
        if (isBundleEqual(extras, status.syncs.get(i).extras)) {
          status.syncs.remove(i);
          break;
        }
      }
    }
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
    return getProvider(uri.getAuthority());
  }

  synchronized private static ContentProvider getProvider(String authority) {
    if (!providers.containsKey(authority)) {
      AndroidManifest manifest = shadowOf(RuntimeEnvironment.application).getAppManifest();
      if (manifest != null) {
        for (ContentProviderData providerData : manifest.getContentProviders()) {
          // todo: handle multiple authorities
          if (providerData.getAuthorities().equals(authority)) {
            providers.put(providerData.getAuthorities(), createAndInitialize(providerData));
          }
        }
      }
    }
    return providers.get(authority);
  }

  /**
   * @deprecated
   * Instead, use
   * <pre>
   * {@code
   * ProviderInfo info = new ProviderInfo();
   * info.authority = authority;
   * Robolectric.buildContentProvider(ContentProvider.class).create(info);
   * }</pre>
   */
  @Deprecated
  synchronized public static void registerProvider(String authority, ContentProvider provider) {
    initialize(provider, authority);
    providers.put(authority, provider);
  }

  synchronized public static void registerProviderInternal(String authority, ContentProvider provider) {
    providers.put(authority, provider);
  }

  public static Status getStatus(Account account, String authority) {
    return getStatus(account, authority, false);
  }

  public static Status getStatus(Account account, String authority, boolean create) {
    Map<Account, Status> map = syncableAccounts.get(authority);
    if (map == null) {
      map = new HashMap<>();
      syncableAccounts.put(authority, map);
    }
    Status status = map.get(account);
    if (status == null && create) {
      status = new Status();
      map.put(account, status);
    }
    return status;
  }

  public void setCursor(BaseCursor cursor) {
    this.cursor = cursor;
  }

  public void setCursor(Uri uri, BaseCursor cursorForUri) {
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
    List<Uri> uris = new ArrayList<>();
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

  public List<ContentProviderOperation> getContentProviderOperations(String authority) {
    List<ContentProviderOperation> operations = contentProviderOperations.get(authority);
    if (operations == null)
      return new ArrayList<>();
    return operations;
  }

  public void setContentProviderResult(ContentProviderResult[] contentProviderResults) {
    this.contentProviderResults = contentProviderResults;
  }

  @Implementation
  synchronized public void registerContentObserver( Uri uri, boolean notifyForDescendents, ContentObserver observer) {
    CopyOnWriteArraySet<ContentObserver> observers = contentObservers.get(uri);
    if (observers == null) {
      observers = new CopyOnWriteArraySet<>();
      contentObservers.put(uri, observers);
    }
    observers.add(observer);
  }

  @Implementation
  public void registerContentObserver(Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
    registerContentObserver(uri, notifyForDescendents, observer);
  }

  @Implementation
  public void unregisterContentObserver( ContentObserver observer ) {
    if ( observer != null ) {
      Collection<CopyOnWriteArraySet<ContentObserver>> observerSets;
      synchronized (this) {
        observerSets = contentObservers.values();
      }
      for (CopyOnWriteArraySet<ContentObserver> observers : observerSets) {
        observers.remove(observer);
      }
    }
  }

  /**
   * Non-Android accessor.  Clears the list of registered content observers.
   * Commonly used in test case setup.
   */
  synchronized public void clearContentObservers() {
    contentObservers.clear();
  }

  /**
   * Non-Android accessor.  Returns one (which one is unspecified) of the content observers registered with
   * the given URI, or null if none is registered.
   * @param uri Given URI
   * @return The content observer
   *
   * @deprecated This method return random observer, {@link #getContentObservers} should be used instead.
   */
  @Deprecated
  public ContentObserver getContentObserver( Uri uri ) {
    Collection<ContentObserver> observers = getContentObservers(uri);
    return observers.isEmpty() ? null : observers.iterator().next();
  }


  /**
   * Non-Android accessor. Returns the content observers registered with
   * the given URI, will be empty if no observer is registered.
   * @param uri Given URI
   * @return The content observers
   */
  synchronized public Collection<ContentObserver> getContentObservers( Uri uri ) {
    CopyOnWriteArraySet<ContentObserver> observers = contentObservers.get(uri);
    return (observers == null) ? Collections.<ContentObserver>emptyList() : observers;
  }

  @Implementation
  public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) throws FileNotFoundException {
    ContentProvider provider = getProvider(uri);
    if (provider == null) {
      return null;
    }
    return provider.openTypedAssetFile(uri, mimeType, opts);
  }

  private static ContentProvider createAndInitialize(ContentProviderData providerData) {
    try {
      ContentProvider provider = (ContentProvider) Class.forName(providerData.getClassName()).newInstance();
      initialize(provider, providerData.getAuthorities());
      return provider;
    } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
      throw new RuntimeException("Error instantiating class " + providerData.getClassName());
    }
  }

  private static void initialize(ContentProvider provider, String authorities) {
    ProviderInfo providerInfo = new ProviderInfo();
    providerInfo.authority = authorities; // todo: support multiple authorities
    providerInfo.grantUriPermissions = true;
    provider.attachInfo(RuntimeEnvironment.application, providerInfo);
    provider.onCreate();
  }

  private BaseCursor getCursor(Uri uri) {
    if (uriCursorMap.get(uri) != null) {
      return uriCursorMap.get(uri);
    } else if (cursor != null) {
      return cursor;
    } else {
      return null;
    }
  }

  private static boolean isBundleEqual(Bundle bundle1, Bundle bundle2) {
    if (bundle1 == null || bundle2 == null) {
      return false;
    }
    if (bundle1.size() != bundle2.size()) {
      return false;
    }
    for (String key : bundle1.keySet()) {
      if (!bundle1.get(key).equals(bundle2.get(key))) {
        return false;
      }
    }
    return true;
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

  private static class UnregisteredInputStream extends InputStream implements NamedStream {
    private final Uri uri;

    public UnregisteredInputStream(Uri uri) {
      this.uri = uri;
    }

    @Override
    public int read() throws IOException {
      throw new UnsupportedOperationException("You must use ShadowContentResolver.registerInputStream() in order to call read()");
    }

    @Override
    public String toString() {
      return "stream for " + uri;
    }
  }
}
