package com.xtremelabs.droidsugar.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;

import static com.xtremelabs.droidsugar.view.FakeHelper.newInstanceOf;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeSQLiteDatabase {

    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        return newInstanceOf(SQLiteDatabase.class);
    }

    Map<String, Table> tables = new HashMap<String, Table>();

    public long insert(String table, String nullColumnHack, ContentValues values) {
        Table theTable = getTable(table);
        theTable.insert(values);
        return -1;
    }

    public Cursor query(final String table, final String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        final Table theTable = getTable(table);
        return new SQLiteCursor(null, null, null, null) {
            private int currentRowNumber = 0;

            @Override
            public int getCount() {
                return theTable.rows.size();
            }

            @Override
            public byte[] getBlob(int columnIndex) {
                return (byte[]) get(columnIndex);
            }

            @Override
            public String getString(int columnIndex) {
                return (String) get(columnIndex);
            }

            private Object get(int columnIndex) {
                return theTable.rows.get(currentRowNumber).get(columns[columnIndex]);
            }
        };
    }

    private Table getTable(String tableName) {
        Table table = tables.get(tableName);
        if (table == null) {
            table = new Table();
            tables.put(tableName, table);
        }
        return table;
    }

    private class Table {
        List<ContentValues> rows = new ArrayList<ContentValues>();

        public void insert(ContentValues values) {
            rows.add(values);
        }
    }
}
