package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentValues;
import android.database.AbstractCursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class AbstractCursorTest {
	
	private TestCursor cursor;

	@Before
	public void setUp() throws Exception {
		
        Robolectric.bindShadowClass(ContentValues.class, ShadowContentValues.class);
        Robolectric.bindShadowClass(AbstractCursor.class, ShadowAbstractCursor.class);
        
        cursor = new TestCursor();
	}

	@Test
	public void testMoveToFirst() {
		cursor.theTable.add( "Foobar" );		
    	assertThat(cursor.moveToFirst(), equalTo(true));
    	assertThat(cursor.getCount(), equalTo(1));
	}
	
	@Test
	public void testMoveToFirstEmptyList() {		
    	assertThat(cursor.moveToFirst(), equalTo(false));
    	assertThat(cursor.getCount(), equalTo(0));
	}

	@Test
	public void testGetPosition() {
		cursor.theTable.add( "Foobar" );
		cursor.theTable.add( "Bletch" );
		
    	assertThat(cursor.moveToFirst(), equalTo(true));
		assertThat(cursor.getCount(), equalTo(2));
    	assertThat(cursor.getPosition(), equalTo(0));
	}

	@Test
	public void testGetPositionSingleEntry() {
		cursor.theTable.add( "Foobar" );
		
    	assertThat(cursor.moveToFirst(), equalTo(true));
		assertThat(cursor.getCount(), equalTo(1));
    	assertThat(cursor.getPosition(), equalTo(0));
	}
	
	@Test
	public void testGetPositionEmptyList() {
    	assertThat(cursor.moveToFirst(), equalTo(false));
		assertThat(cursor.getCount(), equalTo(0));
    	assertThat(cursor.getPosition(), equalTo(0));
	}
	
	@Test
	public void testMoveToNext() {
		cursor.theTable.add( "Foobar" );
		cursor.theTable.add( "Bletch" );
		
		assertThat(cursor.moveToFirst(), equalTo(true));
		assertThat(cursor.getCount(), equalTo(2));
    	assertThat(cursor.moveToNext(), equalTo(true));
    	assertThat(cursor.getPosition(), equalTo(1));
	}
	
	@Test
	public void testAttemptToMovePastEnd() {
		cursor.theTable.add( "Foobar" );
		cursor.theTable.add( "Bletch" );
		
		assertThat(cursor.moveToFirst(), equalTo(true));
		assertThat(cursor.getCount(), equalTo(2));
    	assertThat(cursor.moveToNext(), equalTo(true));
    	assertThat(cursor.moveToNext(), equalTo(false));
    	assertThat(cursor.getPosition(), equalTo(1));
	}
	
	@Test
	public void testAttemptToMovePastSingleEntry() {
		cursor.theTable.add( "Foobar" );
		
		assertThat(cursor.moveToFirst(), equalTo(true));
		assertThat(cursor.getCount(), equalTo(1));
    	assertThat(cursor.moveToNext(), equalTo(false));
    	assertThat(cursor.getPosition(), equalTo(0));
	}
	
	@Test
	public void testAttemptToMovePastEmptyList() {
		assertThat(cursor.moveToFirst(), equalTo(false));
		assertThat(cursor.getCount(), equalTo(0));
    	assertThat(cursor.moveToNext(), equalTo(false));
    	assertThat(cursor.getPosition(), equalTo(0));
	}
	
	private class TestCursor extends AbstractCursor {

		public List<Object> theTable = new ArrayList<Object>();
    	
        @Override
        public int getCount() {
            return theTable.size();
        }

		@Override
		public String[] getColumnNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getDouble(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public float getFloat(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getInt(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getLong(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public short getShort(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getString(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isNull(int arg0) {
			// TODO Auto-generated method stub
			return false;
		}

    };

}
