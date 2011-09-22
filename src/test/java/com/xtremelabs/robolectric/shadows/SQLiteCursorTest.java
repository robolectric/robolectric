package com.xtremelabs.robolectric.shadows;


import android.database.sqlite.SQLiteCursor;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteCursorTest {

    private Connection connection;
    private ResultSet resultSet;
    private SQLiteCursor cursor;

    @Before
    public void setUp() throws Exception {
    	connection = DatabaseConfig.getMemoryConnection();

        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE table_name(" +
                "id INTEGER PRIMARY KEY, name VARCHAR(255), long_value BIGINT," +
                "float_value REAL, double_value DOUBLE, blob_value BINARY, clob_value CLOB );");

        addPeople();
        setupCursor();
    }

    @After
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void testGetColumnNames() throws Exception {
        String[] columnNames = cursor.getColumnNames();

        assertColumnNames(columnNames);
    }

    @Test
    public void testGetColumnNamesEmpty() throws Exception {
        setupEmptyResult();
        String[] columnNames = cursor.getColumnNames();

        // Column names are present even with an empty result.
        assertThat(columnNames, notNullValue());
        assertColumnNames(columnNames);
    }

    @Test
    public void testGetColumnIndex() throws Exception {
        assertThat(cursor.getColumnIndex("id"), equalTo(0));
        assertThat(cursor.getColumnIndex("name"), equalTo(1));
    }

    @Test
    public void testGetColumnIndexNotFound() throws Exception {
        assertThat(cursor.getColumnIndex("Fred"), equalTo(-1));
    }

    @Test
    public void testGetColumnIndexEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.getColumnIndex("id"), equalTo(0));
        assertThat(cursor.getColumnIndex("name"), equalTo(1));
    }

    @Test
    public void testGetColumnIndexOrThrow() throws Exception {
        assertThat(cursor.getColumnIndexOrThrow("id"), equalTo(0));
        assertThat(cursor.getColumnIndexOrThrow("name"), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexOrThrowNotFound() throws Exception {
        cursor.getColumnIndexOrThrow("Fred");
    }

    @Test
    public void testGetColumnIndexOrThrowEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.getColumnIndexOrThrow("name"), equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColumnIndexOrThrowNotFoundEmpty() throws Exception {
        setupEmptyResult();

        cursor.getColumnIndexOrThrow("Fred");
    }

    @Test
    public void testMoveToFirst() throws Exception {
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getInt(0), equalTo(1234));
        assertThat(cursor.getString(1), equalTo("Chuck"));
    }

    @Test
    public void testMoveToFirstEmpty() throws Exception {
        setupEmptyResult();

        assertThat(cursor.moveToFirst(), equalTo(false));
    }

    @Test
    public void testMoveToNext() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getInt(0), equalTo(1235));
        assertThat(cursor.getString(1), equalTo("Julie"));
    }

    @Test
    public void testMoveToNextPastEnd() throws Exception {
        cursor.moveToFirst();

        cursor.moveToNext();
        cursor.moveToNext();
        cursor.moveToNext();

        assertThat(cursor.moveToNext(), equalTo(false));
    }
    
    @Test
    public void testMoveBackwards() throws Exception {
    	assertThat(cursor.getPosition(), equalTo(-1));
    	
        cursor.moveToFirst();
        assertThat(cursor.getPosition(), equalTo(0));
        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(1));
        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(2));
        
        cursor.moveToFirst();
        assertThat(cursor.getPosition(), equalTo(0));
        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(1));
        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(2));
        
        cursor.moveToPosition(1);
        assertThat(cursor.getPosition(), equalTo(1));
    }

    @Test
    public void testMoveToNextEmpty() throws Exception {
        setupEmptyResult();

        cursor.moveToFirst();
        assertThat(cursor.moveToNext(), equalTo(false));
    }
    
    @Test
    public void testMoveToPrevious() throws Exception {
    	cursor.moveToFirst();
    	cursor.moveToNext();
    	
    	assertThat(cursor.moveToPrevious(), equalTo(true));
        assertThat(cursor.getInt(0), equalTo(1234));
        assertThat(cursor.getString(1), equalTo("Chuck"));
    }
    
    @Test
    public void testMoveToPreviousPastStart() throws Exception {
    	cursor.moveToFirst();
    	
    	// Possible to move cursor before the first item
    	assertThat(cursor.moveToPrevious(), equalTo(true));
    	// After that, attempts to move cursor back return false
    	assertThat(cursor.moveToPrevious(), equalTo(false));
    }
    
    @Test
    public void testMoveToPreviousEmpty() throws Exception {
        setupEmptyResult();
    	cursor.moveToFirst();
    	
    	assertThat(cursor.moveToPrevious(), equalTo(false));
    }

    @Test
    public void testGetPosition() throws Exception {
        cursor.moveToFirst();
        assertThat(cursor.getPosition(), equalTo(0));

        cursor.moveToNext();
        assertThat(cursor.getPosition(), equalTo(1));
    }

    @Test
    public void testGetBlob() throws Exception {
        String sql = "UPDATE table_name set blob_value=? where id=1234";
        byte[] byteData = sql.getBytes();

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, byteData);
        statement.executeUpdate();

        setupCursor();
        cursor.moveToFirst();

        byte[] retrievedByteData = cursor.getBlob(5);
        assertThat(byteData.length, equalTo(retrievedByteData.length));

        for (int i = 0; i < byteData.length; i++) {
            assertThat(byteData[i], equalTo(retrievedByteData[i]));
        }
    }

    @Test
    public void testGetClob() throws Exception {
        String sql = "UPDATE table_name set clob_value=? where id=1234";
        String s = "Don't CLOBber my data, please. Thank you.";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setObject(1, s);
        statement.executeUpdate();

        setupCursor();
        cursor.moveToFirst();

        String actual = cursor.getString(6);
        assertThat(s, equalTo(actual));
    }

    @Test
    public void testGetString() throws Exception {
        cursor.moveToFirst();

        String[] data = {"Chuck", "Julie", "Chris"};

        for (String aData : data) {
            assertThat(cursor.getString(1), equalTo(aData));
            cursor.moveToNext();
        }
    }

    @Test
    public void testGetInt() throws Exception {
        cursor.moveToFirst();

        int[] data = {1234, 1235, 1236};

        for (int aData : data) {
            assertThat(cursor.getInt(0), equalTo(aData));
            cursor.moveToNext();
        }
    }

    @Test
    public void testGetLong() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getLong(2), equalTo(3463L));
    }

    @Test
    public void testGetFloat() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getFloat(3), equalTo((float) 1.5));
    }

    @Test
    public void testGetDouble() throws Exception {
        cursor.moveToFirst();

        assertThat(cursor.getDouble(4), equalTo(3.14159));
    }

    @Test
    public void testClose() throws Exception {
        assertThat(cursor.isClosed(), equalTo(false));
        cursor.close();
        assertThat(cursor.isClosed(), equalTo(true));
    }

    @Test
    public void testIsNullWhenNull() throws Exception {
        cursor.moveToFirst();
        assertThat(cursor.moveToNext(), equalTo(true));

        assertThat(cursor.isNull(cursor.getColumnIndex("id")), equalTo(false));
        assertThat(cursor.isNull(cursor.getColumnIndex("name")), equalTo(false));

        assertThat(cursor.isNull(cursor.getColumnIndex("long_value")), equalTo(true));
        assertThat(cursor.isNull(cursor.getColumnIndex("float_value")), equalTo(true));
        assertThat(cursor.isNull(cursor.getColumnIndex("double_value")), equalTo(true));
    }

    @Test
    public void testIsNullWhenNotNull() throws Exception {
        cursor.moveToFirst();

        for (int i = 0; i < 5; i++) {
            assertThat(cursor.isNull(i), equalTo(false));
        }
    }

    @Test
    public void testIsNullWhenIndexOutOfBounds() throws Exception {
        cursor.moveToFirst();

        // column index 5 is out-of-bounds
        assertThat(cursor.isNull(5), equalTo(true));
    }

    private void addPeople() throws Exception {
        String[] inserts = {
                "INSERT INTO table_name (id, name, long_value, float_value, double_value) VALUES(1234, 'Chuck', 3463, 1.5, 3.14159);",
                "INSERT INTO table_name (id, name) VALUES(1235, 'Julie');",
                "INSERT INTO table_name (id, name) VALUES(1236, 'Chris');"
        };

        for (String insert : inserts) {
            connection.createStatement().executeUpdate(insert);
        }
    }

    private void setupCursor() throws Exception {
        Statement statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
        String sql ="SELECT * FROM table_name;";
        resultSet = statement.executeQuery("SELECT * FROM table_name;");
        cursor = new SQLiteCursor(null, null, null, null);
        Robolectric.shadowOf(cursor).setResultSet(resultSet, sql);
    }

    private void setupEmptyResult() throws Exception {
        Statement statement = connection.createStatement();
        statement.executeUpdate("DELETE FROM table_name;");

        setupCursor();
    }

    private void assertColumnNames(String[] columnNames) {
        assertThat(columnNames.length, equalTo(7));
        assertThat(columnNames[0], equalTo("id"));
        assertThat(columnNames[1], equalTo("name"));
        assertThat(columnNames[2], equalTo("long_value"));
        assertThat(columnNames[3], equalTo("float_value"));
        assertThat(columnNames[4], equalTo("double_value"));
        assertThat(columnNames[5], equalTo("blob_value"));
        assertThat(columnNames[6], equalTo("clob_value"));
    }

}
