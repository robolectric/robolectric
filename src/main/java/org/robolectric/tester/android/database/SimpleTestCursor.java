package org.robolectric.tester.android.database;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class SimpleTestCursor extends TestCursor {
  public Uri uri;
  public String[] projection;
  public String selection;
  public String[] selectionArgs;
  public String sortOrder;
  protected Object[][] results = new Object[0][0];
  protected List<String> columnNames= new ArrayList<String>();
  int resultsIndex = -1;
  boolean closeWasCalled;

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
    if(col == -1){
      throw new IllegalArgumentException("No column with name: "+columnName);
    }
    return col;
  }

  @Override
  public int getColumnIndex(String columnName) {
    return columnNames.indexOf(columnName);
  }

  @Override
  public int getColumnCount() {
    return columnNames.size();
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnNames.get(columnIndex);
  }

  @Override
  public String getString(int columnIndex) {
    return (String) results[resultsIndex][columnIndex];
  }

  @Override
  public int getInt(int columnIndex) {
    return (Integer) results[resultsIndex][columnIndex];
  }

  @Override
  public long getLong(int columnIndex) {
    return (Long) results[resultsIndex][columnIndex];
  }

  @Override
  public short getShort(int columnIndex) {
    return (Short) results[resultsIndex][columnIndex];
  }

  @Override
  public float getFloat(int columnIndex) {
    return (Float) results[resultsIndex][columnIndex];
  }

  @Override
  public double getDouble(int columnIndex) {
    return (Double) results[resultsIndex][columnIndex];
  }

  @Override
  public byte[] getBlob(int columnIndex) {
    return (byte[]) results[resultsIndex][columnIndex];
  }

  @Override
  public int getCount() {
    return results.length;
  }

  @Override
  public int getPosition() {
    return resultsIndex;
  }

  @Override
  public String[] getColumnNames() {
    return columnNames.toArray(new String[columnNames.size()]);
  }

  @Override
  public boolean moveToNext() {
    ++resultsIndex;
    return resultsIndex < results.length;
  }

  @Override
  public void close() {
    closeWasCalled = true;
  }

  /**
   * Use {@link #setResults(java.util.List, Object[][])} to set the column names and the data
   * in an atomic operation.
   * @param columnNames list of column names
   */
  @Deprecated
  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  /**
   * Use {@link #setResults(java.util.List, Object[][])} to set the column names and the data
   * in an atomic operation.
   * @param results matrix of results
   */
  @Deprecated
  public void setResults(Object[][] results) {
    this.results = results;
  }

  /**
   * Sets the results and column names in an atomic operation, verifies columns count
   * aligns with the data.
   * @param columnNames list of column names
   * @param results matrix of results
   */
  public void setResults(List<String> columnNames, Object[][] results) {
    for (Object[] result : results) {
      if (columnNames.size() != result.length) {
        throw new IllegalArgumentException("Each row must have the correct number of columns");
      }
    }
    this.columnNames = columnNames;
    this.results = results;
  }

  public boolean getCloseWasCalled() {
    return closeWasCalled;
  }
}
