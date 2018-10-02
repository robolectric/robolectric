package org.robolectric.fakes;

import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

/**
 * Robolectric implementation of {@link android.database.Cursor}.
 */
public class RoboCursor extends BaseCursor {
  public Uri uri;
  public String[] projection;
  public String selection;
  public String[] selectionArgs;
  public String sortOrder;
  protected Object[][] results = new Object[0][0];
  protected List<String> columnNames = new ArrayList<>();
  private int resultsIndex = -1;
  private boolean closeWasCalled;
  private Bundle extras;

  @Override
  public void setQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    this.uri = uri;
    this.projection = projection;
    this.selection = selection;
    this.selectionArgs = selectionArgs;
    this.sortOrder = sortOrder;
  }

  @Override
  public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
    int col = getColumnIndex(columnName);
    if (col == -1) {
      throw new IllegalArgumentException("No column with name: " + columnName);
    }
    return col;
  }

  @Override
  public int getColumnIndex(String columnName) {
    return columnNames.indexOf(columnName);
  }

  @Override
  public String getString(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null ? null : value.toString();
  }

  @Override
  public short getShort(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null ? 0 : (value instanceof Number ? ((Number) value).shortValue() : Short.parseShort(value.toString()));
  }

  @Override
  public int getInt(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null ? 0 : (value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString()));
  }

  @Override
  public long getLong(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null
        ? 0
        : (value instanceof Number
            ? ((Number) value).longValue()
            : Long.parseLong(value.toString()));
  }

  @Override
  public float getFloat(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null ? 0 : (value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString()));
  }

  @Override
  public double getDouble(int columnIndex) {
    Object value = results[resultsIndex][columnIndex];
    return value == null ? 0 : (value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString()));
  }

  @Override
  public byte[] getBlob(int columnIndex) {
    return (byte[]) results[resultsIndex][columnIndex];
  }

  @Override
  public int getType(int columnIndex) {
    return DatabaseUtils.getTypeOfObject(results[0][columnIndex]);
  }

  @Override
  public boolean isNull(int columnIndex) {
    return results[resultsIndex][columnIndex] == null;
  }

  @Override
  public int getCount() {
    return results.length;
  }

  @Override
  public boolean moveToNext() {
    return doMoveToPosition(resultsIndex + 1);
  }

  @Override
  public boolean moveToFirst() {
    return doMoveToPosition(0);
  }

  @Override
  public boolean moveToPosition(int position) {
    return doMoveToPosition(position);
  }

  private boolean doMoveToPosition(int position) {
    resultsIndex = position;
    return resultsIndex >= 0 && resultsIndex < results.length;
  }

  @Override
  public void close() {
    closeWasCalled = true;
  }

  @Override
  public int getColumnCount() {
    if (columnNames.isEmpty()) {
      return results[0].length;
    } else {
      return columnNames.size();
    }
  }

  @Override
  public String getColumnName(int index) {
    return columnNames.get(index);
  }

  @Override
  public boolean isBeforeFirst() {
    return resultsIndex < 0;
  }

  @Override
  public boolean isAfterLast() {
    return resultsIndex > results.length - 1;
  }

  @Override
  public boolean isFirst() {
    return resultsIndex == 0;
  }

  @Override
  public boolean isLast() {
    return resultsIndex == results.length - 1;
  }

  @Override public int getPosition() {
    return resultsIndex;
  }

  @Override public boolean move(int offset) {
    return doMoveToPosition(resultsIndex + offset);
  }

  @Override public boolean moveToLast() {
    return doMoveToPosition(results.length - 1);
  }

  @Override public boolean moveToPrevious() {
    return doMoveToPosition(resultsIndex - 1);
  }

  @Override public String[] getColumnNames() {
    return columnNames.toArray(new String[columnNames.size()]);
  }

  @Override public boolean isClosed() {
    return closeWasCalled;
  }

  @Override public Bundle getExtras() {
    return extras;
  }

  @Override
  public void setExtras(Bundle extras) {
    this.extras = extras;
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  public void setResults(Object[][] results) {
    this.results = results;
  }

  public boolean getCloseWasCalled() {
    return closeWasCalled;
  }
}
