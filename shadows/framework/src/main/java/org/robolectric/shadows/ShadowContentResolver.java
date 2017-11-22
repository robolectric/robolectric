package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.Shadows.shadowOf;

import android.accounts.Account;
import android.annotation.NonNull;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.UriPermission;
import android.content.pm.ProviderInfo;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.fakes.BaseCursor;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.NamedStream;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(ContentResolver.class)
public class ShadowContentResolver {
  private int nextDatabaseIdForInserts;
  private int nextDatabaseIdForUpdates = -1;

  @RealObject ContentResolver realContentResolver;

  private BaseCursor cursor;
  private final List<Statement> statements = new ArrayList<>();
  private final List<InsertStatement> insertStatements = new ArrayList<>();
  private final List<UpdateStatement> updateStatements = new ArrayList<>();
  private final List<DeleteStatement> deleteStatements = new ArrayList<>();
  private List<NotifiedUri> notifiedUris = new ArrayList<>();
  private Map<Uri, BaseCursor> uriCursorMap = new HashMap<>();
  private Map<Uri, InputStream> inputStreamMap = new HashMap<>();
  private final Map<String, List<ContentProviderOperation>> contentProviderOperations = new HashMap<>();
  private ContentProviderResult[] contentProviderResults;
  private final List<UriPermission> uriPermissions = new ArrayList<>();

  private final CopyOnWriteArrayList<ContentObserverEntry> contentObservers =
      new CopyOnWriteArrayList<>();

  private static final Map<String, Map<Account, Status>> syncableAccounts = new HashMap<>();
  private static final Map<String, ContentProvider> providers = new HashMap<>();
  private static boolean masterSyncAutomatically;

  @Resetter
  synchronized public static void reset() {
    syncableAccounts.clear();
    providers.clear();
    masterSyncAutomatically = false;
  }

  private static class ContentObserverEntry {
    public final Uri uri;
    public final boolean notifyForDescendents;
    public final ContentObserver observer;

    private ContentObserverEntry(Uri uri, boolean notifyForDescendents, ContentObserver observer) {
      this.uri = uri;
      this.notifyForDescendents = notifyForDescendents;
      this.observer = observer;

      if (uri == null || observer == null) {
        throw new NullPointerException();
      }
    }

    public boolean matches(Uri test) {
      if (!Objects.equals(uri.getScheme(), test.getScheme())) {
        return false;
      }
      if (!Objects.equals(uri.getAuthority(), test.getAuthority())) {
        return false;
      }

      String uriPath = uri.getPath();
      String testPath = test.getPath();

      return Objects.equals(uriPath, testPath)
          || notifyForDescendents && testPath != null && testPath.startsWith(uriPath);
    }
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

  /**
   * If a {@link ContentProvider} is registered for the given {@link Uri}, its
   * {@link ContentProvider#insert(Uri, ContentValues)} method will be invoked.
   *
   * Tests can verify that this method was called using {@link #getStatements()} or
   * {@link #getInsertStatements()}.
   *
   * If no appropriate {@link ContentProvider} is found, no action will be taken and
   * a {@link Uri} including the incremented value set with {@link #setNextDatabaseIdForInserts(int)} will returned.
   */
  @Implementation
  public final Uri insert(Uri url, ContentValues values) {
    ContentProvider provider = getProvider(url);
    ContentValues valuesCopy = (values == null) ? null : new ContentValues(values);
    InsertStatement insertStatement = new InsertStatement(url, provider, valuesCopy);
    statements.add(insertStatement);
    insertStatements.add(insertStatement);

    if (provider != null) {
      return provider.insert(url, values);
    } else {
      return Uri.parse(url.toString() + "/" + ++nextDatabaseIdForInserts);
    }
  }

  /**
   * If a {@link ContentProvider} is registered for the given {@link Uri}, its
   * {@link ContentProvider#update(Uri, ContentValues, String, String[])} method will be invoked.
   *
   * Tests can verify that this method was called using {@link #getStatements()} or
   * {@link #getUpdateStatements()}.
   *
   * If no appropriate {@link ContentProvider} is found, no action will be taken and
   * the value set with {@link #setNextDatabaseIdForUpdates(int)} will be incremented and returned.
   *
   * *Note:* the return value in this case will be changed to {@code 1} in a future release of Robolectric.
   */
  @Implementation
  public int update(Uri uri, ContentValues values, String where, String[] selectionArgs) {
    ContentProvider provider = getProvider(uri);
    ContentValues valuesCopy = (values == null) ? null : new ContentValues(values);
    UpdateStatement updateStatement = new UpdateStatement(uri, provider, valuesCopy, where, selectionArgs);
    statements.add(updateStatement);
    updateStatements.add(updateStatement);

    if (provider != null) {
      return provider.update(uri, values, where, selectionArgs);
    } else {
      return nextDatabaseIdForUpdates == -1 ? 1 : ++nextDatabaseIdForUpdates;
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

  /**
   * If a {@link ContentProvider} is registered for the given {@link Uri}, its
   * {@link ContentProvider#delete(Uri, String, String[])} method will be invoked.
   *
   * Tests can verify that this method was called using {@link #getDeleteStatements()}
   * or {@link #getDeletedUris()}.
   *
   * If no appropriate {@link ContentProvider} is found, no action will be taken and
   * {@code 1} will be returned.
   */
  @Implementation
  public final int delete(Uri url, String where, String[] selectionArgs) {
    ContentProvider provider = getProvider(url);

    DeleteStatement deleteStatement = new DeleteStatement(url, provider, where, selectionArgs);
    statements.add(deleteStatement);
    deleteStatements.add(deleteStatement);

    if (provider != null) {
      return provider.delete(url, where, selectionArgs);
    } else {
      return 1;
    }
  }

  /**
   * If a {@link ContentProvider} is registered for the given {@link Uri}, its
   * {@link ContentProvider#bulkInsert(Uri, ContentValues[])} method will be invoked.
   *
   * Tests can verify that this method was called using {@link #getStatements()} or
   * {@link #getInsertStatements()}.
   *
   * If no appropriate {@link ContentProvider} is found, no action will be taken and
   * the number of rows in {@code values} will be returned.
   */
  @Implementation
  public final int bulkInsert(Uri url, ContentValues[] values) {
    ContentProvider provider = getProvider(url);

    InsertStatement insertStatement = new InsertStatement(url, provider, values);
    statements.add(insertStatement);
    insertStatements.add(insertStatement);

    if (provider != null) {
      return provider.bulkInsert(url, values);
    } else {
      return values.length;
    }
  }

  @Implementation
  public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    notifiedUris.add(new NotifiedUri(uri, observer, syncToNetwork));

    for (ContentObserverEntry entry : contentObservers) {
      if (entry.matches(uri) && entry.observer != observer) {
        entry.observer.dispatchChange(false, uri);
      }
    }
    if (observer != null && observer.deliverSelfNotifications()) {
      observer.dispatchChange(true, uri);
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
      throw new IllegalArgumentException("unexpected value type: " + value.getClass().getName());
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

  @Implementation(minSdk = KITKAT)
  public void takePersistableUriPermission(@NonNull Uri uri, int modeFlags) {
    Objects.requireNonNull(uri, "uri may not be null");
    modeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

    // If neither read nor write permission is specified there is nothing to do.
    if (modeFlags == 0) {
      return;
    }

    // Attempt to locate an existing record for the uri.
    for (Iterator<UriPermission> i = uriPermissions.iterator(); i.hasNext();) {
      UriPermission perm = i.next();
      if (uri.equals(perm.getUri())) {
        if (perm.isReadPermission()) {
          modeFlags |= Intent.FLAG_GRANT_READ_URI_PERMISSION;
        }
        if (perm.isWritePermission()) {
          modeFlags |= Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        }
        i.remove();
        break;
      }
    }

    addUriPermission(uri, modeFlags);
  }

  @Implementation(minSdk = KITKAT)
  public void releasePersistableUriPermission(@NonNull Uri uri, int modeFlags) {
    Objects.requireNonNull(uri, "uri may not be null");
    modeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

    // If neither read nor write permission is specified there is nothing to do.
    if (modeFlags == 0) {
      return;
    }

    // Attempt to locate an existing record for the uri.
    for (Iterator<UriPermission> i = uriPermissions.iterator(); i.hasNext();) {
      UriPermission perm = i.next();
      if (uri.equals(perm.getUri())) {
        // Reconstruct the current mode flags.
        int oldModeFlags = (perm.isReadPermission() ? Intent.FLAG_GRANT_READ_URI_PERMISSION : 0)
            | (perm.isWritePermission() ? Intent.FLAG_GRANT_WRITE_URI_PERMISSION : 0);

        // Apply the requested permission change.
        int newModeFlags = oldModeFlags & ~modeFlags;

        // Update the permission record if a change occurred.
        if (newModeFlags != oldModeFlags) {
          i.remove();
          if (newModeFlags != 0) {
            addUriPermission(uri, newModeFlags);
          }
        }
        break;
      }
    }
  }

  @Implementation(minSdk = KITKAT)
  public @NonNull List<UriPermission> getPersistedUriPermissions() {
    return uriPermissions;
  }

  private void addUriPermission(@NonNull Uri uri, int modeFlags) {
    ClassParameter<Uri> p1 = new ClassParameter<>(Uri.class, uri);
    ClassParameter<Integer> p2 = new ClassParameter<>(int.class, modeFlags);
    ClassParameter<Long> p3 = new ClassParameter<>(long.class, System.currentTimeMillis());
    UriPermission perm = ReflectionHelpers.callConstructor(UriPermission.class, p1, p2, p3);
    uriPermissions.add(perm);
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
   * Internal-only method, do not use!
   *
   * Instead, use
   * ```java
   * ProviderInfo info = new ProviderInfo();
   * info.authority = authority;
   * Robolectric.buildContentProvider(ContentProvider.class).create(info);
   * ```
   */
  synchronized public static void registerProviderInternal(String authority, ContentProvider provider) {
    providers.put(authority, provider);
  }

  public static Status getStatus(Account account, String authority) {
    return getStatus(account, authority, false);
  }

  /**
   * Retrieve information on the status of the given account.
   *
   * @param account the account
   * @param authority the authority
   * @param create whether to create if no such account is found
   * @return the account's status
   */
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

  @SuppressWarnings({"unused", "WeakerAccess"})
  public void setNextDatabaseIdForInserts(int nextId) {
    nextDatabaseIdForInserts = nextId;
  }

  /**
   * Set the value to be returned by {@link ContentResolver#update(Uri, ContentValues, String, String[])}
   * when no appropriate {@link ContentProvider} can be found.
   *
   * @deprecated This method will be removed in Robolectric 3.5. Instead, {@code 1} will be returned.
   *
   * @param nextId the number of rows to return
   */
  @Deprecated
  @SuppressWarnings({"unused", "WeakerAccess"})
  public void setNextDatabaseIdForUpdates(int nextId) {
    nextDatabaseIdForUpdates = nextId;
  }

  /**
   * Returns the list of {@link InsertStatement}s, {@link UpdateStatement}s, and
   * {@link DeleteStatement}s invoked on this {@link ContentResolver}.
   *
   * @return a list of statements
   */
  @SuppressWarnings({"unused", "WeakerAccess"})
  public List<Statement> getStatements() {
    return statements;
  }

  /**
   * Returns the list of {@link InsertStatement}s for corresponding calls to
   * {@link ContentResolver#insert(Uri, ContentValues)} or
   * {@link ContentResolver#bulkInsert(Uri, ContentValues[])}.
   *
   * @return a list of insert statements
   */
  @SuppressWarnings({"unused", "WeakerAccess"})
  public List<InsertStatement> getInsertStatements() {
    return insertStatements;
  }

  /**
   * Returns the list of {@link UpdateStatement}s for corresponding calls to
   * {@link ContentResolver#update(Uri, ContentValues, String, String[])}.
   *
   * @return a list of update statements
   */
  @SuppressWarnings({"unused", "WeakerAccess"})
  public List<UpdateStatement> getUpdateStatements() {
    return updateStatements;
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
  public List<Uri> getDeletedUris() {
    List<Uri> uris = new ArrayList<>();
    for (DeleteStatement deleteStatement : deleteStatements) {
      uris.add(deleteStatement.getUri());
    }
    return uris;
  }

  /**
   * Returns the list of {@link DeleteStatement}s for corresponding calls to
   * {@link ContentResolver#delete(Uri, String, String[])}.
   *
   * @return a list of delete statements
   */
  @SuppressWarnings({"unused", "WeakerAccess"})
  public List<DeleteStatement> getDeleteStatements() {
    return deleteStatements;
  }

  @SuppressWarnings({"unused", "WeakerAccess"})
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
  public void registerContentObserver(
      Uri uri, boolean notifyForDescendents, ContentObserver observer) {
    if (uri == null || observer == null) {
      throw new NullPointerException();
    }
    contentObservers.add(new ContentObserverEntry(uri, notifyForDescendents, observer));
  }

  @Implementation
  public void registerContentObserver(
      Uri uri, boolean notifyForDescendents, ContentObserver observer, int userHandle) {
    registerContentObserver(uri, notifyForDescendents, observer);
  }

  @Implementation
  public void unregisterContentObserver(ContentObserver observer) {
    synchronized (contentObservers) {
      for (ContentObserverEntry entry : contentObservers) {
        if (entry.observer == observer) {
          contentObservers.remove(entry);
        }
      }
    }
  }

  /** @deprecated Do not use this method. */
  @Deprecated
  public void clearContentObservers() {
    contentObservers.clear();
  }

  /**
   * Returns the content observers registered for updates under the given URI.
   *
   * <p>Will be empty if no observer is registered.
   *
   * @param uri Given URI
   * @return The content observers, or null.
   */
  public Collection<ContentObserver> getContentObservers(Uri uri) {
    ArrayList<ContentObserver> observers = new ArrayList<>(1);
    for (ContentObserverEntry entry : contentObservers) {
      if (entry.matches(uri)) {
        observers.add(entry.observer);
      }
    }
    return observers;
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

  public static class Statement {
    private final Uri uri;
    private final ContentProvider contentProvider;

    Statement(Uri uri, ContentProvider contentProvider) {
      this.uri = uri;
      this.contentProvider = contentProvider;
    }

    public Uri getUri() {
      return uri;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ContentProvider getContentProvider() {
      return contentProvider;
    }
  }

  public static class InsertStatement extends Statement {
    private final ContentValues[] bulkContentValues;

    InsertStatement(Uri uri, ContentProvider contentProvider, ContentValues contentValues) {
      super(uri, contentProvider);
      this.bulkContentValues = new ContentValues[]{contentValues};
    }

    InsertStatement(Uri uri, ContentProvider contentProvider, ContentValues[] bulkContentValues) {
      super(uri, contentProvider);
      this.bulkContentValues = bulkContentValues;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ContentValues getContentValues() {
      if (bulkContentValues.length != 1) {
        throw new ArrayIndexOutOfBoundsException("bulk insert, use getBulkContentValues() instead");
      }
      return bulkContentValues[0];
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ContentValues[] getBulkContentValues() {
      return bulkContentValues;
    }
  }

  public static class UpdateStatement extends Statement {
    private final ContentValues values;
    private final String where;
    private final String[] selectionArgs;

    UpdateStatement(Uri uri, ContentProvider contentProvider, ContentValues values, String where, String[] selectionArgs) {
      super(uri, contentProvider);
      this.values = values;
      this.where = where;
      this.selectionArgs = selectionArgs;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ContentValues getContentValues() {
      return values;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String getWhere() {
      return where;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String[] getSelectionArgs() {
      return selectionArgs;
    }
  }

  public static class DeleteStatement extends Statement {
    private final String where;
    private final String[] selectionArgs;

    DeleteStatement(Uri uri, ContentProvider contentProvider, String where, String[] selectionArgs) {
      super(uri, contentProvider);
      this.where = where;
      this.selectionArgs = selectionArgs;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String getWhere() {
      return where;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public String[] getSelectionArgs() {
      return selectionArgs;
    }
  }

  private static class UnregisteredInputStream extends InputStream implements NamedStream {
    private final Uri uri;

    UnregisteredInputStream(Uri uri) {
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
