package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteCursor;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Simulates an Android Cursor object, by wrapping a JDBC ResultSet.
 */
@Implements(SQLiteCursor.class)
public class ShadowSQLiteCursor extends ShadowAbstractCursor {

    private ResultSet resultSet;
    private int rowCount;

    @Implementation
    public int getCount() {
        return rowCount;
    }

    @Implementation
    public String[] getColumnNames() {
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            String[] columnNames = new String[metaData.getColumnCount()];
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                columnNames[columnIndex - 1] = metaData.getColumnName(columnIndex);
            }
            return columnNames;
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getColumnNames", e);
        }
    }

    @Implementation
    public int getColumnIndex(String columnName) {
        if (columnName == null) {
            return -1;
        }

        String[] columnNames = getColumnNames();
        for (int columnIndex = 0; columnIndex < columnNames.length; columnIndex++) {
            if (columnNames[columnIndex].equalsIgnoreCase(columnName)) {
                return columnIndex;
            }
        }

        return -1;
    }

    @Implementation
    public int getColumnIndexOrThrow(String columnName) {
        int columnIndex = getColumnIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column index does not exist");
        }
        return columnIndex;
    }

    @Implementation
    @Override
    public final boolean moveToFirst() {
        try {
            resultSet.first();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToFirst", e);
        }
        return super.moveToFirst();
    }

    @Implementation
    @Override
    public boolean moveToNext() {
        try {
            resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToNext", e);
        }
        return super.moveToNext();
    }
    
    @Implementation
    @Override
    public boolean moveToPrevious() {
        try {
            resultSet.previous();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToPrevious", e);
        }
        return super.moveToPrevious();
    }
    
    @Implementation
    @Override
    public boolean moveToPosition(int pos) {
    	try {
    		resultSet.absolute(pos + 1);
    	} catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToPosition", e);
        }
    	return super.moveToPosition(pos);
    }

    @Implementation
    public byte[] getBlob(int columnIndex) {
        try {
            return resultSet.getBytes(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getBlob", e);
        }
    }

    @Implementation
    public String getString(int columnIndex) {
        try {
            return resultSet.getString(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getString", e);
        }
    }

    @Implementation
    public int getInt(int columnIndex) {
        try {
            return resultSet.getInt(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getInt", e);
        }
    }

    @Implementation
    public long getLong(int columnIndex) {
        try {
            return resultSet.getLong(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getLong", e);
        }
    }

    @Implementation
    public float getFloat(int columnIndex) {
        try {
            return resultSet.getFloat(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getFloat", e);
        }
    }

    @Implementation
    public double getDouble(int columnIndex) {
        try {
            return resultSet.getDouble(columnIndex + 1);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in getDouble", e);
        }
    }

    @Implementation
    public void close() {
        if (resultSet == null) {
            return;
        }

        try {
            resultSet.close();
            resultSet = null;
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in close", e);
        }
    }

    @Implementation
    public boolean isClosed() {
        return (resultSet == null);
    }

    @Implementation
    public boolean isNull(int columnIndex) {
        try {
            resultSet.getObject(columnIndex + 1);
            return resultSet.wasNull();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in isNull", e);
        }
    }

    /**
     * Allows test cases access to the underlying JDBC ResultSet, for use in
     * assertions.
     *
     * @return the result set
     */
    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet result) {
        this.resultSet = result;
        rowCount = 0;

        // Cache count up front, since computing result count in JDBC
        // is destructive to cursor position.
        if (resultSet != null) {
            try {
                resultSet.beforeFirst();
                resultSet.last();
                rowCount = resultSet.getRow();
            } catch (SQLException e) {
                throw new RuntimeException("SQL exception in setResultSet", e);
            }
        }
    }
}
