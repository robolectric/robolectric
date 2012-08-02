package com.xtremelabs.robolectric.tester.android.database;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class TestCursor implements Cursor {
    @Override
    public int getCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean move(int offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveToPosition(int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveToFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveToLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveToNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean moveToPrevious() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBeforeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAfterLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnIndex(String columnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getColumnName(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getColumnNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getBlob(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(int columnIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isClosed() {
        throw new UnsupportedOperationException();
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
    public boolean getWantsAllOnMoveCalls() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getExtras() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle respond(Bundle extras) {
        throw new UnsupportedOperationException();
    }

	@Override
	public int getType(int columnIndex) {
        throw new UnsupportedOperationException();
	}

	/**
     * Mimics ContentResolver.query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
     **/
    public void setQuery(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Override this in your subclass if you care to implement any of the other methods
        // based on the query that was performed.
    }
}
