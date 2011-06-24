package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteCursor;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simulates an Android Cursor object, by wrapping a JDBC ResultSet.
 */
@Implements(SQLiteCursor.class)
public class ShadowSQLiteCursor extends ShadowAbstractCursor {

    private ResultSet resultSet;
    String[] columnNames;
    private int rowCount;

    @Implementation
    public int getCount() {
    	
        return rowCount;
    }

    /**
     * Stores the column names so they are retrievable after the resultSet has closed
     */
    private void cacheColumnNames(ResultSet rs) {
    	try {
            ResultSetMetaData metaData = rs.getMetaData();
            String[] colNames = new String[metaData.getColumnCount()];
            int columnCount = metaData.getColumnCount();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                colNames[columnIndex - 1] = metaData.getColumnName(columnIndex);
            }
            this.columnNames = colNames;
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in cacheColumnNames", e);
        }
    }
    
    @Implementation
    public String[] getColumnNames() {
    	return columnNames;
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
    	boolean result = false;
    	if (resultSet==null) return false;
        try {
        	if(resultSet.isBeforeFirst()) {
                result = resultSet.next();
        	} else if (resultSet.isFirst()) {
        		result = true;
        	} else {
        		result = false;
        	}
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToFirst", e);
        }
        if (result=true) super.moveToFirst();
        return result;
    }

    @Implementation
    @Override
    public boolean moveToNext() {
    	if (resultSet==null) return false;
        try {
            resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in moveToNext", e);
        }
        return super.moveToNext();
    }
    
    @Implementation
    @Override
    public boolean moveToPosition(int pos) {
    	int plusone = pos+1;
    	try {
    		if (plusone<resultSet.getRow()) throw new RuntimeException("Cannot moveToPosition(" + pos + "), cursor is TYPE_FORWARD_ONLY, and current position is beyond that.");
    		while(plusone>resultSet.getRow())
    		resultSet.next();
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
            Object o = resultSet.getObject(columnIndex + 1);
            return o == null;
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
    
    /**
     * Allows test cases access to the underlying JDBC ResultSetMetaData, for use in
     * assertions. Available even if cl
     *
     * @return the result set
     */
    public ResultSet getResultSetMetaData() {
        return resultSet;
    }

    private void setRowCount(String sql, Connection connection) throws SQLException {
    	Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sql);
        int count = 0;
        
         if (rs.next()) {  
        	         // here you know that there is at least one record  
        	    do {  
        	    	count++;   // here you do whatever needs to be done for each record. Note that it will be called for the first record.  
        	     } while (rs.next());  
        	 } else {  
        		 rs.close();
        	   this.close();  // here you do whatever needs to be done when there is no record  
        	 } 
        
        rowCount = count;
        
    }
    
    public void setResultSet(ResultSet result, String sql) {
        this.resultSet = result;
        rowCount = 0;

        // Cache count up front, since computing result count in JDBC
        // is destructive to cursor position.
        if (resultSet != null) {
        	cacheColumnNames(resultSet);
        	try {
        		setRowCount(sql,result.getStatement().getConnection());
			} catch (SQLException e) {
			    throw new RuntimeException("SQL exception in setResultSet", e);
			}
        }
    }
}
