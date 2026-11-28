package org.robolectric.shadows;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.android.controller.ContentProviderController;

/**
 * Builds a {@link ContentProvider} for tests that returns a {@link Cursor} or file contents for the
 * paths it is given, and records the queries made to it.
 *
 * <p>Install it under an authority with {@link ContentProviderController}:
 *
 * <pre>{@code
 * MatrixCursor cursor = new MatrixCursor(new String[] {"_id", "name"});
 * cursor.addRow(new Object[] {1, "Alice"});
 * FakeContentProvider provider =
 *     ContentProviderBuilder.newBuilder().addCursor("/contacts", cursor).build();
 * ContentProviderController.of(provider).create("com.example.contacts");
 * }</pre>
 *
 * <p>A path pattern is a regular expression that must match the whole path of the URI, or the path
 * followed by {@code ?} and the query of the URI. When several patterns match, the one added first
 * wins. Inserts, updates and deletes are ignored, and {@link ContentProvider#getType} returns null.
 */
public final class ContentProviderBuilder {

  private final Map<Pattern, Supplier<Cursor>> cursors = new LinkedHashMap<>();
  private final Map<Pattern, byte[]> files = new LinkedHashMap<>();

  private ContentProviderBuilder() {}

  /** Returns a new {@link ContentProviderBuilder}. */
  public static ContentProviderBuilder newBuilder() {
    return new ContentProviderBuilder();
  }

  /**
   * Returns {@code cursor} from queries whose path matches {@code pathPattern}. Every such query
   * returns this same cursor, moved back before its first row, so the code under test must not
   * close it if it queries the path again. Use {@link #addCursor(String, Supplier)} to return a new
   * cursor from each query.
   *
   * @param pathPattern a regular expression for the path of the URI that is queried
   * @param cursor the cursor that queries return
   */
  public ContentProviderBuilder addCursor(String pathPattern, Cursor cursor) {
    return addCursor(
        pathPattern,
        () -> {
          cursor.moveToPosition(-1);
          return cursor;
        });
  }

  /**
   * Returns a cursor from {@code cursorSupplier} from each query whose path matches {@code
   * pathPattern}.
   *
   * @param pathPattern a regular expression for the path of the URI that is queried
   * @param cursorSupplier supplies the cursor that each query returns
   */
  public ContentProviderBuilder addCursor(String pathPattern, Supplier<Cursor> cursorSupplier) {
    cursors.put(Pattern.compile(pathPattern), cursorSupplier);
    return this;
  }

  /**
   * Returns {@code contents} from each read of a file whose path matches {@code pathPattern}, for
   * example with {@link android.content.ContentResolver#openInputStream}.
   *
   * @param pathPattern a regular expression for the path of the URI that is opened
   * @param contents the contents of the file
   */
  public ContentProviderBuilder addFile(String pathPattern, byte[] contents) {
    files.put(Pattern.compile(pathPattern), contents.clone());
    return this;
  }

  /**
   * Like {@link #addFile(String, byte[])}, with the contents of {@code inputStream}, which is read
   * and closed now.
   *
   * @param pathPattern a regular expression for the path of the URI that is opened
   * @param inputStream the contents of the file
   */
  public ContentProviderBuilder addFile(String pathPattern, InputStream inputStream) {
    try (InputStream in = inputStream) {
      return addFile(pathPattern, ByteStreams.toByteArray(in));
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read the contents for " + pathPattern, e);
    }
  }

  /**
   * Returns the provider. Paths added to this builder afterwards do not change it. Install it with
   * {@link ContentProviderController}.
   */
  public FakeContentProvider build() {
    return new FakeContentProvider(new LinkedHashMap<>(cursors), new LinkedHashMap<>(files));
  }

  /** A query made to a {@link FakeContentProvider}. */
  public static final class Query {
    private final Uri uri;
    @Nullable private final String[] projection;
    @Nullable private final String selection;
    @Nullable private final String[] selectionArgs;
    @Nullable private final String sortOrder;
    @Nullable private final Bundle queryArgs;

    private Query(
        Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder,
        @Nullable Bundle queryArgs) {
      this.uri = uri;
      this.projection = projection == null ? null : projection.clone();
      this.selection = selection;
      this.selectionArgs = selectionArgs == null ? null : selectionArgs.clone();
      this.sortOrder = sortOrder;
      this.queryArgs = queryArgs == null ? null : new Bundle(queryArgs);
    }

    public Uri getUri() {
      return uri;
    }

    @Nullable
    public String[] getProjection() {
      return projection == null ? null : projection.clone();
    }

    /**
     * Returns the selection. For a query made with a {@link Bundle} of query arguments, this is the
     * {@link android.content.ContentResolver#QUERY_ARG_SQL_SELECTION} argument.
     */
    @Nullable
    public String getSelection() {
      return selection;
    }

    @Nullable
    public String[] getSelectionArgs() {
      return selectionArgs == null ? null : selectionArgs.clone();
    }

    @Nullable
    public String getSortOrder() {
      return sortOrder;
    }

    /**
     * Returns the {@link Bundle} of query arguments, or null for a query made without one, as
     * before {@link android.os.Build.VERSION_CODES#O}.
     */
    @Nullable
    public Bundle getQueryArgs() {
      return queryArgs == null ? null : new Bundle(queryArgs);
    }

    @Override
    public String toString() {
      return "Query{uri="
          + uri
          + ", projection="
          + Arrays.toString(projection)
          + ", selection="
          + selection
          + ", selectionArgs="
          + Arrays.toString(selectionArgs)
          + ", sortOrder="
          + sortOrder
          + ", queryArgs="
          + queryArgs
          + "}";
    }
  }

  /** The {@link ContentProvider} built by {@link ContentProviderBuilder}. */
  public static final class FakeContentProvider extends ContentProvider {
    private final Map<Pattern, Supplier<Cursor>> cursors;
    private final Map<Pattern, byte[]> files;
    private final List<Query> queries = new ArrayList<>();

    /** The query arguments of the {@link Bundle} query in progress, if any. */
    @Nullable private Bundle pendingQueryArgs;

    private FakeContentProvider(
        Map<Pattern, Supplier<Cursor>> cursors, Map<Pattern, byte[]> files) {
      this.cursors = cursors;
      this.files = files;
    }

    /** Returns the queries made to this provider, oldest first. */
    public synchronized ImmutableList<Query> getQueries() {
      return ImmutableList.copyOf(queries);
    }

    /** Returns the most recent query made to this provider, or null if there were none. */
    @Nullable
    public synchronized Query getLastQuery() {
      return queries.isEmpty() ? null : queries.get(queries.size() - 1);
    }

    @Override
    public boolean onCreate() {
      return true;
    }

    @Nullable
    @Override
    public Cursor query(
        @Nonnull Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder) {
      return query(
          uri, projection, selection, selectionArgs, sortOrder, /* cancellationSignal= */ null);
    }

    @Nullable
    @Override
    public synchronized Cursor query(
        @Nonnull Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder,
        @Nullable CancellationSignal cancellationSignal) {
      Bundle queryArgs = pendingQueryArgs;
      pendingQueryArgs = null;
      queries.add(new Query(uri, projection, selection, selectionArgs, sortOrder, queryArgs));
      Supplier<Cursor> cursorSupplier = find(cursors, uri);
      return cursorSupplier == null ? null : cursorSupplier.get();
    }

    /**
     * Records the query arguments, then lets the framework turn them into a selection and sort
     * order, which it passes to {@link #query(Uri, String[], String, String[], String,
     * CancellationSignal)}.
     */
    @Nullable
    @Override
    public synchronized Cursor query(
        @Nonnull Uri uri,
        @Nullable String[] projection,
        @Nullable Bundle queryArgs,
        @Nullable CancellationSignal cancellationSignal) {
      pendingQueryArgs = queryArgs == null ? Bundle.EMPTY : queryArgs;
      try {
        return super.query(uri, projection, queryArgs, cancellationSignal);
      } finally {
        pendingQueryArgs = null;
      }
    }

    @Override
    public ParcelFileDescriptor openFile(@Nonnull Uri uri, @Nonnull String mode)
        throws FileNotFoundException {
      byte[] contents = find(files, uri);
      if (contents == null) {
        throw new FileNotFoundException("No file for " + uri);
      }
      if (!mode.equals("r")) {
        throw new FileNotFoundException("Only reading is supported, not mode " + mode);
      }
      try {
        File file = File.createTempFile("content-provider-builder", null);
        file.deleteOnExit();
        try (OutputStream out = new FileOutputStream(file)) {
          out.write(contents);
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
      } catch (IOException e) {
        FileNotFoundException notFound = new FileNotFoundException("Could not open " + uri);
        notFound.initCause(e);
        throw notFound;
      }
    }

    @Nullable
    @Override
    public String getType(@Nonnull Uri uri) {
      return null;
    }

    @Nullable
    @Override
    public Uri insert(@Nonnull Uri uri, @Nullable ContentValues values) {
      return null;
    }

    @Override
    public int delete(
        @Nonnull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(
        @Nonnull Uri uri,
        @Nullable ContentValues values,
        @Nullable String selection,
        @Nullable String[] selectionArgs) {
      return 0;
    }

    @Nullable
    private static <T> T find(Map<Pattern, T> entries, Uri uri) {
      String path = uri.getPath();
      if (path == null) {
        return null;
      }
      String query = uri.getQuery();
      for (Map.Entry<Pattern, T> entry : entries.entrySet()) {
        Pattern pattern = entry.getKey();
        if (pattern.matcher(path).matches()
            || (query != null && pattern.matcher(path + "?" + query).matches())) {
          return entry.getValue();
        }
      }
      return null;
    }
  }
}
