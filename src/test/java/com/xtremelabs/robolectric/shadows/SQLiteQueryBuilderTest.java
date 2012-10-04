package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.sqlite.SQLiteQueryBuilder;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteQueryBuilderTest {

  SQLiteQueryBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new SQLiteQueryBuilder();
  }

  @Test
  public void testDistinct() {
    String s = "SELECT DISTINCT id, name FROM table_name";
    
    String tables = "table_name";
    String[] columns = new String[] { "id", "name" };
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(true, tables, columns, null, null, null, null, null);
    assertThat(sql1, equalTo(s));
    
    builder.setTables(tables);
    builder.setDistinct(true);
    String sql2 = builder.buildQuery(columns, null, null, null, null, null);
    assertThat(sql2, equalTo(s));
  }

  @Test
  public void testSelectColumn() {
    String s = "SELECT id FROM table_name";

    String tables = "table_name";
    String[] columns = new String[] { "id" };
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, null, null, null, null, null);
    assertThat(sql1, equalTo(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, null, null, null, null, null);
    assertThat(sql2, equalTo(s));
  }

  @Test
  public void testSelectColumns() {
    String s = "SELECT id, name FROM table_name";

    String tables = "table_name";
    String[] columns = new String[] { "id", "name" };
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, null, null, null, null, null);
    assertThat(sql1, equalTo(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, null, null, null, null, null);
    assertThat(sql2, equalTo(s));
  }

  @Test
  public void testSelectAllColumns() {
    String s = "SELECT * FROM table_name";    

    String tables = "table_name";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, null, null, null, null, null, null);
    assertThat(sql1, equalTo(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(null, null, null, null, null, null);
    assertThat(sql2, equalTo(s));
  }

  @Test
  public void testWhereClause() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\)";    

    String tables = "table_name";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", null, null, null, null);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, null, null, null, null);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));

    builder.appendWhere(where);
    String sql3 = builder.buildQuery(columns, null, null, null, null, null);
    assertTrue("'" + sql3 + "' doesn't match '" + s + "'", sql3.matches(s));
  }

  @Test
  public void testEmptyWhereClause() {
    String s = "SELECT person, department, division FROM table_name GROUP BY person";    

    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, null, groupBy, null, null, null);
    assertThat(sql1, equalTo(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, null, groupBy, null, null, null);
    assertThat(sql2, equalTo(s));
  }

  @Test
  public void testGroupBy() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person";    

    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };    
    String where = "id = 2 AND name = 'Chuck'";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, null, null, null);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, null, null, null);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }

  @Test
  public void testHavingWithEmptyGroupByBuildQueryString() {
    String tables = "table_name";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String having = "SUM(hours) < 20";
    
    try {
      SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", null, having, null, null);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }

    try {
      builder.setTables(tables);
      builder.buildQuery(columns, where, null, having, null, null);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testHaving() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person HAVING SUM\\(hours\\) < 20";
    
    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String having = "SUM(hours) < 20";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, having, null, null);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, having, null, null);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }

  @Test
  public void testEmptyHaving() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person ORDER BY id ASC";

    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String orderBy = "id ASC";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, null, orderBy, null);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, null, orderBy, null);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }

  @Test
  public void testSortOrder() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person HAVING SUM\\(hours\\) < 20 ORDER BY id ASC";

    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String having = "SUM(hours) < 20";
    String orderBy = "id ASC";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, having, orderBy, null);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));

    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, having, orderBy, null);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }

  @Test
  public void testEmptySortOrder() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person HAVING SUM\\(hours\\) < 20 LIMIT 10";
    
    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String having = "SUM(hours) < 20";
    String limit = "10";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, having, null, limit);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'", sql1.matches(s));
    
    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, having, null, limit);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }

  @Test
  public void testLimit() {
    String s = "SELECT person, department, division FROM table_name WHERE \\(id = 2 AND name = 'Chuck'\\s?\\) GROUP BY person HAVING SUM\\(hours\\) < 20 ORDER BY id ASC LIMIT 10";
    
    String tables = "table_name";
    String groupBy = "person";
    String[] columns = new String[] { "person", "department", "division" };
    String where = "id = 2 AND name = 'Chuck'";
    String having = "SUM(hours) < 20";
    String orderBy = "id ASC";
    String limit = "10";
    
    String sql1 = SQLiteQueryBuilder.buildQueryString(false, tables, columns, "(" + where + " )", groupBy, having, orderBy, limit);
    assertTrue("'" + sql1 + "' doesn't match '" + s + "'",sql1.matches(s));
    
    builder.setTables(tables);
    String sql2 = builder.buildQuery(columns, where, groupBy, having, orderBy, limit);
    assertTrue("'" + sql2 + "' doesn't match '" + s + "'", sql2.matches(s));
  }
  
  @Test
  public void testProjectionMap() {
    String s = "SELECT person, department, division FROM table_name";
    
    String tables = "table_name";
    String[] columns = new String[] { "pn", "dt", "dn" };
    Map<String, String> map = new HashMap<String, String>(3);
    map.put("pn", "person");
    map.put("dt", "department");
    map.put("dn", "division");
    
    builder.setTables(tables);
    builder.setProjectionMap(map);
    String sql = builder.buildQuery(columns, null, null, null, null, null);
    assertThat(sql, equalTo(s));
  }

}
