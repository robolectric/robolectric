package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLiteMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
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

        Statement statement = shadowOf(database).getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT name FROM table_name where id = "+id);

        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getString(1), equalTo("Norris"));
    }
}
