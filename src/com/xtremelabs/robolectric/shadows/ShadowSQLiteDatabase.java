package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 */
@Implements(SQLiteDatabase.class)
public class ShadowSQLiteDatabase {
    private static Connection conn;

    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
    	try {
			Class.forName("org.h2.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:h2:mem:");
		} catch (Exception e) {
			rethrowException( e, "SQL exception in openDatabase" );
		}
        return newInstanceOf(SQLiteDatabase.class);
    }
    
    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
    	
    	SQLStringAndBindings sqlInsertString = buildInsertString( table, values );
    	int generatedKey = -1;
    	
    	try {
	    	PreparedStatement statement = conn.prepareStatement( sqlInsertString.sql );
	    	Iterator<Object> colIter = sqlInsertString.colValues.iterator();
	    	int i = 1;
	    	while ( colIter.hasNext() ) {
	    		statement.setObject( i++, colIter.next() );
	    	}
	    	
	    	statement.executeUpdate();
	    	// TODO fetch the generated key
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in insert" );
    	}
    	
    	return generatedKey;
    }
    
    @Implementation
    public Cursor query(boolean distinct, String table, String[] columns, 
    		String selection, String[] selectionArgs, String groupBy, 
    		String having, String orderBy, String limit) {
    	ResultSet rs = null;
    	
    	String where = selection;
    	if ( selection != null && selectionArgs != null ) {
    		where = buildWhereClause( selection, selectionArgs );
    	}
    	
    	String sql = SQLiteQueryBuilder.buildQueryString(distinct, table, 
    			columns, where, groupBy, having, orderBy, limit);  
    	
    	try {
	    	Statement statement = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
	    	rs = statement.executeQuery(sql);
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in query" );
    	}
    	
    	SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
    	shadowOf(cursor).setResultSet( rs );
    	return cursor;
    }

    @Implementation
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
    	return query( false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null );
    }
    
    @Implementation
    public Cursor query(String table, String[] columns, String selection,
    		String[] selectionArgs, String groupBy, String having,
    		String orderBy, String limit) {
    	return query( false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit );
    }
    
    @Implementation
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
    	SQLStringAndBindings sqlUpdateString = buildUpdateString( table, values, whereClause, whereArgs );
    	
    	int rowsAffectedCount = 0;

    	try {
	    	PreparedStatement statement = conn.prepareStatement( sqlUpdateString.sql );
	    	Iterator<Object> colIter = sqlUpdateString.colValues.iterator();
	    	int i = 1;
	    	while ( colIter.hasNext() ) {
	    		statement.setObject( i++, colIter.next() );
	    	}
	    	
	    	rowsAffectedCount = statement.executeUpdate();
	    	// TODO fetch the generated key
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in update" );
    	}
    	
    	return rowsAffectedCount;
    }
    
    @Implementation
    public int delete(String table, String whereClause, String[] whereArgs) {
    	// TODO
    	return 0;
    }
    
    @Implementation
    public void execSQL(String sql) throws SQLException {  	
    	if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
    	
    	// TODO map 'autoincrement' (sqlite) to 'auto_increment' (h2) for compatibility.  use case-insensitive replace.

    	Statement statement = conn.createStatement();
    	statement.execute(sql);
    }
    
    @Implementation
    public boolean isOpen() {
    	return (conn != null);
    }
    
    @Implementation
    public void close() {
    	if (!isOpen()) {
    		return;
    	}
    	try {
			conn.close();
			conn = null;
		} catch (SQLException e) {
			rethrowException( e, "SQL exception in close" );
		}
    }
    
    /**
     * Allows test cases access to the underlying JDBC connection, for use in
     * setup or assertions.
     * 
     * @return
     */
    public Connection getConnection() {
    	return conn;
    }
    
    // TODO DRY out with ShadowSQLiteCursor
	private static void rethrowException( Exception e, String msg ) {
		AssertionError ae = new AssertionError( msg );
		ae.initCause(e);
		throw ae;
	}
	
	/**
	 * Create a SQL INSERT string.  Values are bound via JDBC to facilitate
	 * various data types.
	 * 
	 * @param table
	 * @param values
	 * @return
	 */
	private SQLStringAndBindings buildInsertString(String table, ContentValues values) {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "INSERT INTO " );
		sb.append( table );
		sb.append( " " );
		
		SQLStringAndBindings colValuesClause = buildColumnValuesClause( values );	
		sb.append( colValuesClause.sql );
		sb.append( ";" );
	
		return new SQLStringAndBindings( sb.toString(), colValuesClause.colValues );
	}
	
	/**
	 * Create a SQL UPDATE string.  Values are bound via JDBC to facilitate
	 * various data types.
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	private SQLStringAndBindings buildUpdateString(String table, ContentValues values, String whereClause, String[] whereArgs ) {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "UPDATE " );
		sb.append( table );
		sb.append( " SET " );		
		
		SQLStringAndBindings colAssignmentsClause = buildColumnAssignmentsClause( values );	
		sb.append( colAssignmentsClause.sql );
		
		if ( whereClause != null ) {
			String where = whereClause;
			if ( whereArgs != null ) {
				where = buildWhereClause( whereClause, whereArgs );
			}
			sb.append( " WHERE " );
			sb.append( where );
		}
		sb.append(";");
		
		return new SQLStringAndBindings( sb.toString(), colAssignmentsClause.colValues );
	}
	
	/**
	 * Build a WHERE clause used in SELECT and UPDATE statements.
	 * And probably DELETE too.
	 * 
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	private String buildWhereClause( String selection, String[] selectionArgs ) {
		String retVal = selection;
		
		for ( int i = 0; i < selectionArgs.length; i++ ) {
			retVal = retVal.replace("?", "'" + selectionArgs[i] + "'" );
		}
		
		return retVal;
	}
	
	/**
	 * Build the '(columns...) VALUES (values...)' clause used in INSERT
	 * statements.
	 * 
	 * @param values
	 * @return
	 */
	private SQLStringAndBindings buildColumnValuesClause( ContentValues values ) {
		StringBuilder sb = new StringBuilder();		
		sb.append( "(" );
		
		List<Object> columnValues = new ArrayList<Object>(values.size());
		
		Set<Entry<String,Object>> items = values.valueSet();
		Iterator<Entry<String,Object>> itemsIter = items.iterator();
		while( itemsIter.hasNext() ) {
			Entry<String,Object> thisEntry = itemsIter.next();
			sb.append( thisEntry.getKey() );
			if ( itemsIter.hasNext() ) {
				sb.append( "," );
			}
			sb.append( " " );
			columnValues.add( thisEntry.getValue() );
		}
		sb.append(") VALUES (");
		for ( int i = 0; i < values.size() -1; i++ ) {
			sb.append( "?, ");
		}
		sb.append("?)");
		
		return new SQLStringAndBindings( sb.toString(), columnValues );
	}
	
	/**
	 * Build the '(col1=?, col2=? ... )' clause used in UPDATE statements.
	 * 
	 * @return
	 */
	private SQLStringAndBindings buildColumnAssignmentsClause( ContentValues values ) {
		StringBuilder sb = new StringBuilder();		
		
		List<Object> columnValues = new ArrayList<Object>(values.size());
		
		Set<Entry<String,Object>> items = values.valueSet();
		Iterator<Entry<String,Object>> itemsIter = items.iterator();
		while( itemsIter.hasNext() ) {
			Entry<String,Object> thisEntry = itemsIter.next();
			sb.append( thisEntry.getKey() );
			sb.append("=?");
			if ( itemsIter.hasNext() ) {
				sb.append( "," );
			}
			sb.append( " " );
			columnValues.add( thisEntry.getValue() );
		}
		
		return new SQLStringAndBindings( sb.toString(), columnValues );
	}
	
	/**
	 * Container for a SQL fragment and the objects which are to be
	 * bound to the arguments in the fragment.
	 */
	private class SQLStringAndBindings {
		public String sql;
		public List<Object> colValues;

		public SQLStringAndBindings(String sql, List<Object> colValues) {
			this.sql = sql;
			this.colValues = colValues;
		}
	}

}
