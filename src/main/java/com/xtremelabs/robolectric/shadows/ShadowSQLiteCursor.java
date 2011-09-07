package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteCursor;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulates an Android Cursor object, by wrapping a JDBC ResultSet.
 */
@Implements(SQLiteCursor.class)
public class ShadowSQLiteCursor extends ShadowAbstractCursor {

    private ResultSet resultSet;
    
    
    /**
     * Stores the column names so they are retrievable after the resultSet has closed
     */
    private void cacheColumnNames(ResultSet rs) {
    	try {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();    
            columnNameArray = new String[columnCount];
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                String cName = metaData.getColumnName(columnIndex).toLowerCase();
                this.columnNames.put(cName, columnIndex-1);
                this.columnNameArray[columnIndex-1]=cName;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in cacheColumnNames", e);
        }
    }
    
   

  
    private Integer getColIndex(String columnName) {
        if (columnName == null) {
            return -1;
        }
        
        Integer i  = this.columnNames.get(columnName.toLowerCase());
        if (i==null) return -1;
        return i;
    }
    
    @Implementation
    public int getColumnIndex(String columnName) {
    	return getColIndex(columnName);
    }

    @Implementation
    public int getColumnIndexOrThrow(String columnName) {
    	Integer columnIndex = getColIndex(columnName);
        if (columnIndex == -1) {
            throw new IllegalArgumentException("Column index does not exist");
        }
        return columnIndex;
    }

    @Implementation
    @Override
    public final boolean moveToLast() {
        return super.moveToLast();
    }
    
    @Implementation
    @Override
    public final boolean moveToFirst() {
        return super.moveToFirst();
    }

    @Implementation
    @Override
    public boolean moveToNext() {
        return super.moveToNext();
    }
    
    @Implementation
    @Override
    public boolean moveToPrevious() {
        return super.moveToPrevious();
    }
    
    @Implementation
    @Override
    public boolean moveToPosition(int pos) {
    	return super.moveToPosition(pos);
    }

    @Implementation
    public byte[] getBlob(int columnIndex) {
    	checkPosition();
        return (byte[]) this.currentRow.get(getColumnNames()[columnIndex]);
    }

    @Implementation
    public String getString(int columnIndex) {
        checkPosition();
        Object value = this.currentRow.get(getColumnNames()[columnIndex]);
        if (value instanceof Clob) {
            try {
                return ((Clob) value).getSubString(1, (int)((Clob) value).length());
            } catch (SQLException x) {
                throw new RuntimeException(x);
            }
        } else {
            return (String)value;
        }
    }
	
	@Implementation
	public short getShort(int columnIndex) {
		checkPosition();
		Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    	if (o==null) return 0;
        return new Short(o.toString());
	}
	
    @Implementation
    public int getInt(int columnIndex) {
    	checkPosition();
    	Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    	if (o==null) return 0;
        return new Integer(o.toString());
    }

    @Implementation
    public long getLong(int columnIndex) {
    	checkPosition();
    	Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    	if (o==null) return 0;
        return new Long(o.toString());
    }

    @Implementation
    public float getFloat(int columnIndex) {
    	checkPosition();
    	Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    	if (o==null) return 0;
        return new Float(o.toString());
        
    }

    @Implementation
    public double getDouble(int columnIndex) {
    	checkPosition();
    	Object o =this.currentRow.get(getColumnNames()[columnIndex]);
    	if (o==null) return 0;
    	return new Double(o.toString());
    }
    
    private void checkPosition() {
        if (-1 == currentRowNumber || getCount() == currentRowNumber) {      
            throw new IndexOutOfBoundsException(currentRowNumber + " " + getCount());
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
            rows = null;
            currentRow = null;
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
        Object o = this.currentRow.get(getColumnNames()[columnIndex]);
        return o == null;
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
    
    /**
     * loads a row's values
     * @param rs
     * @return
     * @throws SQLException
     */
    private Map<String,Object> fillRowValues(ResultSet rs) throws SQLException {
    	Map<String,Object> row = new HashMap<String,Object>();
    	for (String s : getColumnNames()) {
			  row.put(s, rs.getObject(s));
    	}
    	return row;
    }
    private void fillRows(String sql, Connection connection) throws SQLException {
    	//ResultSets in SQLite\Android are only TYPE_FORWARD_ONLY. Android caches results in the WindowedCursor to allow moveToPrevious() to function.
    	//Robolectric will have to cache the results too. In the rows map.
    	Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sql);
        int count = 0;
        if (rs.next()) {  
        	     do {
        	    	Map<String,Object> row = fillRowValues(rs);
         	    	rows.put(count, row);
        	    	count++;
        	     } while (rs.next());  
        	 } else {  
        		 rs.close(); 
        	 } 
        
        rowCount = count;
        
    }
    
    public void setResultSet(ResultSet result, String sql) {
        this.resultSet = result;
        rowCount = 0;

        //Cache all rows.  Caching rows should be thought of as a simple replacement for ShadowCursorWindow
        if (resultSet != null) {
        	cacheColumnNames(resultSet);
        	try {
        		fillRows(sql, result.getStatement().getConnection());
			} catch (SQLException e) {
			    throw new RuntimeException("SQL exception in setResultSet", e);
			}
        }
    }
}
