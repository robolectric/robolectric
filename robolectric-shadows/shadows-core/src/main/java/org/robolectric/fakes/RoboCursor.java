package org.robolectric.fakes;

import android.database.DatabaseUtils;
import android.net.Uri;

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
  protected List<String> columnNames= new ArrayList<>();
  private int resultsIndex = -1;
  private boolean closeWasCalled;

  @Override
  public void setQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    this.uri = uri;
    this.projection = projection;
    this.selection = selection;
    this.selectionArgs = selectionArgs;
    this.sortOrder = sortOrder;
  }

  @Override
  public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException{
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
    return (String) results[resultsIndex][columnIndex];
  }

  @Override
  public long getLong(int columnIndex) {
    return (Long) results[resultsIndex][columnIndex];
  }

  @Override
  public int getInt(int columnIndex) {
    return (Integer) results[resultsIndex][columnIndex];
  }

  @Override
  public int getCount() {
    return results.length;
  }

  @Override
  public boolean moveToNext() {
    return moveToPosition(resultsIndex + 1);
  }

  @Override
  public boolean moveToFirst() {
    return moveToPosition(0);
  }

  @Override
  public boolean moveToPosition(int position) {
    resultsIndex = position;
    return resultsIndex >= 0 && resultsIndex < results.length;
  }

  @Override
  public void close() {
    closeWasCalled = true;
  }

  @Override
  public int getColumnCount() {
    return results[0].length;
  }

  @Override
  public String getColumnName(int index) {
    return columnNames.get(index);
  }

  @Override
  public int getType(int columnIndex) {
    return DatabaseUtils.getTypeOfObject(results[0][columnIndex]);
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
