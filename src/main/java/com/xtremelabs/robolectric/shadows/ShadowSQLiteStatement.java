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
	String mSql;
	 public void init(SQLiteDatabase db,  String sql) {
		 super.init(db,sql);
		 mSql = sql;
	    }
}
