package com.xtremelabs.robolectric.tester.android.database;

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
    public boolean moveToNext() {
        ++resultsIndex;
        return resultsIndex < results.length;
    }

    @Override
    public void close() {
        closeWasCalled = true;
    }

    public void setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
    }

    public void setResults(Object[][] results) {
        this.results = results;
    }

    public boolean getCloseWasCalled() {
        return closeWasCalled;
    }
}
