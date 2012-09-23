package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLiteMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@DatabaseConfig.UsingDatabaseMap(SQLiteMap.class)
@RunWith(WithTestDefaultsRunner.class)
public class SQLiteDatabaseTest extends DatabaseTestBase {

    @Test
    public void shouldUseSQLiteDatabaseMap() throws Exception {
        assertThat(DatabaseConfig.getDatabaseMap().getClass().getName(),
                equalTo(SQLiteMap.class.getName()));
    }
    
    @Test
    public void testReplace() throws Exception {
        long id = addChuck();
        assertThat(id, not(equalTo(-1L)));

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", "Norris");

        long replaceId = database.replace("table_name", null, values);
        assertThat(replaceId, equalTo(id));

        String query = "SELECT name FROM table_name where id = " + id;
        ResultSet resultSet = executeQuery(query);

        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getString(1), equalTo("Norris"));
    }

    @Test
    public void testReplaceIsReplacing() throws SQLException {
        final String query = "SELECT first_column FROM table_name WHERE id = ";
        String stringValueA = "column_valueA";
        String stringValueB = "column_valueB";
        long id = 1;

        ContentValues valuesA = new ContentValues();
        valuesA.put("id", id);
        valuesA.put("first_column", stringValueA);

        ContentValues valuesB = new ContentValues();
        valuesB.put("id", id);
        valuesB.put("first_column", stringValueB);

        long firstId = database.replaceOrThrow("table_name", null, valuesA);
        ResultSet firstResultSet = executeQuery(query + firstId);
        firstResultSet.next();
        long secondId = database.replaceOrThrow("table_name", null, valuesB);
        ResultSet secondResultSet = executeQuery(query + secondId);
        secondResultSet.next();

        assertThat(firstId, equalTo(id));
        assertThat(secondId, equalTo(id));
        assertThat(firstResultSet.getString(1), equalTo(stringValueA));
        assertThat(secondResultSet.getString(1), equalTo(stringValueB));
    }

    private ResultSet executeQuery(String query) throws SQLException {
        Statement statement = shadowOf(database).getConnection().createStatement();
        return statement.executeQuery(query);
    }
}
