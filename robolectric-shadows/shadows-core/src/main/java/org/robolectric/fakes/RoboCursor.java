package org.robolectric.fakes;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class RoboCursor extends BaseCursor {
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
    ++resultsIndex;
    return resultsIndex < results.length;
  }

  @Override
  public void close() {
    closeWasCalled = true;
  }

  @Override
  public int getCount() {
    return results.length;
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
