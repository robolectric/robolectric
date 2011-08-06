package com.xtremelabs.robolectric.shadows;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.jdbc.JdbcSQLException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SQLiteStatement.class)
public class ShadowSQLiteStatement extends ShadowSQLiteProgram {
	// @RealObject private SQLiteStatement realSQLiteStatement;
	String mSql;


	public void init(SQLiteDatabase db, String sql) {
		super.init(db, sql);
		mSql = sql;
		
	}
	@Implementation
	  public void execute() {
	        if (!mDatabase.isOpen()) {
	            throw new IllegalStateException("database " + mDatabase.getPath() + " already closed");
	        }
	        try {
				actualDBstatement.execute();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
	  }

	
	 @Implementation
	 public long executeInsert() {
		 try {
			actualDBstatement.executeUpdate();
            ResultSet resultSet = actualDBstatement.getGeneratedKeys();

         if (resultSet.next()) {
             return resultSet.getLong(1);
         } else {
        	 throw new RuntimeException("Could not retrive generatedKeys");
         }
		 } catch (SQLException e) {
			 throw new RuntimeException(e);
		 }
	 }
	 
	 @Implementation
	 public long simpleQueryForLong() {	 
		ResultSet rs;
		try {
			rs = actualDBstatement.executeQuery();
			rs.first();
			return rs.getLong(1);
		} catch (JdbcSQLException e) {
			if (e.getMessage().contains("No data is available")) throw new android.database.sqlite.SQLiteDoneException("No data is available"); //if the query returns zero rows
			throw new RuntimeException(e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} 
	 }
	 	@Implementation
	    public String simpleQueryForString() {
	 		ResultSet rs;
			try {
				rs = actualDBstatement.executeQuery();
				rs.first();
				return rs.getString(1);
			} catch (JdbcSQLException e) {
				if (e.getMessage().contains("No data is available")) throw new android.database.sqlite.SQLiteDoneException("No data is available"); //if the query returns zero rows
				throw new RuntimeException(e);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} 
	 	}
}