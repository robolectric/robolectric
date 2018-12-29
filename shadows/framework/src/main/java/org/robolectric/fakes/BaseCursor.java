package org.robolectric.fakes;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

/**
 * Robolectric implementation of {@link android.database.Cursor}.
 */
public class BaseCursor implements Cursor {
  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public int getPosition() {
    return -1;
  }

  @Override
  public boolean move(int offset) {
    return false;
  }

  @Override
  public boolean moveToPosition(int position) {
    return false;
  }

  @Override
  public boolean moveToFirst() {
    return false;
  }

  @Override
  public boolean moveToLast() {
    return false;
  }

  @Override
  public boolean moveToNext() {
    return false;
  }

  @Override
  public boolean moveToPrevious() {
    return false;
  }

  @Override
  public boolean isFirst() {
    return false;
  }

  @Override
  public boolean isLast() {
    return false;
  }

  @Override
  public boolean isBeforeFirst() {
    return true;
  }

  @Override
  public boolean isAfterLast() {
    return false;
  }

  @Override
  public int getColumnIndex(String columnName) {
    return -1;
  }

  @Override
  public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
    throw new IllegalArgumentException();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return null;
  }

  @Override
  public String[] getColumnNames() {
    return new String[0];
  }

  @Override
  public int getColumnCount() {
    return 0;
  }

  @Override
  public byte[] getBlob(int columnIndex) {
    return null;
  }

  @Override
  public String getString(int columnIndex) {
    return null;
  }

  @Override
  public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
  }

  @Override
  public short getShort(int columnIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getInt(int columnIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLong(int columnIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public float getFloat(int columnIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public double getDouble(int columnIndex) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isNull(int columnIndex) {
    return true;
  }

  @Override
  public void deactivate() {
  }

  @Override
  public boolean requery() {
    return true;
  }

  @Override
  public void close() {
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public void registerContentObserver(ContentObserver observer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unregisterContentObserver(ContentObserver observer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNotificationUri(ContentResolver cr, Uri uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Uri getNotificationUri() {
    return null;
  }

  @Override
  public boolean getWantsAllOnMoveCalls() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setExtras(Bundle extras) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Bundle getExtras() {
    return Bundle.EMPTY;
  }

  @Override
  public Bundle respond(Bundle extras) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getType(int columnIndex) {
    return FIELD_TYPE_NULL;
  }

  /*
   * Mimics ContentResolver.query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
   */
  public void setQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    // Override this in your subclass if you care to implement any of the other methods based on the query that was performed.
  }
}
