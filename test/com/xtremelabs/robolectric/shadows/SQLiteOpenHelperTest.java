package com.xtremelabs.robolectric.shadows;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteOpenHelperTest {
	
	private TestOpenHelper helper;

	@Before
	public void setUp() throws Exception {
		helper = new TestOpenHelper(null, "path", null, 1);
	}
	
	@Test
	public void testInitialGetReadableDatabase() throws Exception {	
		SQLiteDatabase db = helper.getReadableDatabase();
		assertInitialDB( db );
	}
	
	@Test
	public void testSubsequentGetReadableDatabase() throws Exception {
		SQLiteDatabase db = helper.getReadableDatabase();
		helper.reset();
		db = helper.getReadableDatabase();
		
		assertSubsequentDB( db );
	}
	
	@Test
	public void testInitialGetWriteableDatabase() throws Exception {
		SQLiteDatabase db = helper.getWritableDatabase();
		assertInitialDB( db );		
	}
	
	@Test
	public void testSubsequentGetWriteableDatabase() throws Exception {
		SQLiteDatabase db = helper.getWritableDatabase();
		helper.reset();
		db = helper.getWritableDatabase();
		
		assertSubsequentDB( db );	
	}
	
	@Test
	public void testClose() throws Exception {
		SQLiteDatabase db = helper.getWritableDatabase();
		
		assertThat( db.isOpen(), equalTo(true));		
		helper.close();
		assertThat( db.isOpen(), equalTo(false));
	}
	
	private void assertInitialDB( SQLiteDatabase db ) {
		assertThat( db, notNullValue() );
		assertThat( db.isOpen(), equalTo(true));
		assertThat( helper.onCreateCalled, equalTo(true) );
		assertThat( helper.onOpenCalled, equalTo(true) );
		assertThat( helper.onUpgradeCalled, equalTo(false) );
	}
	
	private void assertSubsequentDB( SQLiteDatabase db  ){
		assertThat( db, notNullValue() );
		assertThat( db.isOpen(), equalTo(true));
		assertThat( helper.onCreateCalled, equalTo(false) );
		assertThat( helper.onOpenCalled, equalTo(true) );
		assertThat( helper.onUpgradeCalled, equalTo(false) );
	}

	private class TestOpenHelper extends SQLiteOpenHelper {
		
		public boolean onCreateCalled;
		public boolean onUpgradeCalled;
		public boolean onOpenCalled;

		public TestOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
			reset();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onCreateCalled = true;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onUpgradeCalled = true;
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			onOpenCalled = true;
		}
		
		public void reset() {
			onCreateCalled = false;
			onUpgradeCalled = false;
			onOpenCalled = false;
		}
		
	}
	
}
