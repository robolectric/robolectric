package com.xtremelabs.robolectric.shadows;

import android.content.ContentResolver;
import android.database.*;
import android.net.Uri;
import android.os.Bundle;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(CursorWrapper.class)
public class ShadowCursorWrapper implements Cursor {
    private Cursor wrappedCursor;

    public void __constructor__(Cursor c) {
        wrappedCursor = c;
    }

    @Implementation
    public int getCount() {
        return wrappedCursor.getCount();
    }

    @Implementation
    public int getPosition() {
        return wrappedCursor.getPosition();
    }

    @Implementation
    public boolean move(int i) {
        return wrappedCursor.move(i);
    }

    @Implementation
    public boolean moveToPosition(int i) {
        return wrappedCursor.moveToPosition(i);
    }

    @Implementation
    public boolean moveToFirst() {
        return wrappedCursor.moveToFirst();
    }

    @Implementation
    public boolean moveToLast() {
        return wrappedCursor.moveToLast();
    }

    @Implementation
    public boolean moveToNext() {
        return wrappedCursor.moveToNext();
    }

    @Implementation
    public boolean moveToPrevious() {
        return wrappedCursor.moveToPrevious();
    }

    @Implementation
    public boolean isFirst() {
        return wrappedCursor.isFirst();
    }

    @Implementation
    public boolean isLast() {
        return wrappedCursor.isLast();
    }

    @Implementation
    public boolean isBeforeFirst() {
        return wrappedCursor.isBeforeFirst();
    }

    @Implementation
    public boolean isAfterLast() {
        return wrappedCursor.isAfterLast();
    }

    @Implementation
    public int getColumnIndex(String s) {
        return wrappedCursor.getColumnIndex(s);
    }

    @Implementation
    public int getColumnIndexOrThrow(String s) throws IllegalArgumentException {
        return wrappedCursor.getColumnIndexOrThrow(s);
    }

    @Implementation
    public String getColumnName(int i) {
        return wrappedCursor.getColumnName(i);
    }

    @Implementation
    public String[] getColumnNames() {
        return wrappedCursor.getColumnNames();
    }

    @Implementation
    public int getColumnCount() {
        return wrappedCursor.getColumnCount();
    }

    @Implementation
    public byte[] getBlob(int i) {
        return wrappedCursor.getBlob(i);
    }

    @Implementation
    public String getString(int i) {
        return wrappedCursor.getString(i);
    }

    @Implementation
    public void copyStringToBuffer(int i, CharArrayBuffer charArrayBuffer) {
        wrappedCursor.copyStringToBuffer(i, charArrayBuffer);
    }

    @Implementation
    public short getShort(int i) {
        return wrappedCursor.getShort(i);
    }

    @Implementation
    public int getInt(int i) {
        return wrappedCursor.getInt(i);
    }

    @Implementation
    public long getLong(int i) {
        return wrappedCursor.getLong(i);
    }

    @Implementation
    public float getFloat(int i) {
        return wrappedCursor.getFloat(i);
    }

    @Implementation
    public double getDouble(int i) {
        return wrappedCursor.getDouble(i);
    }

    @Implementation
    public boolean isNull(int i) {
        return wrappedCursor.isNull(i);
    }

    @Implementation
    public void deactivate() {
        wrappedCursor.deactivate();
    }

    @Implementation
    public boolean requery() {
        return wrappedCursor.requery();
    }

    @Implementation
    public void close() {
        wrappedCursor.close();
    }

    @Implementation
    public boolean isClosed() {
        return wrappedCursor.isClosed();
    }

    @Implementation
    public void registerContentObserver(ContentObserver contentObserver) {
        wrappedCursor.registerContentObserver(contentObserver);
    }

    @Implementation
    public void unregisterContentObserver(ContentObserver contentObserver) {
        wrappedCursor.unregisterContentObserver(contentObserver);
    }

    @Implementation
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        wrappedCursor.registerDataSetObserver(dataSetObserver);
    }

    @Implementation
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        wrappedCursor.unregisterDataSetObserver(dataSetObserver);
    }

    @Implementation
    public void setNotificationUri(ContentResolver contentResolver, Uri uri) {
        wrappedCursor.setNotificationUri(contentResolver, uri);
    }

    @Implementation
    public boolean getWantsAllOnMoveCalls() {
        return wrappedCursor.getWantsAllOnMoveCalls();
    }

    @Implementation
    public Bundle getExtras() {
        return wrappedCursor.getExtras();
    }

    @Implementation
    public Bundle respond(Bundle bundle) {
        return wrappedCursor.respond(bundle);
    }
    
    @Implementation
	public int getType(int columnIndex) {
		return 0;
	}

    @Implementation
	public Cursor getWrappedCursor() {
        return wrappedCursor;
    }

}
