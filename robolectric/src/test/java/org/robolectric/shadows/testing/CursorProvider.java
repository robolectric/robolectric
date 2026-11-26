package org.robolectric.shadows.testing;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A minimal {@link ContentProvider} for tests that returns a configured {@link Cursor} from {@code
 * query} and records the arguments of the most recent query.
 */
public class CursorProvider extends ContentProvider {

  private final Cursor cursor;

  public Uri lastUri;
  public String[] lastProjection;
  public String lastSelection;
  public String[] lastSelectionArgs;
  public String lastSortOrder;
  public Bundle lastQueryArgs;
  public CancellationSignal lastCancellationSignal;

  public CursorProvider(Cursor cursor) {
    this.cursor = cursor;
  }

  @Override
  public boolean onCreate() {
    return true;
  }

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

  @Override
  public Cursor query(
      @Nonnull Uri uri,
      @Nullable String[] projection,
      @Nullable String selection,
      @Nullable String[] selectionArgs,
      @Nullable String sortOrder,
      @Nullable CancellationSignal cancellationSignal) {
    lastUri = uri;
    lastProjection = projection;
    lastSelection = selection;
    lastSelectionArgs = selectionArgs;
    lastSortOrder = sortOrder;
    lastQueryArgs = null;
    lastCancellationSignal = cancellationSignal;
    return cursor;
  }

  @Override
  public Cursor query(
      @Nonnull Uri uri,
      @Nullable String[] projection,
      @Nullable Bundle queryArgs,
      @Nullable CancellationSignal cancellationSignal) {
    lastUri = uri;
    lastProjection = projection;
    lastSelection = null;
    lastSelectionArgs = null;
    lastSortOrder = null;
    lastQueryArgs = queryArgs;
    lastCancellationSignal = cancellationSignal;
    return cursor;
  }

  @Override
  public String getType(@Nonnull Uri uri) {
    return null;
  }

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
}
