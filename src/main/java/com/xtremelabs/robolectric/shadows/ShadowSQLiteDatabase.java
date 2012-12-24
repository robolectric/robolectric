package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.*;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLite.*;

import java.sql.*;
import java.util.*;
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
public class ShadowSQLiteDatabase {
    @RealObject SQLiteDatabase realSQLiteDatabase;
    private static Connection connection;
    private final ReentrantLock mLock = new ReentrantLock(true);
    private boolean mLockingEnabled = true;
    private WeakHashMap<SQLiteClosable, Object> mPrograms;
    private boolean inTransaction = false;
    private boolean transactionSuccess = false;
    private boolean throwOnInsert;
    private Set<Cursor> cursors = new HashSet<Cursor>();
    private List<String> querySql = new ArrayList<String>();

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

    public void setThrowOnInsert(boolean throwOnInsert) {
        this.throwOnInsert = throwOnInsert;
    }

    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        connection = DatabaseConfig.getMemoryConnection();
        return newInstanceOf(SQLiteDatabase.class);
    }

    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
        try {
            return insertOrThrow(table, nullColumnHack, values);
        } catch (android.database.SQLException e) {
            return -1;
        }
    }

    @Implementation
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) throws android.database.SQLException {
        if (throwOnInsert)
            throw new android.database.SQLException();
        return insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_NONE);
    }

    @Implementation
    public long replace(String table, String nullColumnHack, ContentValues values) {
        try {
            return replaceOrThrow(table, nullColumnHack, values);
        } catch (android.database.SQLException e) {
            return -1;
        }
    }

    @Implementation
    public long replaceOrThrow(String table, String nullColumnHack, ContentValues values) {
        return insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Implementation
    public long insertWithOnConflict(String table, String nullColumnHack,
                                     ContentValues initialValues, int conflictAlgorithm) throws android.database.SQLException {

        try {
            SQLStringAndBindings sqlInsertString = buildInsertString(table, initialValues, conflictAlgorithm);
            PreparedStatement insert = connection.prepareStatement(sqlInsertString.sql, Statement.RETURN_GENERATED_KEYS);
            Iterator<Object> columns = sqlInsertString.columnValues.iterator();
            int i = 1;
            long result = -1;
            while (columns.hasNext()) {
                insert.setObject(i++, columns.next());
            }
            insert.executeUpdate();
            ResultSet resultSet = insert.getGeneratedKeys();
            if (resultSet.next()) {
                result = resultSet.getLong(1);
            }
            resultSet.close();
            return result;
        } catch (SQLException e) {
            throw new android.database.SQLException(e.getLocalizedMessage());
        }
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
        querySql.add(sql);

        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in query", e);
        }

        SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet, sql);
        cursors.add(cursor);
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
            String scrubbedSql = DatabaseConfig.getScrubSQL(sql);
            connection.createStatement().execute(scrubbedSql);
        } catch (java.sql.SQLException e) {
            android.database.SQLException ase = new android.database.SQLException();
            ase.initCause(e);
            throw ase;
        }
    }

    @Implementation
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        if (bindArgs == null) {
            throw new IllegalArgumentException("Empty bindArgs");
        }
        String scrubbedSql = DatabaseConfig.getScrubSQL(sql);


        SQLiteStatement statement = null;
        try {
            statement = compileStatement(scrubbedSql);
            if (bindArgs != null) {
                int numArgs = bindArgs.length;
                for (int i = 0; i < numArgs; i++) {
                    DatabaseUtils.bindObjectToProgram(statement, i + 1, bindArgs[i]);
                }
            }
            statement.execute();
        } catch (SQLiteDatabaseCorruptException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    @Implementation
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return rawQueryWithFactory(new SQLiteDatabase.CursorFactory() {
            @Override
            public Cursor newCursor(SQLiteDatabase db,
                                    SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
                return new SQLiteCursor(db, masterQuery, editTable, query);
            }

        }, sql, selectionArgs, null);
    }

    @Implementation
    public Cursor rawQueryWithFactory(SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
        String sqlBody = sql;
        if (sql != null) {
            sqlBody = buildWhereClause(sql, selectionArgs);
        }

        ResultSet resultSet;
        try {
            SQLiteStatement stmt = compileStatement(sql);

            int numArgs = selectionArgs == null ? 0
                    : selectionArgs.length;
            for (int i = 0; i < numArgs; i++) {
                stmt.bindString(i + 1, selectionArgs[i]);
            }

            resultSet = Robolectric.shadowOf(stmt).getStatement().executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in query", e);
        }
        //TODO: assert rawquery with args returns actual values

        SQLiteCursor cursor = (SQLiteCursor) cursorFactory.newCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet, sqlBody);
        cursors.add(cursor);
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

    @Implementation
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in beginTransaction", e);
        } finally {
            inTransaction = true;
        }
    }

    @Implementation
    public void setTransactionSuccessful() {
        if (!isOpen()) {
            throw new IllegalStateException("connection is not opened");
        } else if (transactionSuccess) {
            throw new IllegalStateException("transaction already successfully");
        }
        transactionSuccess = true;
    }

    @Implementation
    public void endTransaction() {
        try {
            if (transactionSuccess) {
                transactionSuccess = false;
                connection.commit();
            } else {
                connection.rollback();
            }
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in beginTransaction", e);
        } finally {
            inTransaction = false;
        }
    }

    @Implementation
    public boolean inTransaction() {
        return inTransaction;
    }

    /**
     * Allows tests cases to query the transaction state
     *
     * @return
     */
    public boolean isTransactionSuccess() {
        return transactionSuccess;
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
        String scrubbedSql = DatabaseConfig.getScrubSQL(sql);
        try {
            SQLiteStatement stmt = Robolectric.newInstanceOf(SQLiteStatement.class);
            Robolectric.shadowOf(stmt).init(realSQLiteDatabase, scrubbedSql);
            return stmt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            unlock();
        }
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

    public boolean hasOpenCursors() {
        for (Cursor cursor : cursors) {
            if (!cursor.isClosed()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getQuerySql() {
        return querySql;
    }
}
