package com.xtremelabs.robolectric.util;


import android.content.ContentValues;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.SQLite.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.xtremelabs.robolectric.util.SQLite.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteTest {

    ContentValues values;

    @Before
    public void setUp() throws Exception {
        String byteString = "byte_string";
        byte[] byteData = byteString.getBytes();

        values = new ContentValues();
        values.put("name", "Chuck");
        values.put("int_value", 33);
        values.put("float_value", (float) 1.5);
        values.put("byte_data", byteData);
    }

    @Test
    public void testBuildInsertString() {
        SQLStringAndBindings insertString = buildInsertString("table_name", values);
        assertThat(insertString.sql, equalTo("INSERT INTO table_name (float_value, byte_data, name, int_value) VALUES (?, ?, ?, ?);"));
        verifyColumnValues(insertString.columnValues);
    }

    @Test
    public void testBuildUpdateString() {
        SQLStringAndBindings insertString = buildUpdateString("table_name", values, "id=?", new String[]{"1234"});
        assertThat(insertString.sql, equalTo("UPDATE table_name SET float_value=?, byte_data=?, name=?, int_value=? WHERE id='1234';"));
        verifyColumnValues(insertString.columnValues);
    }

    @Test
    public void testBuildDeleteString() {
        String deleteString = buildDeleteString("table_name", "id=?", new String[]{"1234"});
        assertThat(deleteString, equalTo("DELETE FROM table_name WHERE id='1234';"));
    }

    @Test
    public void testBuildWhereClause() {
        String whereClause = buildWhereClause("id=? AND name=? AND int_value=?", new String[]{"1234", "Chuck", "33"});
        assertThat(whereClause, equalTo("id='1234' AND name='Chuck' AND int_value='33'"));
    }

    @Test
    public void testBuildColumnValuesClause() {
        SQLStringAndBindings columnValuesClause = buildColumnValuesClause(values);

        assertThat(columnValuesClause.sql, equalTo("(float_value, byte_data, name, int_value) VALUES (?, ?, ?, ?)"));
        verifyColumnValues(columnValuesClause.columnValues);
    }

    @Test
    public void testBuildColumnAssignmentsClause() {
        SQLStringAndBindings columnAssignmentsClause = buildColumnAssignmentsClause(values);

        assertThat(columnAssignmentsClause.sql, equalTo("float_value=?, byte_data=?, name=?, int_value=?"));
        verifyColumnValues(columnAssignmentsClause.columnValues);
    }

    private void verifyColumnValues(List<Object> colValues) {
        assertThat(colValues.get(0), instanceOf(Float.class));
        assertThat(colValues.get(1), instanceOf(byte[].class));
        assertThat(colValues.get(2), instanceOf(String.class));
        assertThat(colValues.get(3), instanceOf(Integer.class));
    }
}
