package com.xtremelabs.robolectric.shadows;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import android.database.sqlite.SQLiteCursor;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@Implements(SQLiteCursor.class)
public class ShadowSQLiteCursor extends ShadowAbstractCursor {
	
	// TODO figure out what to do with the SQLExceptions.  In a test environment,
	// they ought to bubble up to the test case, so they can register as Errors.
	// So we need to rethrow some sort of exception that is appropriate for a
	// test scenario.  I'm sure JUnit has something appropriate.
	
	private ResultSet rs;
	private int rowCount;

	@Implementation
    public int getCount() {
        return rowCount;
    }
	
	@Implementation
	public String[] getColumnNames() {
		ResultSetMetaData md;
		String[] retVal = null;
		
		try {
			md = rs.getMetaData();
			retVal = new String[md.getColumnCount()];
			int colCount = md.getColumnCount();
			for ( int colIndex = 1; colIndex <= colCount; colIndex++ ) {
				retVal[ colIndex - 1] = md.getColumnName( colIndex );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	@Implementation
	public int getColumnIndex(String columnName) {
		if ( columnName == null ) {
			return -1;
		}
		
		String [] columnNames = getColumnNames();
		for ( int colIndex = 0; colIndex < columnNames.length; colIndex++ ) {
			if ( columnNames[colIndex].equalsIgnoreCase(columnName) ) {
				return colIndex;
			}
		}
		
		return -1;
	}
	
	@Implementation
	public int getColumnIndexOrThrow(String columnName) {
		int retVal = getColumnIndex( columnName );
		if ( retVal == -1 ) {
			throw new IllegalArgumentException("Column index does not exist");
		}
		return retVal;
	}
	
    @Implementation
    @Override
    public final boolean moveToFirst() {
    	try {
			rs.first();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return super.moveToFirst();
    }

    @Implementation
    @Override
    public boolean moveToNext() {
    	try {
			rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return super.moveToNext();
    }
    
	@Implementation
    public byte[] getBlob(int columnIndex) {		
		byte[] retVal = null;

        try {
			retVal = rs.getBytes(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return retVal;
    }

	@Implementation
    public String getString(int columnIndex) {
		String retVal = null;
		
		try {
			retVal = rs.getString(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        return retVal;
    }
    
	@Implementation
    public int getInt(int columnIndex) {
		int retVal = 0;
		
		try {
			retVal = rs.getInt(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        return retVal;
    }
    
	@Implementation
    public long getLong(int columnIndex) {
		long retVal = 0L;
		
		try {
			retVal = rs.getLong(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return retVal;
    }
	
	@Implementation
	public float getFloat(int columnIndex) {
		float retVal = (float) 0.0;

		try {
			retVal = rs.getFloat(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	@Implementation
	public double getDouble(int columnIndex) {
		double retVal = 0.0;

		try {
			retVal = rs.getDouble(columnIndex + 1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	@Implementation
	public void close() {
		if ( rs == null ) {
			return;
		}
		
		try {
			rs.close();
			rs = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Implementation
	public boolean isClosed() {
		return ( rs == null );
	}
	
	/**
	 * Allows test cases access to the underlying JDBC ResultSet, for use in
     * assertions.
     *
	 * @return
	 */
	public ResultSet getResultSet() {
		return rs;
	}
	
	public void setResultSet( ResultSet _rs ) {
		rs = _rs;
		rowCount = 0;
		
		// Cache count up front, since computing result count in JDBC
		// is destructive to cursor position.
		if (rs != null)	 {
			try {
				rs.beforeFirst();  
				rs.last();  
				rowCount = rs.getRow();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}  
	}

}
