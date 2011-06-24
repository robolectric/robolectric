package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import java.sql.*;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.SQLite.*;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 * Implemented as a wrapper around an embedded SQL database, accessed via JDBC.  The JDBC connection is
 * made available to test cases for use in fixture setup and assertions.
 */
@Implements(SQLiteDatabase.class)
public class ShadowSQLiteDatabase extends ShadowSQLiteClosable {
	@RealObject	SQLiteDatabase realSQLiteDatabase;
    private static Connection connection;
    private final ReentrantLock mLock = new ReentrantLock(true);
    private boolean mLockingEnabled = true;
    private WeakHashMap<SQLiteClosable, Object> mPrograms;
    
    @Implementation
    public void setLockingEnabled(boolean lockingEnabled) {
        mLockingEnabled = lockingEnabled;
    }
    
    public void lock() {
        if (!mLockingEnabled) return;
        mLock.lock();
    }
    
    public void unlock() {
        if (!mLockingEnabled) return;
        mLock.unlock();
    }
    
    
    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        try {
     			Class.forName("org.sqlite.JDBC");
     			connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        } catch (Exception e) {
            throw new RuntimeException("SQL exception in openDatabase", e);
        }
        return newInstanceOf(SQLiteDatabase.class);
    }

    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
        SQLStringAndBindings sqlInsertString = buildInsertString(table, values);
        try {
            PreparedStatement statement = connection.prepareStatement(sqlInsertString.sql, Statement.RETURN_GENERATED_KEYS);
            Iterator<Object> columns = sqlInsertString.columnValues.iterator();
            int i = 1;
            while (columns.hasNext()) {
                statement.setObject(i++, columns.next());
            }

            statement.executeUpdate();

            ResultSet resultSet = statement.getGeneratedKeys();

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in insert", e);
        }
        return -1;
    }

    @Implementation
    public long insertWithOnConflict(String table, String nullColumnHack,
            ContentValues initialValues, int conflictAlgorithm) {
        long result = insert(table, nullColumnHack, initialValues);
        if (conflictAlgorithm == SQLiteDatabase.CONFLICT_IGNORE) {
            try {
                final Statement statement = connection.createStatement();
                final ResultSet resultSet = statement.executeQuery("SELECT IDENTITY();");
                if (resultSet.isBeforeFirst()) {
                    resultSet.first();
                }
                result = resultSet.getInt(1);
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return result;
    }

    @Implementation
    public Cursor query(boolean distinct, String table, String[] columns,
                        String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {

        String where = selection;
        if (selection != null && selectionArgs != null) {
            where = buildWhereClause(selection, selectionArgs);
        }

        String sql = SQLiteQueryBuilder.buildQueryString(distinct, table,
                columns, where, groupBy, having, orderBy, limit);

        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in query", e);
        }

        SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet,sql, connection);
        return cursor;
    }

    @Implementation
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    @Implementation
    public Cursor query(String table, String[] columns, String selection,
                        String[] selectionArgs, String groupBy, String having,
                        String orderBy, String limit) {
        return query(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Implementation
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        SQLStringAndBindings sqlUpdateString = buildUpdateString(table, values, whereClause, whereArgs);

        try {
            PreparedStatement statement = connection.prepareStatement(sqlUpdateString.sql);
            Iterator<Object> columns = sqlUpdateString.columnValues.iterator();
            int i = 1;
            while (columns.hasNext()) {
                statement.setObject(i++, columns.next());
            }

            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in update", e);
        }
    }

    @Implementation
    public int delete(String table, String whereClause, String[] whereArgs) {
        String sql = buildDeleteString(table, whereClause, whereArgs);

        try {
            return connection.prepareStatement(sql).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in delete", e);
        }
    }

    @Implementation
    public void execSQL(String sql) throws android.database.SQLException {
        if (!isOpen()) {
            throw new IllegalStateException("database not open");
        }

        try {
            connection.createStatement().execute(sql);
        } catch (java.sql.SQLException e) {
            android.database.SQLException ase = new android.database.SQLException();
            ase.initCause(e);
            throw ase;
        }
    }

    @Implementation
    public Cursor rawQuery (String sql, String[] selectionArgs){
        String sqlBody = sql;
        if (sql != null && selectionArgs != null) {
        	sqlBody = buildWhereClause(sql, selectionArgs);
        }
        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sqlBody);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in query", e);
        }

        SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet, sql, connection);
        return cursor;
    }
    
    @Implementation
    public boolean isOpen() {
        return (connection != null);
    }

    @Implementation
    public void close() {
        if (!isOpen()) {
            return;
        }
        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in close", e);
        }
    }

    /**
     * Allows test cases access to the underlying JDBC connection, for use in
     * setup or assertions.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }
    
    @Implementation
    public SQLiteStatement compileStatement(String sql) throws SQLException {
        lock();
        
        try {
        	SQLiteStatement stmt = Robolectric.newInstanceOf(SQLiteStatement.class);
        	Robolectric.shadowOf(stmt).init(realSQLiteDatabase, sql);
            return stmt;
        } catch (Exception e){
        	throw new RuntimeException(e);
        } finally {
            unlock();
        }
    }
    
    @Implementation
	@Override
	public void onAllReferencesReleased() {
		close();
	}
	   /**
     * @param closable
     */
    void addSQLiteClosable(SQLiteClosable closable) {
        lock();
        try {
            mPrograms.put(closable, null);
        } finally {
            unlock();
        }
    }

    void removeSQLiteClosable(SQLiteClosable closable) {
        lock();
        try {
            mPrograms.remove(closable);
        } finally {
            unlock();
        }
    }
	
}
