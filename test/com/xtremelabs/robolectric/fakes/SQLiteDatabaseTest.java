package com.xtremelabs.robolectric.fakes;

import android.content.ContentValues;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class SQLiteDatabaseTest {
    private SQLiteDatabase database;

    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(SQLiteDatabase.class, FakeSQLiteDatabase.class);
        DogfoodRobolectricTestRunner.addProxy(ContentValues.class, FakeContentValues.class);
        DogfoodRobolectricTestRunner.addProxy(AbstractCursor.class, FakeAbstractCursor.class);

        database = SQLiteDatabase.openDatabase("path", null, 0);
    }

    @Test
    public void testInsertAndQuery() throws Exception {

        String stringColumnValue = "column_value";
        byte[] byteColumnValue = new byte[] {1,2,3};

        ContentValues values = new ContentValues();

        values.put("first_column", stringColumnValue);
        values.put("second_column", byteColumnValue);

        database.insert("table_name", null, values);

        Cursor cursor = database.query("table_name", new String[] {"second_column", "first_column"}, null, null, null, null, null);

        assertThat(cursor.moveToFirst(), equalTo(true));

        byte[] byteValueFromDatabase = cursor.getBlob(0);
        String stringValueFromDatabase = cursor.getString(1);

        assertThat(stringValueFromDatabase, equalTo(stringColumnValue));
        assertThat(byteValueFromDatabase, equalTo(byteColumnValue));
    }

    @Test
    public void testEmptyTable() throws Exception {
        Cursor cursor = database.query("table_name", new String[] {"second_column", "first_column"}, null, null, null, null, null);

        assertThat(cursor.moveToFirst(), equalTo(false));
    }
}
