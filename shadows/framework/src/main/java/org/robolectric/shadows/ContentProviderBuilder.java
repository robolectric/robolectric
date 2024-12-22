package org.robolectric.shadows;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.io.ByteStreams;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A quick low-boilerplate way to build {@link android.content.ContentProvider} objects used for
 * testing purposes. It allows setting specific paths to return either {@link Cursor) or
 * {@link ParcelFileDescriptor} objects.
 */
public class ContentProviderBuilder {

  private Map<Pattern, Cursor> queryPaths = new HashMap<>();
  private Map<Pattern, ParcelFileDescriptor> filePaths = new HashMap<>();

  private ContentProviderBuilder() {}

  /** Factory method for {@link ContentProviderBuilder} . */
  public static ContentProviderBuilder newBuilder() {
    return new ContentProviderBuilder();
  }

  /**
   * Add a {@link Cursor} for the path represented by {@param pathPattern}.
   *
   * @param pathPattern a String regex representing the path that is queried
   * @param cursor the cursor data to be returned
   */
  public ContentProviderBuilder addCursor(String pathPattern, Cursor cursor) {
    queryPaths.put(Pattern.compile(pathPattern), cursor);
    return this;
  }

  /**
   * Add a {@link ParcelFileDescriptor} for the path represented by {@param pathPattern}.
   *
   * @param pathPattern a String regex representing the path that is queried
   * @param parcelFileDescriptor the ParcelFileDescriptor to be returned
   */
  public ContentProviderBuilder addFile(
      String pathPattern, ParcelFileDescriptor parcelFileDescriptor) {
    filePaths.put(Pattern.compile(pathPattern), parcelFileDescriptor);
    return this;
  }

  /**
   * Helper to add a {@link ParcelFileDescriptor} derived from the {@link InputStream} for the path
   * represented by {@param pathPattern}.
   *
   * @param pathPattern a String regex representing the path that is queried
   * @param inputStream the input stream for which to build a ParcelFileDescriptor
   */
  public ContentProviderBuilder addFile(String pathPattern, InputStream inputStream) {
    ParcelFileDescriptor pfd;
    try {
      ParcelFileDescriptor[] pfds = ParcelFileDescriptor.createPipe();
      FileOutputStream outputStream = new FileOutputStream(pfds[1].getFileDescriptor());
      ByteStreams.copy(inputStream, outputStream);
      pfd = pfds[0];
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return addFile(pathPattern, pfd);
  }

  /** Builds and returns the {@link ContentProvider} object. */
  public ContentProvider build() {
    return new NoOpContentProvider() {
      @Nullable
      @Override
      public Cursor query(
          @NonNull Uri uri,
          @Nullable String[] projection,
          @Nullable String selection,
          @Nullable String[] selectionArgs,
          @Nullable String sortOrder) {
        for (Map.Entry<Pattern, Cursor> entry : queryPaths.entrySet()) {
          if (entry.getKey().matcher(uri.getPath()).matches()
              || entry.getKey().matcher(uri.getPath() + "?" + uri.getQuery()).matches()) {
            return entry.getValue();
          }
        }
        return null;
      }

      @Nullable
      @Override
      public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) {
        for (Map.Entry<Pattern, ParcelFileDescriptor> entry : filePaths.entrySet()) {
          if (entry.getKey().matcher(uri.getPath()).matches()
              || entry.getKey().matcher(uri.getPath() + "?" + uri.getQuery()).matches()) {
            return entry.getValue();
          }
        }
        return null;
      }
    };
  }

  static class NoOpContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
      return true;
    }

    @Nullable
    @Override
    public Cursor query(
        @NonNull Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder) {
      return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
      return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
      return null;
    }

    @Override
    public int delete(
        @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
      return 0;
    }

    @Override
    public int update(
        @NonNull Uri uri,
        @Nullable ContentValues values,
        @Nullable String selection,
        @Nullable String[] selectionArgs) {
      return 0;
    }
  }
}
