package com.xtremelabs.robolectric.shadows;


import android.database.sqlite.SQLiteQueryBuilder;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteQueryBuilderTest {

    SQLiteQueryBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = new SQLiteQueryBuilder();
    }

    @Test
    public void testDistinct() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                true,
                "table_name",
                new String[]{"id", "name"},
                null, null, null, null, null);
        assertThat(sql, equalTo("SELECT DISTINCT id, name FROM table_name"));

    }

    @Test
    public void testSelectColumn() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"id"},
                null, null, null, null, null);
        assertThat(sql, equalTo("SELECT id FROM table_name"));
    }

    @Test
    public void testSelectColumns() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"id", "name"},
                null, null, null, null, null);
        assertThat(sql, equalTo("SELECT id, name FROM table_name"));
    }

    @Test
    public void testSelectAllColumns() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                null, null, null, null, null, null);
        assertThat(sql, equalTo("SELECT * FROM table_name"));
    }

    @Test
    public void testWhereClause() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", null, null, null, null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck')"));
    }

    @Test
    public void testEmptyWhereClause() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                null, "person", null, null, null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name GROUP BY person"));
    }

    @Test
    public void testGroupBy() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", null, null, null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person"));
    }

    @Test
    public void testEmptyGroupBy() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", null, "SUM(hours) < 20", null, null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') HAVING SUM(hours) < 20"));
    }

    @Test
    public void testHaving() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", "SUM(hours) < 20", null, null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person HAVING SUM(hours) < 20"));
    }

    @Test
    public void testEmptyHaving() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", null, "id ASC", null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person ORDER BY id ASC"));
    }

    @Test
    public void testSortOrder() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", "SUM(hours) < 20", "id ASC", null);
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person HAVING SUM(hours) < 20 ORDER BY id ASC"));
    }

    @Test
    public void testEmptySortOrder() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", "SUM(hours) < 20", null, "10");
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person HAVING SUM(hours) < 20 LIMIT 10"));
    }

    @Test
    public void testLimit() {
        String sql = SQLiteQueryBuilder.buildQueryString(
                false,
                "table_name",
                new String[]{"person", "department", "division"},
                "(id = 2 AND name = 'Chuck')", "person", "SUM(hours) < 20", "id ASC", "10");
        assertThat(sql, equalTo("SELECT person, department, division FROM table_name WHERE (id = 2 AND name = 'Chuck') GROUP BY person HAVING SUM(hours) < 20 ORDER BY id ASC LIMIT 10"));
    }
}
