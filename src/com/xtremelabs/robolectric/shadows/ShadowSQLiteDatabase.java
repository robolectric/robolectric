package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(SQLiteDatabase.class)
public class ShadowSQLiteDatabase {
    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        return newInstanceOf(SQLiteDatabase.class);
    }

    Map<String, Table> tables = new HashMap<String, Table>();

    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
        Table theTable = getTable(table);
        theTable.insert(values);
        return -1;
    }

    @Implementation
    public Cursor query(final String table, final String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        final Table theTable = getTable(table);
        return new SQLiteCursor(null, null, null, null) {
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
            
            @Override
            public int getInt(int columnIndex) {
                return (int) (Integer) get(columnIndex);
            }
            
            @Override
            public long getLong(int columnIndex) {
            	return (long) (Long) get(columnIndex);
            }
            
            private Object get(int columnIndex) {
                return theTable.rows.get(getPosition()).get(columns[columnIndex]);
            }
        };
    }
    
    @Implementation
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
    	 Table theTable = getTable(table);
         return theTable.update(values, whereClause);
    }
    
    @Implementation
    public int delete(String table, String whereClause, String[] whereArgs) {
    	 Table theTable = getTable(table);
    	 return theTable.delete(whereClause);
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
        
        public int update(ContentValues values, String whereClause) {
        	String columnName = whereColumn(whereClause);
        	String value = whereValue(whereClause);
              	
        	int affectedCount = 0;
        	
        	for (ContentValues v : rows) {
        		if (columnName.isEmpty() || (value.equals(v.getAsString(columnName)))) {
        			v.putAll(values);
        			affectedCount++;
        		}
        	}
        	
        	return affectedCount;
        }
        
        public int delete(String whereClause) {
        	String columnName = whereColumn(whereClause);
        	String value = whereValue(whereClause);
        	
        	List<ContentValues> deleted = new ArrayList<ContentValues>();
        	for (ContentValues v : rows) {
        		if ("1".equals(whereClause) || (value.equals(v.getAsString(columnName)))) {
        			deleted.add( v );
        		}
        	}
        	rows.removeAll(deleted);
						
			return deleted.size();
        }
        
        // Parse whereClause of form "<column>=<value>".
        // Handles special cases specified by Android APIs.

        private String whereColumn(String whereClause) {
        	if (isEmptyOrWhitespace(whereClause)) {
        		return "";
        	}
        	if (isSpecialCaseOrUnknown(whereClause)) {
        		return whereClause;
        	}
        	return whereClause.substring(0, whereClause.indexOf("="));
        }
        
        private String whereValue(String whereClause) {
        	if (isEmptyOrWhitespace(whereClause)) {
        		return "";
        	}
        	if (isSpecialCaseOrUnknown(whereClause)) {
        		return whereClause;
        	}
        	return whereClause.substring(whereClause.indexOf("=") + 1, whereClause.length());
        }
        
        private boolean isEmptyOrWhitespace(String s) {
            return (s == null) || (s.trim().isEmpty());
        }
        
        private boolean isSpecialCaseOrUnknown(String s) {
        	return "1".equals(s) || !s.contains("=");
        }
    }
}
