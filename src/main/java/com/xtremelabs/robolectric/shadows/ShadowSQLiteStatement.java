package com.xtremelabs.robolectric.shadows;



import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.xtremelabs.robolectric.internal.Implements;

@Implements(SQLiteStatement.class)
public class ShadowSQLiteStatement extends ShadowSQLiteProgram {
	//@RealObject private SQLiteStatement realSQLiteStatement;
	String mSql;
	 public void init(SQLiteDatabase db,  String sql) {
		 super.init(db,sql);
		 mSql = sql;
	    }
}
