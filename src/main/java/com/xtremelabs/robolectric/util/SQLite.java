package com.xtremelabs.robolectric.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * SQL utility methods to support the database-related shadows, such as
 * {@code ShadowSQLiteDatabase} and {@code ShadowSQLiteCursor}.
 */
public class SQLite {
    private static final String[] CONFLICT_VALUES = {"", "OR ROLLBACK ", "OR ABORT ", "OR FAIL ", "OR IGNORE ", "OR REPLACE "};

    /**
     * Create a SQL INSERT string.  Returned values are then bound via
     * JDBC to facilitate various data types.
     *
     * @param table  table name
     * @param values column name/value pairs
     * @param conflictAlgorithm the conflict algorithm to use
     * @return insert string
     */
    public static SQLStringAndBindings buildInsertString(String table, ContentValues values, int conflictAlgorithm) throws SQLException {
        StringBuilder sb = new StringBuilder();

        sb.append("INSERT ");
        sb.append(CONFLICT_VALUES[conflictAlgorithm]);
        sb.append("INTO ");

        sb.append(table);
        sb.append(" ");

        SQLStringAndBindings columnsValueClause = buildColumnValuesClause(values);
        sb.append(columnsValueClause.sql);
        sb.append(";");

        String sql = DatabaseConfig.getScrubSQL(sb.toString());
        return new SQLStringAndBindings(sql, columnsValueClause.columnValues);
    }

    /**
     * Create a SQL UPDATE string.  Returned values are then bound via
     * JDBC to facilitate various data types.
     *
     * @param table       table name
     * @param values      column name/value pairs
     * @param whereClause SQL where clause fragment
     * @param whereArgs   Array of substitutions for args in whereClause
     * @return update string
     */
    public static SQLStringAndBindings buildUpdateString(String table, ContentValues values, String whereClause, String[] whereArgs) {
        StringBuilder sb = new StringBuilder();

        sb.append("UPDATE ");
        sb.append(table);
        sb.append(" SET ");

        SQLStringAndBindings columnAssignmentsClause = buildColumnAssignmentsClause(values);
        sb.append(columnAssignmentsClause.sql);

        if (whereClause != null) {
            String where = whereClause;
            if (whereArgs != null) {
                where = buildWhereClause(whereClause, whereArgs);
            }
            sb.append(" WHERE ");
            sb.append(where);
        }
        sb.append(";");

        return new SQLStringAndBindings(sb.toString(), columnAssignmentsClause.columnValues);
    }

    /**
     * Create a SQL DELETE string.
     *
     * @param table       table name
     * @param whereClause SQL where clause fragment
     * @param whereArgs   Array of substitutions for args in whereClause
     * @return delete string
     */
    public static String buildDeleteString(String table, String whereClause, String[] whereArgs) {
        StringBuilder sb = new StringBuilder();

        sb.append("DELETE FROM ");
        sb.append(table);

        if (whereClause != null) {
            String where = whereClause;
            if (whereArgs != null) {
                where = buildWhereClause(whereClause, whereArgs);
            }
            sb.append(" WHERE ");
            sb.append(where);
        }
        sb.append(";");

        return sb.toString();
    }

    /**
     * Build a WHERE clause used in SELECT, UPDATE and DELETE statements.
     *
     * @param selection     SQL where clause fragment
     * @param selectionArgs Array of substitutions for args in selection
     * @return where clause
     */
    public static String buildWhereClause(String selection, String[] selectionArgs) throws SQLiteException {
        String whereClause = selection;
        int argsNeeded = 0;
        int args = 0;

        for (char c : selection.toCharArray()) {
            if (c == '?') argsNeeded++;
        }
        if (selectionArgs != null) {
            for (int x = 0; x < selectionArgs.length; x++) {
                if (selectionArgs[x] == null) {
                    throw new IllegalArgumentException("the bind value at index " + x + " is null");
                } else {
                    args++;
                }
                whereClause = whereClause.replaceFirst("\\?", "'" + selectionArgs[x] + "'");
            }
        }
        if (argsNeeded != args) {
            throw new SQLiteException("bind or column index out of range: count of selectionArgs does not match count of (?) placeholders for given sql statement!");
        }

        return whereClause;
    }

   /**
     * Build the '(columns...) VALUES (values...)' clause used in INSERT
     * statements.
     *
     * @param values column name/value pairs
     * @return SQLStringAndBindings
     */
    public static SQLStringAndBindings buildColumnValuesClause(ContentValues values) {
        StringBuilder clause = new StringBuilder("(");
        List<Object> columnValues = new ArrayList<Object>(values.size());

        Iterator<Entry<String, Object>> itemEntries = values.valueSet().iterator();
        while (itemEntries.hasNext()) {
            Entry<String, Object> entry = itemEntries.next();
            clause.append(entry.getKey());
            if (itemEntries.hasNext()) {
                clause.append(", ");
            }
            columnValues.add(entry.getValue());
        }

        clause.append(") VALUES (");
        for (int i = 0; i < values.size() - 1; i++) {
            clause.append("?, ");
        }
        clause.append("?)");

        return new SQLStringAndBindings(clause.toString(), columnValues);
    }

    /**
     * Build the '(col1=?, col2=? ... )' clause used in UPDATE statements.
     *
     * @param values column name/value pairs
     * @return SQLStringAndBindings
     */
    public static SQLStringAndBindings buildColumnAssignmentsClause(ContentValues values) {
        StringBuilder clause = new StringBuilder();
        List<Object> columnValues = new ArrayList<Object>(values.size());

        Iterator<Entry<String, Object>> itemsEntries = values.valueSet().iterator();
        while (itemsEntries.hasNext()) {
            Entry<String, Object> entry = itemsEntries.next();
            clause.append(entry.getKey());
            clause.append("=?");
            if (itemsEntries.hasNext()) {
                clause.append(", ");
            }
            columnValues.add(entry.getValue());
        }

        return new SQLStringAndBindings(clause.toString(), columnValues);
    }

    /**
     * Container for a SQL fragment and the objects which are to be
     * bound to the arguments in the fragment.
     */
    public static class SQLStringAndBindings {
        public String sql;
        public List<Object> columnValues;

        public SQLStringAndBindings(String sql, List<Object> columnValues) {
            this.sql = sql;
            this.columnValues = columnValues;
        }
    }

}
