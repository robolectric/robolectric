package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.SQLite.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 * Implemented as a wrapper around an embedded SQL database, accessed via JDBC.  The JDBC connection is
 * made available to test cases for use in fixture setup and assertions.
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
    	long generatedKey = -1;
    	
    	try {
	    	PreparedStatement statement = conn.prepareStatement(sqlInsertString.sql, Statement.RETURN_GENERATED_KEYS );
	    	Iterator<Object> colIter = sqlInsertString.colValues.iterator();
	    	int i = 1;
	    	while ( colIter.hasNext() ) {
	    		statement.setObject( i++, colIter.next() );
	    	}
	    	
	    	statement.executeUpdate();
	    	
	    	ResultSet rs = statement.getGeneratedKeys();
	    	if ( rs.first() ) {
	    		generatedKey = rs.getLong(1);
	    	}
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
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in update" );
    	}
    	
    	return rowsAffectedCount;
    }
    
    @Implementation
    public int delete(String table, String whereClause, String[] whereArgs) {
    	String sql = buildDeleteString( table, whereClause, whereArgs );
    	
    	int rowsAffectedCount = 0;

    	try {
	    	PreparedStatement statement = conn.prepareStatement( sql );
	    	rowsAffectedCount = statement.executeUpdate();
    	} catch( SQLException e ) {
			rethrowException( e, "SQL exception in delete" );
    	}
    	
    	return rowsAffectedCount;
    }
    
    @Implementation
    public void execSQL(String sql) throws SQLException {  	
    	if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }
    	
    	// Map 'autoincrement' (sqlite) to 'auto_increment' (h2).
    	String scrubbedSQL = sql.replaceAll("(?i:autoincrement)", "auto_increment");

    	Statement statement = conn.createStatement();
    	statement.execute(scrubbedSQL);
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

}
