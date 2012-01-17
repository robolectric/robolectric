package com.xtremelabs.robolectric.util;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class H2MapTest {

    H2Map map;

    @Before
    public void setUp() throws Exception {
        map = new H2Map();
    }

    @Test
    public void testDriverClassName() {
        assertThat(map.getDriverClassName(), equalTo("org.h2.Driver"));
    }

    @Test
    public void testConnectionString() {
        assertThat(map.getConnectionString(), equalTo("jdbc:h2:mem:"));
    }

    @Test
    public void testScrubSQLReplacesAutoIncrement() throws SQLException {
        assertThat(map.getScrubSQL("autoincrement"), equalTo("auto_increment"));
    }

    @Test
    public void testScrubSQLReplacesIntegerWithBigInt() throws SQLException {
        assertThat(map.getScrubSQL("integer"), equalTo("bigint(19)"));
    }

    @Test
    public void testScrubSQLAcceptsIntegerPrimaryKey() throws SQLException {
        map.getScrubSQL("INTEGER PRIMARY KEY AUTOINCREMENT");
    }

    @Test(expected = SQLException.class)
    public void testScrubSQLRejectsIntPrimaryKeyThrowsException() throws SQLException {
        map.getScrubSQL("INT PRIMARY KEY AUTOINCREMENT");
    }

    @Test(expected = SQLException.class)
    public void testScrubSQLRejectsCharPrimaryKeyThrowsException2() throws SQLException {
        map.getScrubSQL("CHAR PRIMARY KEY AUTOINCREMENT");
    }

    @Test
    public void testGetSelectLastInsertIdentity() throws SQLException {
        assertThat(map.getSelectLastInsertIdentity(), equalTo("SELECT IDENTITY();"));
    }

    @Test
    public void testGetH2ResultSetIs_TYPE_SCROLL_INSENSITIVE() throws SQLException {
        assertThat(map.getResultSetType(), equalTo(ResultSet.TYPE_SCROLL_INSENSITIVE));
    }

    @Test
    public void scrubSQL_shouldRemoveConflictAlgorithms() throws Exception {
        assertThat(map.getScrubSQL("INSERT INTO "), equalTo("INSERT INTO "));
        assertThat(map.getScrubSQL("INSERT OR ROLLBACK INTO "), equalTo("INSERT INTO "));
        assertThat(map.getScrubSQL("INSERT OR ABORT INTO "), equalTo("INSERT INTO "));
        assertThat(map.getScrubSQL("INSERT OR FAIL INTO "), equalTo("INSERT INTO "));
        assertThat(map.getScrubSQL("INSERT OR IGNORE INTO "), equalTo("INSERT INTO "));
        assertThat(map.getScrubSQL("INSERT OR REPLACE INTO "), equalTo("INSERT INTO "));
    }
}
