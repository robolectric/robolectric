package com.xtremelabs.robolectric.shadows;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.database.sqlite.SQLiteCursor;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteCursorTest {
	
	private Connection conn;
	private ResultSet rs;
	private SQLiteCursor cursor;
	
	@Before
	public void setUp() throws Exception {
		Class.forName("org.h2.Driver").newInstance();
		conn = DriverManager.getConnection("jdbc:h2:mem:");
		
		Statement statement = conn.createStatement();
		statement.execute("CREATE TABLE table_name(" +
				 	      "id INT PRIMARY KEY, name VARCHAR(255), long_value BIGINT," +
				 	      "float_value REAL, double_value DOUBLE, blob_value BINARY );");
		
		addPeople();
		setupCursor();
	}

	@After
	public void tearDown() throws Exception {
		conn.close();
	}
	
	@Test
	public void testGetColumnNames() throws Exception {
		String[] colNames = cursor.getColumnNames();

		assertColumnNames( colNames );
	}

	@Test
	public void testGetColumnNamesEmpty() throws Exception {
		setupEmptyResult();
		String[] colNames = cursor.getColumnNames();

		// Column names are present even with an empty result.
		assertThat( colNames, notNullValue() );
		assertColumnNames( colNames );
	}
	
	@Test
	public void testGetColumnIndex() throws Exception {
		int index;

		index = cursor.getColumnIndex("id");
		assertThat( index, equalTo(0) );
		
		index = cursor.getColumnIndex("name");
		assertThat( index, equalTo(1) );
	}
	
	@Test
	public void testGetColumnIndexNotFound() throws Exception {
		int index = cursor.getColumnIndex("Fred");
		assertThat( index, equalTo(-1) );
	}
	
	@Test
	public void testGetColumnIndexEmpty() throws Exception {
		int index;
		
		setupEmptyResult();
		
		index = cursor.getColumnIndex("id");
		assertThat( index, equalTo(0) );
		
		index = cursor.getColumnIndex("name");
		assertThat( index, equalTo(1) );		
	}
	
	@Test
	public void testGetColumnIndexOrThrow() throws Exception {
		int index;

		index = cursor.getColumnIndexOrThrow("id");
		assertThat( index, equalTo(0) );
		
		index = cursor.getColumnIndexOrThrow("name");
		assertThat( index, equalTo(1) );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetColumnIndexOrThrowNotFound() throws Exception {
		cursor.getColumnIndexOrThrow("Fred");		
	}
	
	@Test
	public void testGetColumnIndexOrThrowEmpty() throws Exception {
		setupEmptyResult();
		
		int index = cursor.getColumnIndexOrThrow("name");
		assertThat( index, equalTo(1) );
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetColumnIndexOrThrowNotFoundEmpty() throws Exception {
		setupEmptyResult();
		
		cursor.getColumnIndexOrThrow("Fred");
	}
	
	@Test
	public void testMoveToFirst() throws Exception {
		boolean hasData = cursor.moveToFirst();

		assertThat( hasData, equalTo(true) );
		assertThat( cursor.getInt(0), equalTo(1234) );
		assertThat( cursor.getString(1), equalTo("Chuck") );
	}
	
	@Test
	public void testMoveToFirstEmpty() throws Exception {
		setupEmptyResult();
		
		boolean hasData = cursor.moveToFirst();
		
		assertThat( hasData, equalTo(false) );	
	}
	
	@Test
	public void testMoveToNext() throws Exception {
		cursor.moveToFirst();
		
		boolean hasData = cursor.moveToNext();

		assertThat( hasData, equalTo(true) );
		assertThat( cursor.getInt(0), equalTo(1235) );
		assertThat( cursor.getString(1), equalTo("Julie") );
	}
	
	@Test
	public void testMoveToNextPastEnd() throws Exception {
		cursor.moveToFirst();
		
		boolean hasData = cursor.moveToNext();
		hasData = cursor.moveToNext();
		hasData = cursor.moveToNext();
		
		assertThat( hasData, equalTo(false) );
	}
	
	@Test
	public void testMoveToNextEmpty() throws Exception {
		setupEmptyResult();
		
		cursor.moveToFirst();
		boolean hasData = cursor.moveToNext();
		assertThat( hasData, equalTo(false) );
	}
	 
	@Test
	public void testGetPosition() throws Exception {
		cursor.moveToFirst();
		
		assertThat( cursor.getPosition(), equalTo( 0 ) );
		cursor.moveToNext();
		assertThat( cursor.getPosition(), equalTo( 1 ) );
	}
	
	@Test
	public void testGetBlob() throws Exception {		
		String sql = "UPDATE table_name set blob_value=? where id=1234";
		byte[] byteData = sql.getBytes();
		
		PreparedStatement statement = conn.prepareStatement( sql );
		statement.setObject(1, byteData);
		statement.executeUpdate();
		
		setupCursor();
		cursor.moveToFirst();
		
		byte[] retrievedByteData = cursor.getBlob( 5 );
		
		assertThat( byteData.length, equalTo( retrievedByteData.length ));
		
		for ( int i = 0; i < byteData.length; i++ ) {
			assertThat( byteData[i], equalTo( retrievedByteData[i]));
		}
	}
	
	@Test
	public void testGetString() throws Exception {
		cursor.moveToFirst();
		
		String[] data = { "Chuck", "Julie", "Chris" };
		
		for ( int i = 0; i < data.length; i++ ) {
			assertThat( cursor.getString(1), equalTo(data[i]) );
			cursor.moveToNext();
		}	
	}
	
	@Test
	public void testGetInt() throws Exception {
		cursor.moveToFirst();
		
		int[] data = { 1234, 1235, 1236 };
		
		for ( int i = 0; i < data.length; i++ ) {
			assertThat( cursor.getInt(0), equalTo(data[i]) );
			cursor.moveToNext();
		}
	}
	
	@Test
	public void testGetLong() throws Exception {
		cursor.moveToFirst();
		
		assertThat( cursor.getLong(2), equalTo(3463L) );
	}
	
	@Test
	public void testGetFloat() throws Exception {
		cursor.moveToFirst();
		
		assertThat( cursor.getFloat(3), equalTo((float)1.5) );	
	}
	
	@Test
	public void testGetDouble() throws Exception {
		cursor.moveToFirst();
		
		assertThat( cursor.getDouble(4), equalTo(3.14159) );	
	}
	
	@Test
	public void testClose() throws Exception {
		assertThat( cursor.isClosed(), equalTo(false) );
		cursor.close();
		assertThat( cursor.isClosed(), equalTo(true) );
	}

	
	private void addPeople() throws Exception {
		String[] statements = {
			"INSERT INTO table_name (id, name, long_value, float_value, double_value) VALUES(1234, 'Chuck', 3463, 1.5, 3.14159);",
			"INSERT INTO table_name (id, name) VALUES(1235, 'Julie');",
			"INSERT INTO table_name (id, name) VALUES(1236, 'Chris');"				
		};
		
		for ( int i = 0; i < statements.length; i++ ) {
			Statement statement = conn.createStatement();
			statement.executeUpdate( statements[i] );
		}
	}
	
	private void setupCursor() throws Exception {
		Statement statement = conn.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );
		rs = statement.executeQuery( "SELECT * FROM table_name;" );
		cursor = new SQLiteCursor( null, null, null, null );
		Robolectric.shadowOf(cursor).setResultSet( rs );
	}
	
	private void setupEmptyResult() throws Exception {
		Statement statement = conn.createStatement();
		statement.executeUpdate( "DELETE FROM table_name;" );

		setupCursor();
	}
	
	private void assertColumnNames(String[] colNames) {
		assertThat( colNames.length, equalTo(6) );
		assertThat( colNames[0], equalTo("ID") );
		assertThat( colNames[1], equalTo("NAME") );
		assertThat( colNames[2], equalTo("LONG_VALUE") );
		assertThat( colNames[3], equalTo("FLOAT_VALUE") );
		assertThat( colNames[4], equalTo("DOUBLE_VALUE") );
		assertThat( colNames[5], equalTo("BLOB_VALUE") );
	}

}
