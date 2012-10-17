package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.Cursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteCursor;

@RunWith(WithTestDefaultsRunner.class)
public class MergeCursorTest {
	
    private Connection connection;

	private MergeCursor cursor;
	private SQLiteCursor dbCursor1;
	private SQLiteCursor dbCursor2;
	
    private static String[] TABLE_1_INSERTS = {
            "INSERT INTO table_1 (id, name_1, value_1, float_value_1, double_value_1) VALUES(1234, 'Chuck', 3463, 1.5, 3.14159);",
            "INSERT INTO table_1 (id, name_1) VALUES(1235, 'Julie');",
            "INSERT INTO table_1 (id, name_1) VALUES(1236, 'Chris');"
    };
    
    private static String[] TABLE_2_INSERTS = {
            "INSERT INTO table_2 (id, name_2, value_2, float_value_2, double_value_2) VALUES(4321, 'Mary', 3245, 5.4, 2.7818);",
            "INSERT INTO table_2 (id, name_2) VALUES(4322, 'Elizabeth');",
            "INSERT INTO table_2 (id, name_2) VALUES(4323, 'Chester');"
    };
	
    @Before
    public void setUp() throws Exception {
    	connection = DatabaseConfig.getMemoryConnection();
    	
    	setupTable1();
    	setupTable2();
    }
    
    private void setupTable1() throws Exception {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE table_1(" +
                "id INTEGER PRIMARY KEY, name_1 VARCHAR(255), value_1 INTEGER," +
                "float_value_1 REAL, double_value_1 DOUBLE, blob_value_1 BINARY, clob_value_1 CLOB );");

        for (String insert : TABLE_1_INSERTS) {
            connection.createStatement().executeUpdate(insert);
        }
        
        statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
        String sql ="SELECT * FROM table_1;";
        ResultSet rs = statement.executeQuery(sql);
        dbCursor1 = new SQLiteCursor(null, null, null, null);
        Robolectric.shadowOf(dbCursor1).setResultSet(rs, sql);
    }

    private void setupTable2() throws Exception {
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE table_2(" +
                "id INTEGER PRIMARY KEY, name_2 VARCHAR(255), value_2 INTEGER," +
                "float_value_2 REAL, double_value_2 DOUBLE, blob_value_2 BINARY, clob_value_2 CLOB );");
        
        for (String insert : TABLE_2_INSERTS) {
            connection.createStatement().executeUpdate(insert);
        }
        
        statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
        String sql ="SELECT * FROM table_2;";
        ResultSet rs = statement.executeQuery(sql);
        dbCursor2 = new SQLiteCursor(null, null, null, null);
        Robolectric.shadowOf(dbCursor2).setResultSet(rs, sql);
    }
    
    @Test
    public void testEmptyCursors() throws Exception {
    	// null cursor list
    	cursor = new MergeCursor( null );
    	assertThat( cursor.getCount(), equalTo(0) );
    	assertThat( cursor.moveToFirst(), equalTo(false) );
    	assertThat( cursor.getColumnNames(), notNullValue() );
    	
    	// cursor list with null contents
    	cursor = new MergeCursor( new Cursor[1] );
    	assertThat( cursor.getCount(), equalTo(0) );    	
    	assertThat( cursor.moveToFirst(), equalTo(false) );
    	assertThat( cursor.getColumnNames(), notNullValue() );

    	// cursor list with partially null contents
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = null;
    	cursors[1] = dbCursor1;
    	cursor = new MergeCursor( cursors );
    	assertThat( cursor.getCount(), equalTo(TABLE_1_INSERTS.length) );
    	assertThat( cursor.moveToFirst(), equalTo(true) );
    	assertThat( cursor.getColumnNames(), notNullValue() );
    }
    
    @Test
    public void testMoveToPositionEmptyCursor() throws Exception {
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = null;
    	cursors[1] = null;
    	
    	cursor = new MergeCursor( cursors );
    	assertThat( cursor.getCount(), equalTo(0) );
    	assertThat( cursor.getColumnNames(), notNullValue() );
    	
    	cursor.moveToPosition(0);

    	assertThat( cursor.getColumnNames(), notNullValue() );    	
    }
    
    @Test
    public void testBoundsSingleCursor() throws Exception {
    	Cursor[] cursors = new Cursor[1];
    	cursors[0] = dbCursor1;
    	
    	assertBounds( cursors, TABLE_1_INSERTS.length );
    }
    
    @Test
    public void testBoundsMultipleCursor() throws Exception {
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = dbCursor1;
    	cursors[1] = dbCursor2;

    	assertBounds( cursors, TABLE_1_INSERTS.length + TABLE_2_INSERTS.length );
    } 
    
	private void assertBounds( Cursor[] cursors, int expectedLength ) {
    	cursor = new MergeCursor( cursors );

		assertThat( cursor.getCount(), equalTo(expectedLength) );
    	assertThat( cursor.moveToFirst(), equalTo(true) );
    	
    	for ( int i = 0; i < expectedLength; i++ ) {
    		assertThat( cursor.moveToPosition(i), equalTo(true) );
        	assertThat( cursor.isAfterLast(), equalTo(false) );
    	}
    	assertThat( cursor.moveToNext(), equalTo(false) );
    	assertThat( cursor.isAfterLast(), equalTo(true) );
    	assertThat( cursor.moveToPosition(expectedLength), equalTo(false) );
	}
    
    @Test
    public void testGetDataSingleCursor() throws Exception {
    	Cursor[] cursors = new Cursor[1];
    	cursors[0] = dbCursor1;
    	cursor = new MergeCursor( cursors );
    	
    	cursor.moveToFirst();
    	assertDataCursor1();
    }

    @Test
    public void testGetDataMultipleCursor() throws Exception {
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = dbCursor1;
    	cursors[1] = dbCursor2;
    	cursor = new MergeCursor( cursors );

    	cursor.moveToFirst();
    	assertDataCursor1();
    	cursor.moveToNext();
    	assertDataCursor2();
    }
    
    private void assertDataCursor1() throws Exception {
        assertThat( cursor.getInt(0), equalTo(1234) );
        assertThat( cursor.getString(1), equalTo("Chuck") );
        assertThat( cursor.getInt(2), equalTo(3463) );
        assertThat( cursor.getFloat(3), equalTo(1.5f) );
        assertThat( cursor.getDouble(4), equalTo(3.14159) );
    	
    	cursor.moveToNext();
        assertThat( cursor.getInt(0), equalTo(1235) );
        assertThat( cursor.getString(1), equalTo("Julie") );
    	
    	cursor.moveToNext();
        assertThat( cursor.getInt(0), equalTo(1236) );
        assertThat( cursor.getString(1), equalTo("Chris") );
    }
    
    private void assertDataCursor2() throws Exception {
        assertThat( cursor.getInt(0), equalTo(4321) );
        assertThat( cursor.getString(1), equalTo("Mary") );
        assertThat( cursor.getInt(2), equalTo(3245) );
        assertThat( cursor.getFloat(3), equalTo(5.4f) );
        assertThat( cursor.getDouble(4), equalTo(2.7818) );
    	
    	cursor.moveToNext();
        assertThat( cursor.getInt(0), equalTo(4322) );
        assertThat( cursor.getString(1), equalTo("Elizabeth") );
    	
    	cursor.moveToNext();
        assertThat( cursor.getInt(0), equalTo(4323) );
        assertThat( cursor.getString(1), equalTo("Chester") );
    }

    @Test
    public void testColumnNamesSingleCursor() throws Exception {
    	Cursor[] cursors = new Cursor[1];
    	cursors[0] = dbCursor1;
    	cursor = new MergeCursor( cursors );
    	
    	for ( int i = 0; i < TABLE_1_INSERTS.length; i++ ) {
    		cursor.moveToPosition(i);
        	String[] columnNames = cursor.getColumnNames();
        	assertColumnNamesCursor1(columnNames);
    	}
    }

    @Test
    public void testColumnNamesMultipleCursors() throws Exception {
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = dbCursor1;
    	cursors[1] = dbCursor2;
    	cursor = new MergeCursor( cursors );

    	for ( int i = 0; i < TABLE_1_INSERTS.length; i++ ) {
    		cursor.moveToPosition(i);
        	String[] columnNames = cursor.getColumnNames();
        	assertColumnNamesCursor1(columnNames);
    	}
    	
    	for ( int i = 0; i < TABLE_2_INSERTS.length; i++ ) {
    		cursor.moveToPosition(i + TABLE_1_INSERTS.length);
        	String[] columnNames = cursor.getColumnNames();
        	assertColumnNamesCursor2(columnNames);
    	}
    }
    
	private void assertColumnNamesCursor1(String[] columnNames) {
		assertThat(columnNames.length, equalTo(7));
    	assertThat(columnNames[0], equalTo("id"));
        assertThat(columnNames[1], equalTo("name_1"));
        assertThat(columnNames[2], equalTo("value_1"));
        assertThat(columnNames[3], equalTo("float_value_1"));
        assertThat(columnNames[4], equalTo("double_value_1"));
        assertThat(columnNames[5], equalTo("blob_value_1"));
        assertThat(columnNames[6], equalTo("clob_value_1"));
	}
	
	private void assertColumnNamesCursor2(String[] columnNames) {
		assertThat(columnNames.length, equalTo(7));
    	assertThat(columnNames[0], equalTo("id"));
        assertThat(columnNames[1], equalTo("name_2"));
        assertThat(columnNames[2], equalTo("value_2"));
        assertThat(columnNames[3], equalTo("float_value_2"));
        assertThat(columnNames[4], equalTo("double_value_2"));
        assertThat(columnNames[5], equalTo("blob_value_2"));
        assertThat(columnNames[6], equalTo("clob_value_2"));
	}
    
    @Test
    public void testCloseCursors() throws Exception {
    	Cursor[] cursors = new Cursor[2];
    	cursors[0] = dbCursor1;
    	cursors[1] = dbCursor2;
    	cursor = new MergeCursor( cursors );

    	assertThat( cursor.isClosed(), equalTo(false) );
    	assertThat( dbCursor1.isClosed(), equalTo(false) );
    	assertThat( dbCursor2.isClosed(), equalTo(false) );
    	
    	cursor.close();
    	
    	assertThat( cursor.isClosed(), equalTo(true) );
    	assertThat( dbCursor1.isClosed(), equalTo(true) );
    	assertThat( dbCursor2.isClosed(), equalTo(true) );
    }

}
