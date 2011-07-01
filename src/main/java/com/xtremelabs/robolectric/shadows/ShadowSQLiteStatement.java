package com.xtremelabs.robolectric.shadows;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;

import com.xtremelabs.robolectric.Robolectric;
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
}
