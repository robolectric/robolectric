package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(CursorWrapper.class)
public class ShadowCursorWrapper implements Cursor {
  private Cursor wrappedCursor;

  @Implementation
  protected void __constructor__(Cursor c) {
    wrappedCursor = c;
  }

  @Override @Implementation
  public int getCount() {
    return wrappedCursor.getCount();
  }

  @Override @Implementation
  public int getPosition() {
    return wrappedCursor.getPosition();
  }

  @Override @Implementation
  public boolean move(int i) {
    return wrappedCursor.move(i);
  }

  @Override @Implementation
  public boolean moveToPosition(int i) {
    return wrappedCursor.moveToPosition(i);
  }

  @Override @Implementation
  public boolean moveToFirst() {
    return wrappedCursor.moveToFirst();
  }

  @Override @Implementation
  public boolean moveToLast() {
    return wrappedCursor.moveToLast();
  }

  @Override @Implementation
  public boolean moveToNext() {
    return wrappedCursor.moveToNext();
  }

  @Override @Implementation
  public boolean moveToPrevious() {
    return wrappedCursor.moveToPrevious();
  }

  @Override @Implementation
  public boolean isFirst() {
    return wrappedCursor.isFirst();
  }

  @Override @Implementation
  public boolean isLast() {
    return wrappedCursor.isLast();
  }

  @Override @Implementation
  public boolean isBeforeFirst() {
    return wrappedCursor.isBeforeFirst();
  }

  @Override @Implementation
  public boolean isAfterLast() {
    return wrappedCursor.isAfterLast();
  }

  @Override @Implementation
  public int getColumnIndex(String s) {
    return wrappedCursor.getColumnIndex(s);
  }

  @Override @Implementation
  public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
    return wrappedCursor.getColumnIndexOrThrow(s);
  }

  @Override @Implementation
  public String getColumnName(int i) {
    return wrappedCursor.getColumnName(i);
  }

  @Override @Implementation
  public String[] getColumnNames() {
    return wrappedCursor.getColumnNames();
  }

  @Override @Implementation
  public int getColumnCount() {
    return wrappedCursor.getColumnCount();
  }

  @Override @Implementation
  public byte[] getBlob(int i) {
    return wrappedCursor.getBlob(i);
  }

  @Override @Implementation
  public String getString(int i) {
    return wrappedCursor.getString(i);
  }

  @Override @Implementation
  public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
    wrappedCursor.copyStringToBuffer(i, charArrayBuffer);
  }

  @Override @Implementation
  public short getShort(int i) {
    return wrappedCursor.getShort(i);
  }

  @Override @Implementation
  public int getInt(int i) {
    return wrappedCursor.getInt(i);
  }

  @Override @Implementation
  public long getLong(int i) {
    return wrappedCursor.getLong(i);
  }

  @Override @Implementation
  public float getFloat(int i) {
    return wrappedCursor.getFloat(i);
  }

  @Override @Implementation
  public double getDouble(int i) {
    return wrappedCursor.getDouble(i);
  }

  @Override @Implementation
  public boolean isNull(int i) {
    return wrappedCursor.isNull(i);
  }

  @Override @Implementation
  public void deactivate() {
    wrappedCursor.deactivate();
  }

  @Override @Implementation
  public boolean requery() {
    return wrappedCursor.requery();
  }

  @Override @Implementation
  public void close() {
    wrappedCursor.close();
  }

  @Override @Implementation
  public boolean isClosed() {
    return wrappedCursor.isClosed();
  }

  @Override @Implementation
  public void registerContentObserver(ContentObserver contentObserver) {
    wrappedCursor.registerContentObserver(contentObserver);
  }

  @Override @Implementation
  public void unregisterContentObserver(ContentObserver contentObserver) {
    wrappedCursor.unregisterContentObserver(contentObserver);
  }

  @Override @Implementation
  public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    wrappedCursor.registerDataSetObserver(dataSetObserver);
  }

  @Override @Implementation
  public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    wrappedCursor.unregisterDataSetObserver(dataSetObserver);
  }

  @Override @Implementation
  public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
    wrappedCursor.setNotificationUri(contentResolver, uri);
  }

  @Override @Implementation(minSdk = KITKAT)
  public Uri getNotificationUri() {
    return wrappedCursor.getNotificationUri();
  }

  @Override @Implementation
  public boolean getWantsAllOnMoveCalls() {
    return wrappedCursor.getWantsAllOnMoveCalls();
  }

  @Override @Implementation(minSdk = M)
  public void setExtras(Bundle extras) {
    wrappedCursor.setExtras(extras);
  }

  @Override @Implementation
  public Bundle getExtras() {
    return wrappedCursor.getExtras();
  }

  @Override @Implementation
  public Bundle respond(Bundle bundle) {
    return wrappedCursor.respond(bundle);
  }

  @Override @Implementation
  public int getType(int columnIndex) {
    return wrappedCursor.getType(columnIndex);
  }

  @Implementation
  protected Cursor getWrappedCursor() {
    return wrappedCursor;
  }
}
