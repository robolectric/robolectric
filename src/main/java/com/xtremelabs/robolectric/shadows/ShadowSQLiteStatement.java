package com.xtremelabs.robolectric.shadows;



import java.util.ArrayList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.widget.AdapterView;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(SQLiteStatement.class)
public class ShadowSQLiteStatement extends ShadowSQLiteProgram {
	//@RealObject private SQLiteStatement realSQLiteStatement;
	SQLiteDatabase mDb;
	String mSql;
	 public void init(SQLiteDatabase db,  String sql) {
		 super.__constructor__(db,sql);
		 mSql = sql;
	    }
}
