/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xtremelabs.robolectric.shadows;

import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Implements(ContentResolver.class)
public class ShadowContentResolver {

    private static final String TAG = "ContentResolver";

    private static final Map<String, ContentProvider> providers =
            new HashMap<String, ContentProvider>();

    private static class ContentObserverRegistration {
        final Uri uri;
        final boolean notifyForDescendents;
        final ContentObserver observer;

        private ContentObserverRegistration(
                Uri uri,
                boolean notifyForDescendents,
                ContentObserver observer) {
            this.uri = uri;
            this.notifyForDescendents = notifyForDescendents;
            this.observer = observer;
        }
    }

    private Context mContext;
    private List<ContentObserverRegistration> contentObserverRegistrationList =
            new ArrayList<ContentObserverRegistration>();

    /**
     * @deprecated instead use
     * {@link #requestSync(android.accounts.Account, String, android.os.Bundle)}
     */
    @Deprecated
    public static final String SYNC_EXTRAS_ACCOUNT = "account";
    public static final String SYNC_EXTRAS_EXPEDITED = "expedited";
    /**
     * @deprecated instead use`
     * {@link #SYNC_EXTRAS_MANUAL}
     */
    @Deprecated
    public static final String SYNC_EXTRAS_FORCE = "force";

    /**
     * If this extra is set to true then the sync settings (like getSyncAutomatically())
     * are ignored by the sync scheduler.
     */
    public static final String SYNC_EXTRAS_IGNORE_SETTINGS = "ignore_settings";

    /**
     * If this extra is set to true then any backoffs for the initial attempt (e.g. due to retries)
     * are ignored by the sync scheduler. If this request fails and gets rescheduled then the
     * retries will still honor the backoff.
     */
    public static final String SYNC_EXTRAS_IGNORE_BACKOFF = "ignore_backoff";

    /**
     * If this extra is set to true then the request will not be retried if it fails.
     */
    public static final String SYNC_EXTRAS_DO_NOT_RETRY = "do_not_retry";

    /**
     * Setting this extra is the equivalent of setting both {@link #SYNC_EXTRAS_IGNORE_SETTINGS}
     * and {@link #SYNC_EXTRAS_IGNORE_BACKOFF}
     */
    public static final String SYNC_EXTRAS_MANUAL = "force";

    public static final String SYNC_EXTRAS_UPLOAD = "upload";
    public static final String SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS = "deletions_override";
    public static final String SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS = "discard_deletions";

    /**
     * Set by the SyncManager to request that the SyncAdapter initialize itself for
     * the given account/authority pair. One required initialization step is to
     * ensure that {@link #setIsSyncable(android.accounts.Account, String, int)} has been
     * called with a >= 0 value. When this flag is set the SyncAdapter does not need to
     * do a full sync, though it is allowed to do so.
     */
    public static final String SYNC_EXTRAS_INITIALIZE = "initialize";

    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    public static final String SCHEME_FILE = "file";

    /**
     * This is the Android platform's base MIME type for a content: URI
     * containing a Cursor of a single item.  Applications should use this
     * as the base type along with their own sub-type of their content: URIs
     * that represent a particular item.  For example, hypothetical IMAP email
     * client may have a URI
     * <code>content://com.company.provider.imap/inbox/1</code> for a particular
     * message in the inbox, whose MIME type would be reported as
     * <code>CURSOR_ITEM_BASE_TYPE + "/vnd.company.imap-msg"</code>
     *
     * <p>Compare with {@link #CURSOR_DIR_BASE_TYPE}.
     */
    public static final String CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item";

    /**
     * This is the Android platform's base MIME type for a content: URI
     * containing a Cursor of zero or more items.  Applications should use this
     * as the base type along with their own sub-type of their content: URIs
     * that represent a directory of items.  For example, hypothetical IMAP email
     * client may have a URI
     * <code>content://com.company.provider.imap/inbox</code> for all of the
     * messages in its inbox, whose MIME type would be reported as
     * <code>CURSOR_DIR_BASE_TYPE + "/vnd.company.imap-msg"</code>
     *
     * <p>Note how the base MIME type varies between this and
     * {@link #CURSOR_ITEM_BASE_TYPE} depending on whether there is
     * one single item or multiple items in the data set, while the sub-type
     * remains the same because in either case the data structure contained
     * in the cursor is the same.
     */
    public static final String CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir";

    /** @hide */
    public static final int SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS = 1;
    /** @hide */
    public static final int SYNC_ERROR_AUTHENTICATION = 2;
    /** @hide */
    public static final int SYNC_ERROR_IO = 3;
    /** @hide */
    public static final int SYNC_ERROR_PARSE = 4;
    /** @hide */
    public static final int SYNC_ERROR_CONFLICT = 5;
    /** @hide */
    public static final int SYNC_ERROR_TOO_MANY_DELETIONS = 6;
    /** @hide */
    public static final int SYNC_ERROR_TOO_MANY_RETRIES = 7;
    /** @hide */
    public static final int SYNC_ERROR_INTERNAL = 8;

    public static final int SYNC_OBSERVER_TYPE_SETTINGS = 1<<0;
    public static final int SYNC_OBSERVER_TYPE_PENDING = 1<<1;
    public static final int SYNC_OBSERVER_TYPE_ACTIVE = 1<<2;
    /** @hide */
    public static final int SYNC_OBSERVER_TYPE_STATUS = 1<<3;
    /** @hide */
    public static final int SYNC_OBSERVER_TYPE_ALL = 0x7fffffff;

    // Always log queries which take 500ms+; shorter queries are
    // sampled accordingly.
    private static final int SLOW_THRESHOLD_MILLIS = 500;
    private final Random mRandom = new Random();  // guarded by itself

    public void __constructor__(Context context) {
        mContext = context;
    }

    public static synchronized void registerProvider(String authority, ContentProvider provider) {
        providers.put(authority, provider);
    }

    public static synchronized void reset() {
        providers.clear();
    }

    public static ContentProvider acquireProvider(Uri uri) {
        if (!SCHEME_CONTENT.equals(uri.getScheme())) {
            return null;
        }
        return acquireProvider(uri.getAuthority());
    }

    public synchronized static ContentProvider acquireProvider(String authority) {
        ContentProvider contentProvider = providers.get(authority);
        if (contentProvider == null) {
            throw new RuntimeException("No content provider for " + authority);
        }
        return contentProvider;
    }

    /**
     * Return the MIME type of the given content URL.
     *
     * @param url A Uri identifying content (either a list or specific type),
     * using the content:// scheme.
     * @return A MIME type for the content, or null if the URL is invalid or the type is unknown
     */
    @Implementation
    public final String getType(Uri url) {
        return acquireProvider(url).getType(url);
    }

    /**
     * <p>
     * Query the given URI, returning a {@link Cursor} over the result set.
     * </p>
     * <p>
     * For best performance, the caller should follow these guidelines:
     * <ul>
     * <li>Provide an explicit projection, to prevent
     * reading data from storage that aren't going to be used.</li>
     * <li>Use question mark parameter markers such as 'phone=?' instead of
     * explicit values in the {@code selection} parameter, so that queries
     * that differ only by those values will be recognized as the same
     * for caching purposes.</li>
     * </ul>
     * </p>
     *
     * @param uri The URI, using the content:// scheme, for the content to
     *         retrieve.
     * @param projection A list of which columns to return. Passing null will
     *         return all columns, which is inefficient.
     * @param selection A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in the order that they
     *         appear in the selection. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY
     *         clause (excluding the ORDER BY itself). Passing null will use the
     *         default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry, or null
     * @see Cursor
     */
    @Implementation
    public final Cursor query(Uri uri, String[] projection,
                              String selection, String[] selectionArgs, String sortOrder) {
        return acquireProvider(uri).query(uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * Open a stream on to the content associated with a content URI.  If there
     * is no data associated with the URI, FileNotFoundException is thrown.
     *
     * <h5>Accepts the following URI schemes:</h5>
     * <ul>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * </ul>
     *
     * <p>See {@link #openAssetFileDescriptor(Uri, String)} for more information
     * on these schemes.
     *
     * @param uri The desired URI.
     * @return InputStream
     * @throws java.io.FileNotFoundException if the provided URI could not be opened.
     * @see #openAssetFileDescriptor(Uri, String)
     */
    @Implementation
    public final InputStream openInputStream(Uri uri) throws FileNotFoundException {
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            // Note: left here to avoid breaking compatibility.  May be removed
            // with sufficient testing.
            OpenResourceIdResult r = getResourceId(uri);
            try {
                InputStream stream = r.r.openRawResource(r.id);
                return stream;
            } catch (Resources.NotFoundException ex) {
                throw new FileNotFoundException("Resource does not exist: " + uri);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            // Note: left here to avoid breaking compatibility.  May be removed
            // with sufficient testing.
            return new FileInputStream(uri.getPath());
        } else {
            AssetFileDescriptor fd = openAssetFileDescriptor(uri, "r");
            try {
                return fd != null ? fd.createInputStream() : null;
            } catch (IOException e) {
                throw new FileNotFoundException("Unable to create stream");
            }
        }
    }

    /**
     * Synonym for {@link #openOutputStream(Uri, String)
     * openOutputStream(uri, "w")}.
     * @throws FileNotFoundException if the provided URI could not be opened.
     */
    @Implementation
    public final OutputStream openOutputStream(Uri uri)
            throws FileNotFoundException {
        return openOutputStream(uri, "w");
    }

    /**
     * Open a stream on to the content associated with a content URI.  If there
     * is no data associated with the URI, FileNotFoundException is thrown.
     *
     * <h5>Accepts the following URI schemes:</h5>
     * <ul>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * </ul>
     *
     * <p>See {@link #openAssetFileDescriptor(Uri, String)} for more information
     * on these schemes.
     *
     * @param uri The desired URI.
     * @param mode May be "w", "wa", "rw", or "rwt".
     * @return OutputStream
     * @throws FileNotFoundException if the provided URI could not be opened.
     * @see #openAssetFileDescriptor(Uri, String)
     */
    @Implementation
    public final OutputStream openOutputStream(Uri uri, String mode)
            throws FileNotFoundException {
        AssetFileDescriptor fd = openAssetFileDescriptor(uri, mode);
        try {
            return fd != null ? fd.createOutputStream() : null;
        } catch (IOException e) {
            throw new FileNotFoundException("Unable to create stream");
        }
    }

    /**
     * Open a raw file descriptor to access data under a URI.  This
     * is like {@link #openAssetFileDescriptor(Uri, String)}, but uses the
     * underlying {@link ContentProvider#openFile}
     * ContentProvider.openFile()} method, so will <em>not</em> work with
     * providers that return sub-sections of files.  If at all possible,
     * you should use {@link #openAssetFileDescriptor(Uri, String)}.  You
     * will receive a FileNotFoundException exception if the provider returns a
     * sub-section of a file.
     *
     * <h5>Accepts the following URI schemes:</h5>
     * <ul>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * </ul>
     *
     * <p>See {@link #openAssetFileDescriptor(Uri, String)} for more information
     * on these schemes.
     *
     * @param uri The desired URI to open.
     * @param mode The file mode to use, as per {@link ContentProvider#openFile
     * ContentProvider.openFile}.
     * @return Returns a new ParcelFileDescriptor pointing to the file.  You
     * own this descriptor and are responsible for closing it when done.
     * @throws FileNotFoundException Throws FileNotFoundException of no
     * file exists under the URI or the mode is invalid.
     * @see #openAssetFileDescriptor(Uri, String)
     */
    @Implementation
    public final ParcelFileDescriptor openFileDescriptor(Uri uri,
                                                         String mode) throws FileNotFoundException {
        AssetFileDescriptor afd = openAssetFileDescriptor(uri, mode);
        if (afd == null) {
            return null;
        }

        if (afd.getDeclaredLength() < 0) {
            // This is a full file!
            return afd.getParcelFileDescriptor();
        }

        // Client can't handle a sub-section of a file, so close what
        // we got and bail with an exception.
        try {
            afd.close();
        } catch (IOException e) {
        }

        throw new FileNotFoundException("Not a whole file");
    }

    /**
     * Open a raw file descriptor to access data under a URI.  This
     * interacts with the underlying {@link ContentProvider#openAssetFile}
     * method of the provider associated with the given URI, to retrieve any file stored there.
     *
     * <h5>Accepts the following URI schemes:</h5>
     * <ul>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * </ul>
     * <h5>The android.resource ({@link #SCHEME_ANDROID_RESOURCE}) Scheme</h5>
     * <p>
     * A Uri object can be used to reference a resource in an APK file.  The
     * Uri should be one of the following formats:
     * <ul>
     * <li><code>android.resource://package_name/id_number</code><br/>
     * <code>package_name</code> is your package name as listed in your AndroidManifest.xml.
     * For example <code>com.example.myapp</code><br/>
     * <code>id_number</code> is the int form of the ID.<br/>
     * The easiest way to construct this form is
     * <pre>Uri uri = Uri.parse("android.resource://com.example.myapp/" + R.raw.my_resource");</pre>
     * </li>
     * <li><code>android.resource://package_name/type/name</code><br/>
     * <code>package_name</code> is your package name as listed in your AndroidManifest.xml.
     * For example <code>com.example.myapp</code><br/>
     * <code>type</code> is the string form of the resource type.  For example, <code>raw</code>
     * or <code>drawable</code>.
     * <code>name</code> is the string form of the resource name.  That is, whatever the file
     * name was in your res directory, without the type extension.
     * The easiest way to construct this form is
     * <pre>Uri uri = Uri.parse("android.resource://com.example.myapp/raw/my_resource");</pre>
     * </li>
     * </ul>
     *
     * @param uri The desired URI to open.
     * @param mode The file mode to use, as per {@link ContentProvider#openAssetFile
     * ContentProvider.openAssetFile}.
     * @return Returns a new ParcelFileDescriptor pointing to the file.  You
     * own this descriptor and are responsible for closing it when done.
     * @throws FileNotFoundException Throws FileNotFoundException of no
     * file exists under the URI or the mode is invalid.
     */
    @Implementation
    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri,
                                                             String mode) throws FileNotFoundException {
        String scheme = uri.getScheme();
        if (SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            if (!"r".equals(mode)) {
                throw new FileNotFoundException("Can't write resources: " + uri);
            }
            OpenResourceIdResult r = getResourceId(uri);
            try {
                return r.r.openRawResourceFd(r.id);
            } catch (Resources.NotFoundException ex) {
                throw new FileNotFoundException("Resource does not exist: " + uri);
            }
        } else if (SCHEME_FILE.equals(scheme)) {
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(
                    new File(uri.getPath()), modeToMode(uri, mode));
            return new AssetFileDescriptor(pfd, 0, -1);
        } else {
            return acquireProvider(uri).openAssetFile(uri, mode);
        }
    }

    /**
     * A resource identified by the {@link Resources} that contains it, and a resource id.
     *
     * @hide
     */
    public class OpenResourceIdResult {
        public Resources r;
        public int id;
    }

    /**
     * Resolves an android.resource URI to a {@link Resources} and a resource id.
     *
     * @hide
     */
    public OpenResourceIdResult getResourceId(Uri uri) throws FileNotFoundException {
        String authority = uri.getAuthority();
        Resources r;
        if (TextUtils.isEmpty(authority)) {
            throw new FileNotFoundException("No authority: " + uri);
        } else {
            try {
                r = mContext.getPackageManager().getResourcesForApplication(authority);
            } catch (PackageManager.NameNotFoundException ex) {
                throw new FileNotFoundException("No package found for authority: " + uri);
            }
        }
        List<String> path = uri.getPathSegments();
        if (path == null) {
            throw new FileNotFoundException("No path: " + uri);
        }
        int len = path.size();
        int id;
        if (len == 1) {
            try {
                id = Integer.parseInt(path.get(0));
            } catch (NumberFormatException e) {
                throw new FileNotFoundException("Single path segment is not a resource ID: " + uri);
            }
        } else if (len == 2) {
            id = r.getIdentifier(path.get(1), path.get(0), authority);
        } else {
            throw new FileNotFoundException("More than two path segments: " + uri);
        }
        if (id == 0) {
            throw new FileNotFoundException("No resource found for: " + uri);
        }
        OpenResourceIdResult res = new OpenResourceIdResult();
        res.r = r;
        res.id = id;
        return res;
    }

    /** @hide */
    static public int modeToMode(Uri uri, String mode) throws FileNotFoundException {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new FileNotFoundException("Bad mode for " + uri + ": "
                    + mode);
        }
        return modeBits;
    }

    /**
     * Inserts a row into a table at the given URL.
     *
     * If the content provider supports transactions the insertion will be atomic.
     *
     * @param url The URL of the table to insert into.
     * @param values The initial values for the newly inserted row. The key is the column name for
     *               the field. Passing an empty ContentValues will create an empty row.
     * @return the URL of the newly created row.
     */
    @Implementation
    public final Uri insert(Uri url, ContentValues values) {
        return acquireProvider(url).insert(url, values);
    }

    /**
     * Applies each of the {@link ContentProviderOperation} objects and returns an array
     * of their results. Passes through OperationApplicationException, which may be thrown
     * by the call to {@link ContentProviderOperation#apply}.
     * If all the applications succeed then a {@link ContentProviderResult} array with the
     * same number of elements as the operations will be returned. It is implementation-specific
     * how many, if any, operations will have been successfully applied if a call to
     * apply results in a {@link OperationApplicationException}.
     * @param authority the authority of the ContentProvider to which this batch should be applied
     * @param operations the operations to apply
     * @return the results of the applications
     * @throws OperationApplicationException thrown if an application fails.
     * See {@link ContentProviderOperation#apply} for more information.
     * @throws RemoteException thrown if a RemoteException is encountered while attempting
     *   to communicate with a remote provider.
     */
    @Implementation
    public ContentProviderResult[] applyBatch(String authority,
                                              ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
        return acquireProvider(authority).applyBatch(operations);
    }

    /**
     * Inserts multiple rows into a table at the given URL.
     *
     * This function make no guarantees about the atomicity of the insertions.
     *
     * @param url The URL of the table to insert into.
     * @param values The initial values for the newly inserted rows. The key is the column name for
     *               the field. Passing null will create an empty row.
     * @return the number of newly created rows.
     */
    @Implementation
    public final int bulkInsert(Uri url, ContentValues[] values) {
        return acquireProvider(url).bulkInsert(url, values);
    }

    /**
     * Deletes row(s) specified by a content URI.
     *
     * If the content provider supports transactions, the deletion will be atomic.
     *
     * @param url The URL of the row to delete.
     * @param where A filter to apply to rows before deleting, formatted as an SQL WHERE clause
    (excluding the WHERE itself).
     * @return The number of rows deleted.
     */
    @Implementation
    public final int delete(Uri url, String where, String[] selectionArgs) {
        return acquireProvider(url).delete(url, where, selectionArgs);
    }

    /**
     * Update row(s) in a content URI.
     *
     * If the content provider supports transactions the update will be atomic.
     *
     * @param uri The URI to modify.
     * @param values The new field values. The key is the column name for the field.
    A null value will remove an existing field value.
     * @param where A filter to apply to rows before updating, formatted as an SQL WHERE clause
    (excluding the WHERE itself).
     * @return the number of rows updated.
     * @throws NullPointerException if uri or values are null
     */
    @Implementation
    public final int update(Uri uri, ContentValues values, String where,
                            String[] selectionArgs) {
        return acquireProvider(uri).update(uri, values, where, selectionArgs);
    }

    /**
     * Register an observer class that gets callbacks when data identified by a
     * given content URI changes.
     *
     * @param uri The URI to watch for changes. This can be a specific row URI, or a base URI
     * for a whole class of content.
     * @param notifyForDescendents If <code>true</code> changes to URIs beginning with <code>uri</code>
     * will also cause notifications to be sent. If <code>false</code> only changes to the exact URI
     * specified by <em>uri</em> will cause notifications to be sent. If true, than any URI values
     * at or below the specified URI will also trigger a match.
     * @param observer The object that receives callbacks when changes occur.
     * @see #unregisterContentObserver
     */
    @Implementation
    public final synchronized void registerContentObserver(
            Uri uri,
            boolean notifyForDescendents,
            ContentObserver observer) {
        ContentObserverRegistration reg = new ContentObserverRegistration(
                uri,
                notifyForDescendents,
                observer
        );
        contentObserverRegistrationList.add(reg);
    }

    /**
     * Unregisters a change observer.
     *
     * @param observer The previously registered observer that is no longer needed.
     * @see #registerContentObserver
     */
    @Implementation
    public final synchronized void unregisterContentObserver(ContentObserver observer) {
        Iterator<ContentObserverRegistration> iter = contentObserverRegistrationList.iterator();
        while (iter.hasNext()) {
            ContentObserverRegistration reg = iter.next();
            if (reg.observer == observer) {
                iter.remove();
            }
        }
    }

    /**
     * Notify registered observers that a row was updated.
     * To register, call {@link #registerContentObserver(android.net.Uri , boolean, android.database.ContentObserver) registerContentObserver()}.
     * By default, CursorAdapter objects will get this notification.
     *
     * @param uri
     * @param observer The observer that originated the change, may be <code>null</null>
     */
    @Implementation
    public void notifyChange(Uri uri, ContentObserver observer) {
        notifyChange(uri, observer, true /* sync to network */);
    }

    /**
     * Notify registered observers that a row was updated.
     * To register, call {@link #registerContentObserver(android.net.Uri , boolean, android.database.ContentObserver) registerContentObserver()}.
     * By default, CursorAdapter objects will get this notification.
     *
     * @param uri
     * @param observer The observer that originated the change, may be <code>null</null>
     * @param syncToNetwork If true, attempt to sync the change to the network.
     */
    @Implementation
    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
        Set<ContentObserver> matches = new HashSet<ContentObserver>();
        synchronized (this) {
            for (ContentObserverRegistration reg : contentObserverRegistrationList) {
                if (reg.uri != null &&
                    uri.getScheme().equals(reg.uri.getScheme()) &&
                    uri.getAuthority().equals(reg.uri.getAuthority())) {
                    if (reg.notifyForDescendents && uri.getPath().startsWith(reg.uri.getPath())) {
                        matches.add(reg.observer);
                    } else if (uri.getPath().equals(reg.uri.getPath())) {
                        matches.add(reg.observer);
                    }
                }
            }
        }
        for (ContentObserver match : matches) {
            if (match.deliverSelfNotifications() || match != observer) {
                match.dispatchChange(match == observer);
            }
        }
    }
}
