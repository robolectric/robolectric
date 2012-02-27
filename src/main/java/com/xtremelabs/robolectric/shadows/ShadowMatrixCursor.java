package com.xtremelabs.robolectric.shadows;

import android.database.CursorIndexOutOfBoundsException;
import android.database.MatrixCursor;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@Implements(MatrixCursor.class)
public class ShadowMatrixCursor extends ShadowAbstractCursor {
    private List<Object[]> data = new ArrayList<Object[]>();

    public void __constructor__(String[] columns) {
        __constructor__(columns, 0);
    }

    public void __constructor__(String[] columns, int initialCapacity) {
        columnNameArray = columns;
    }

    @Implementation
    public void addRow(Object[] data) {
        rowCount++;
        this.data.add(data);
    }

    @Implementation
    public String getString(int column) {
        return (String) get(column);
    }

    @Implementation
    public long getLong(int column) {
        return (Long) get(column);
    }

    @Implementation
    public short getShort(int column) {
        return (Short) get(column);
    }

    @Implementation
    public int getInt(int column) {
        return (Integer) get(column);
    }

    @Implementation
    public float getFloat(int column) {
        return (Float) get(column);
    }

    @Implementation
    public double getDouble(int column) {
        return (Double) get(column);
    }

    @Implementation
    public boolean isNull(int column) {
        return get(column) == null;
    }

    private Object get(int column) {
        if (column < 0 || column >= columnNameArray.length) {
            throw new CursorIndexOutOfBoundsException(null);
        }
        if (currentRowNumber < 0) {
            throw new CursorIndexOutOfBoundsException("Before first row.");
        }
        if (currentRowNumber >= rowCount) {
            throw new CursorIndexOutOfBoundsException("After last row.");
        }
        return data.get(currentRowNumber)[column];
    }
}
