package com.xtremelabs.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.equalTo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class CursorAdapterTest {

	@Test
	public void testChangeCursor() {
		Cursor curs = new SQLiteCursor( null, null, null, null );
		CursorAdapter adapter = new TestAdapter( curs );
		assertThat( adapter.getCursor(), notNullValue() );
		assertThat( adapter.getCursor(), sameInstance( curs ) );
		
		adapter.changeCursor( null );
		assertThat( curs.isClosed(), equalTo( true ) );
		assertThat( adapter.getCursor(), nullValue() );		
	}
	
	private class TestAdapter extends CursorAdapter {

		public TestAdapter( Cursor curs ) {
			super( Robolectric.application, curs, false );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) { }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return null;
		}		
	}
}
