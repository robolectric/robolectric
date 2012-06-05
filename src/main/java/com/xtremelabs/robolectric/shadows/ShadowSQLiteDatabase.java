package com.xtremelabs.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteTransactionListener;
import android.os.SystemClock;
import android.util.Log;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;
import com.xtremelabs.robolectric.util.DatabaseConfig;
import com.xtremelabs.robolectric.util.SQLite.SQLStringAndBindings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.xtremelabs.robolectric.Robolectric.newInstanceOf;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.SQLite.buildDeleteString;
import static com.xtremelabs.robolectric.util.SQLite.buildInsertString;
import static com.xtremelabs.robolectric.util.SQLite.buildUpdateString;
import static com.xtremelabs.robolectric.util.SQLite.buildWhereClause;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 * Implemented as a wrapper around an embedded SQL database, accessed via JDBC.  The JDBC connection is
 * made available to test cases for use in fixture setup and assertions.
 */
@Implements(SQLiteDatabase.class)
public class ShadowSQLiteDatabase  {

    private static final String TAG = "ShadowSQLiteDatabase";

    private static final String COMMIT_SQL = "COMMIT;";
    private static final String BEGIN_SQL = "BEGIN;";

    private static Object staticLock = new Object();
    private static Map<String, Connection> connections = new HashMap<String, Connection>();
    private static Map<String, List<ShadowSQLiteDatabase>> sqliteDatabaseMap =
            new HashMap<String, List<ShadowSQLiteDatabase>>();

    private Connection connection;
    private String path;
    @RealObject SQLiteDatabase realSQLiteDatabase;
    private final DatabaseReentrantLock mLock = new DatabaseReentrantLock(true);
    private boolean mLockingEnabled = true;
    private boolean throwOnInsert;

    private SQLiteTransactionListener mTransactionListener;
    private boolean mInnerTransactionIsSuccessful;
    private boolean mTransactionIsSuccessful;

    @Implementation
    public void setLockingEnabled(boolean lockingEnabled) {
        mLockingEnabled = lockingEnabled;
    }

    /**
     * Locks the database for exclusive access. The database lock must be held when
     * touch the native sqlite3* object since it is single threaded and uses
     * a polling lock contention algorithm. The lock is recursive, and may be acquired
     * multiple times by the same thread. This is a no-op if mLockingEnabled is false.
     *
     * @see #unlock()
     */
    /* package */ void lock(String sql) {
        lock(false);
    }

    private static final long LOCK_WAIT_PERIOD = 30L;
    void lock(boolean forced) {
        // make sure this method is NOT being called from a 'synchronized' method
        if (Thread.holdsLock(this)) {
            Log.w(TAG, "don't lock() while in a synchronized method");
        }
        verifyDbIsOpen();
        if (!forced && !mLockingEnabled) return;
        boolean done = false;
        long timeStart = SystemClock.uptimeMillis();
        while (!done) {
            try {
                // wait for 30sec to acquire the lock
                done = mLock.tryLock(LOCK_WAIT_PERIOD, TimeUnit.SECONDS);
                if (!done) {
                    // lock not acquired in NSec. print a message and stacktrace saying the lock
                    // has not been available for 30sec.
                    Log.w(TAG, "database lock has not been available for " + LOCK_WAIT_PERIOD +
                            " sec. Current Owner of the lock is " + mLock.getOwnerDescription() +
                            ". Continuing to wait in thread: " + Thread.currentThread().getId());
                }
            } catch (InterruptedException e) {
                // ignore the interruption
            }
        }
    }

    /**
     * Releases the database lock. This is a no-op if mLockingEnabled is false.
     *
     * @see #unlock()
     */
    /* package */ void unlock() {
        if (!mLockingEnabled) return;
        mLock.unlock();
    }

    /**
     * Locks the database for exclusive access. The database lock must be held when
     * touch the native sqlite3* object since it is single threaded and uses
     * a polling lock contention algorithm. The lock is recursive, and may be acquired
     * multiple times by the same thread.
     *
     * @see #unlockForced()
     */
    private void lockForced() {
        lock(true);
    }

    /**
     * Releases the database lock.
     *
     * @see #unlockForced()
     */
    private void unlockForced() {
        mLock.unlock();
    }


    private static class DatabaseReentrantLock extends ReentrantLock {
        DatabaseReentrantLock(boolean fair) {
            super(fair);
        }
        @Override
        public Thread getOwner() {
            return super.getOwner();
        }
        public String getOwnerDescription() {
            Thread t = getOwner();
            return (t== null) ? "none" : String.valueOf(t.getId());
        }
    }

    public void setThrowOnInsert(boolean throwOnInsert) {
        this.throwOnInsert = throwOnInsert;
    }

    /**
     * Getter for the path to the database file.
     *
     * @return the path to our database file.
     */
    @Implementation
    public final String getPath() {
        return path;
    }

    @Implementation
    public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
        synchronized (staticLock) {
            Connection connection = connections.get(path);
            if (connection == null) {
                connection = DatabaseConfig.getMemoryConnection();
                connections.put(path, connection);
            }
            SQLiteDatabase database = newInstanceOf(SQLiteDatabase.class);
            List<ShadowSQLiteDatabase> sqLiteDatabases = sqliteDatabaseMap.get(path);
            if (sqLiteDatabases == null) {
                sqLiteDatabases = new ArrayList<ShadowSQLiteDatabase>();
                sqliteDatabaseMap.put(path, sqLiteDatabases);
            }
            ShadowSQLiteDatabase shadow = Robolectric.shadowOf(database);
            sqLiteDatabases.add(shadow);
            connections.put(path, connection);
            shadow.connection = connection;
            shadow.path = path;
            return database;
        }
    }

    public static void deleteDatabase(String path) {
        synchronized (staticLock) {
            if (sqliteDatabaseMap.containsKey(path)) {
                throw new RuntimeException("Must first close any open databases");
            }
            Connection connection = connections.remove(path);
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to close connection");
                }
            }
        }
    }

    @Implementation
    public long insert(String table, String nullColumnHack, ContentValues values) {
        return insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_NONE);
    }

    @Implementation
    public long insertOrThrow(String table, String nullColumnHack, ContentValues values) {
        if (throwOnInsert)
            throw new android.database.SQLException();
        return insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_NONE);
    }

    @Implementation
    public long replace(String table, String nullColumnHack, ContentValues values) {
        return insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Implementation
    public long replaceOrThrow(String table, String nullColumnHack, ContentValues initialValues) {
        if (throwOnInsert)
            throw new android.database.SQLException();
        return insertWithOnConflict(table, nullColumnHack, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Implementation
    public long insertWithOnConflict(String table, String nullColumnHack,
                                     ContentValues initialValues, int conflictAlgorithm) {

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
            return -1; // this is how SQLite behaves, unlike H2 which throws exceptions
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

        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException("SQL exception in query", e);
        }

        SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet,sql);
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
            String scrubbedSql= DatabaseConfig.getScrubSQL(sql);
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
        String scrubbedSql= DatabaseConfig.getScrubSQL(sql);


        SQLiteStatement statement = null;
        try {
            statement =compileStatement(scrubbedSql);
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
    public Cursor rawQuery (String sql, String[] selectionArgs){
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


        SQLiteCursor cursor = new SQLiteCursor(null, null, null, null);
        shadowOf(cursor).setResultSet(resultSet, sqlBody);
        return cursor;
    }

    @Implementation
    public Cursor rawQueryWithFactory(
            SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs,
            String editTable) {
        return rawQuery(sql, selectionArgs);
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
        synchronized (staticLock) {
            List<ShadowSQLiteDatabase> sqLiteDatabases = sqliteDatabaseMap.get(path);
            if (sqLiteDatabases == null) {
                throw new RuntimeException("Unexpected missing database list");
            }
            sqLiteDatabases.remove(this);
            if (sqLiteDatabases.size() == 0) {
                sqliteDatabaseMap.remove(path);
            }
        }
        connection = null;
    }

    @Implementation
    public void beginTransaction() {
        beginTransaction(null /* transactionStatusCallback */, true);
    }

    private void beginTransaction(SQLiteTransactionListener transactionListener,
                                  boolean exclusive) {
        verifyDbIsOpen();
        lockForced();
        boolean ok = false;
        try {
            // If this thread already had the lock then get out
            if (mLock.getHoldCount() > 1) {
                if (mInnerTransactionIsSuccessful) {
                    String msg = "Cannot call beginTransaction between "
                            + "calling setTransactionSuccessful and endTransaction";
                    IllegalStateException e = new IllegalStateException(msg);
                    Log.e(TAG, "beginTransaction() failed", e);
                    throw e;
                }
                ok = true;
                return;
            }

            if (isH2()) {
                try {
                    connection.setAutoCommit(false);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                execSQL("BEGIN EXCLUSIVE;");
            }
            mTransactionListener = transactionListener;
            mTransactionIsSuccessful = true;
            mInnerTransactionIsSuccessful = false;
            if (transactionListener != null) {
                try {
                    transactionListener.onBegin();
                } catch (RuntimeException e) {
                    if (isH2()) {
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            throw new RuntimeException(e1);
                        }
                    } else {
                        execSQL("ROLLBACK;");
                    }
                    throw e;
                }
            }
            ok = true;
        } finally {
            if (!ok) {
                // beginTransaction is called before the try block so we must release the lock in
                // the case of failure.
                unlockForced();
            }
        }
    }

    @Implementation
    public void endTransaction() {
        verifyLockOwner();
        try {
            if (mInnerTransactionIsSuccessful) {
                mInnerTransactionIsSuccessful = false;
            } else {
                mTransactionIsSuccessful = false;
            }
            if (mLock.getHoldCount() != 1) {
                return;
            }
            RuntimeException savedException = null;
            if (mTransactionListener != null) {
                try {
                    if (mTransactionIsSuccessful) {
                        mTransactionListener.onCommit();
                    } else {
                        mTransactionListener.onRollback();
                    }
                } catch (RuntimeException e) {
                    savedException = e;
                    mTransactionIsSuccessful = false;
                }
            }

            if (isH2()) {
                try {
                    if (mTransactionIsSuccessful) {
                        connection.commit();
                    } else {
                        connection.rollback();
                    }
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    // Swallow
                }
            } else {

                if (mTransactionIsSuccessful) {
                    execSQL(COMMIT_SQL);
                } else {
                    try {
                        execSQL("ROLLBACK;");
                        if (savedException != null) {
                            throw savedException;
                        }
                    } catch (android.database.SQLException e) {
                        if (false) {
                            Log.d(TAG, "exception during rollback, maybe the DB previously "
                                    + "performed an auto-rollback");
                        }
                    }
                }
            }
        } finally {
            mTransactionListener = null;
            unlockForced();
            if (false) {
                Log.v(TAG, "unlocked " + Thread.currentThread()
                        + ", holdCount is " + mLock.getHoldCount());
            }
        }
    }

    /* package */ void verifyLockOwner() {
        verifyDbIsOpen();
        if (mLockingEnabled && !isDbLockedByCurrentThread()) {
            throw new IllegalStateException("Don't have database lock!");
        }
    }

    @Implementation
    public boolean isDbLockedByCurrentThread() {
        return mLock.isHeldByCurrentThread();
    }

    @Implementation
    public void setTransactionSuccessful() {
        verifyDbIsOpen();
        if (!mLock.isHeldByCurrentThread()) {
            throw new IllegalStateException("no transaction pending");
        }
        if (mInnerTransactionIsSuccessful) {
            throw new IllegalStateException(
                    "setTransactionSuccessful may only be called once per call to beginTransaction");
        }
        mInnerTransactionIsSuccessful = true;
    }

    /* package */ void verifyDbIsOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("database " + getPath() + " already closed");
        }
    }

    public boolean isTransactionSuccess() {
        return mInnerTransactionIsSuccessful;
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
        lockForced();
        String scrubbedSql= DatabaseConfig.getScrubSQL(sql);
        try {
            SQLiteStatement stmt = Robolectric.newInstanceOf(SQLiteStatement.class);
            Robolectric.shadowOf(stmt).init(realSQLiteDatabase, scrubbedSql);
            return stmt;
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            unlockForced();
        }
    }

    private boolean isH2() {
        Class<? extends Connection> aClass = connection.getClass();
        return (aClass.getName().startsWith("org.h2"));
    }
}
