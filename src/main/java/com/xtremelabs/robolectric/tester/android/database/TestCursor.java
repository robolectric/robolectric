package com.xtremelabs.robolectric.tester.android.database;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Cursor for testing.
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/7/11
 *         Time: 6:25 AM
 */
public class TestCursor implements Cursor {
  private int position = -1;
  private final List<Map<String,Object>> rows;
  private boolean closed = false;

  public TestCursor(List<Map<String, Object>> rows) {
    this.rows = rows;
  }

  @Override
  public int getCount() {
    return rows.size();
  }

  @Override
  public int getPosition() {
    return position;
  }

  @Override
  public boolean move(int offset) {
    return moveToPosition(position + offset);
  }

  @Override
  public boolean moveToPosition(int position) {
    this.position = Math.min(Math.max(position, -1), getCount());
    return (position < getCount() && position >= 0);
  }

  @Override
  public boolean moveToFirst() {
    return moveToPosition(0);
  }

  @Override
  public boolean moveToLast() {
    return moveToPosition(getCount() - 1);
  }

  @Override
  public boolean moveToNext() {
    return moveToPosition(position + 1);
  }

  @Override
  public boolean moveToPrevious() {
    return moveToPosition(position - 1);
  }

  @Override
  public boolean isFirst() {
    return position == 0;
  }

  @Override
  public boolean isLast() {
    return position == getCount() - 1;
  }

  @Override
  public boolean isBeforeFirst() {
    return position < 0;
  }

  @Override
  public boolean isAfterLast() {
    return position >= getCount();
  }

  @Override
  public int getColumnIndex(String columnName) {
    return new ArrayList<String>(getRow().keySet()).indexOf(columnName);
  }

  @Override
  public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
    int index = new ArrayList<String>(getRow().keySet()).indexOf(columnName);
    if (index < 0) throw new IllegalArgumentException("Column not found: " + columnName);
    return index;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return new ArrayList<String>(getRow().keySet()).get(columnIndex);
  }

  private Map<String, Object> getRow() {
    return rows.get(position);
  }

  @Override
  public String[] getColumnNames() {
    ArrayList<String> columnNames = getColumnNameList();
    return columnNames.toArray(new String[columnNames.size()]);
  }

  private ArrayList<String> getColumnNameList() {
    return new ArrayList<String>(getRow().keySet());
  }

  @Override
  public int getColumnCount() {
    return getRow().size();
  }

  @Override
  public byte[] getBlob(int columnIndex) {
    return (byte[]) getValue(columnIndex);
  }

  private Object getValue(int columnIndex) {
    return getRow().get(getColumnName(columnIndex));
  }

  @Override
  public String getString(int columnIndex) {
    return (String) getValue(columnIndex);
  }

  @Override
  public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
    char[] source = getString(columnIndex).toCharArray();
    int count = Math.min(source.length, buffer.data.length);
    System.arraycopy(source, 0, buffer.data, 0, count);
    buffer.sizeCopied = count;
  }

  @Override
  public short getShort(int columnIndex) {
    return ((Number) getValue(columnIndex)).shortValue();
  }

  @Override
  public int getInt(int columnIndex) {
    return ((Number) getValue(columnIndex)).intValue();
  }

  @Override
  public long getLong(int columnIndex) {
    return ((Number) getValue(columnIndex)).longValue();
  }

  @Override
  public float getFloat(int columnIndex) {
    return ((Number) getValue(columnIndex)).floatValue();
  }

  @Override
  public double getDouble(int columnIndex) {
    return ((Number) getValue(columnIndex)).doubleValue();
  }

  @Override
  public boolean isNull(int columnIndex) {
    return getValue(columnIndex) == null;
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
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void registerContentObserver(ContentObserver observer) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void unregisterContentObserver(ContentObserver observer) {
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
  }

  @Override
  public void setNotificationUri(ContentResolver cr, Uri uri) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public boolean getWantsAllOnMoveCalls() {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Bundle getExtras() {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Bundle respond(Bundle extras) {
    throw new UnsupportedOperationException("not implemented yet");
  }
}
