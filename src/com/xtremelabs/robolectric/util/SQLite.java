package com.xtremelabs.robolectric.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import android.content.ContentValues;

/**
 * 
 * SQL utility methods to support the database-related shadows.
 *
 */
public class SQLite {
	
	/**
	 * Rethrow JDBC exceptions in a form suitable for detection as
	 * Errors in a JUnit test.
	 */
	public static void rethrowException( Exception e, String msg ) {
		AssertionError ae = new AssertionError( msg );
		ae.initCause(e);
		throw ae;
	}

	/**
	 * Create a SQL INSERT string.  Returned values are then bound via
	 * JDBC to facilitate various data types.
	 * 
	 * @param table
	 * @param values
	 * @return
	 */
	public static SQLStringAndBindings buildInsertString(String table, ContentValues values) {
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
	 * Create a SQL UPDATE string.  Returned values are then bound via
	 * JDBC to facilitate various data types.
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static SQLStringAndBindings buildUpdateString( String table, ContentValues values, String whereClause, String[] whereArgs ) {
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
	 * Create a SQL DELETE string.
	 * 
	 * @param table
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public static String buildDeleteString( String table, String whereClause, String[] whereArgs ) {
		StringBuilder sb = new StringBuilder();
		
		sb.append( "DELETE FROM " );
		sb.append( table );

		if ( whereClause != null ) {
			String where = whereClause;
			if ( whereArgs != null ) {
				where = buildWhereClause( whereClause, whereArgs );
			}
			sb.append( " WHERE " );
			sb.append( where );
		}
		sb.append(";");
		
		return sb.toString();
	}
	
	/**
	 * Build a WHERE clause used in SELECT, UPDATE and DELETE statements.
	 * 
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public static String buildWhereClause( String selection, String[] selectionArgs ) {
		String retVal = selection;
		
		for ( int i = 0; i < selectionArgs.length; i++ ) {
			retVal = retVal.replaceFirst( "\\?", "'" + selectionArgs[i] + "'" );
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
	public static SQLStringAndBindings buildColumnValuesClause( ContentValues values ) {
		StringBuilder sb = new StringBuilder();		
		sb.append( "(" );
		
		List<Object> columnValues = new ArrayList<Object>(values.size());
		
		Set<Entry<String,Object>> items = values.valueSet();
		Iterator<Entry<String,Object>> itemsIter = items.iterator();
		while( itemsIter.hasNext() ) {
			Entry<String,Object> thisEntry = itemsIter.next();
			sb.append( thisEntry.getKey() );
			if ( itemsIter.hasNext() ) {
				sb.append( ", " );
			}
			columnValues.add( thisEntry.getValue() );
		}
		sb.append( ") VALUES (" );
		for ( int i = 0; i < values.size() -1; i++ ) {
			sb.append( "?, ");
		}
		sb.append( "?)" );
		
		return new SQLStringAndBindings( sb.toString(), columnValues );
	}
	
	/**
	 * Build the '(col1=?, col2=? ... )' clause used in UPDATE statements.
	 * 
	 * @return
	 */
	public static SQLStringAndBindings buildColumnAssignmentsClause( ContentValues values ) {
		StringBuilder sb = new StringBuilder();		
		
		List<Object> columnValues = new ArrayList<Object>(values.size());
		
		Set<Entry<String,Object>> items = values.valueSet();
		Iterator<Entry<String,Object>> itemsIter = items.iterator();
		while( itemsIter.hasNext() ) {
			Entry<String,Object> thisEntry = itemsIter.next();
			sb.append( thisEntry.getKey() );
			sb.append( "=?" );
			if ( itemsIter.hasNext() ) {
				sb.append( ", " );
			}
			columnValues.add( thisEntry.getValue() );
		}
		
		return new SQLStringAndBindings( sb.toString(), columnValues );
	}
	
	/**
	 * Container for a SQL fragment and the objects which are to be
	 * bound to the arguments in the fragment.
	 */
	public static class SQLStringAndBindings {
		public String sql;
		public List<Object> colValues;

		public SQLStringAndBindings(String sql, List<Object> colValues) {
			this.sql = sql;
			this.colValues = colValues;
		}
	}

}
