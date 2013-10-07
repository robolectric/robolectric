package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.SQLite.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.SQLite.*;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 * Implemented as a wrapper around an embedded SQL database, accessed via JDBC. The JDBC connection is
 * made available to test cases for use in fixture setup and assertions.
 */
@Implements(value = SQLiteDatabase.class, inheritImplementationMethods = true)
public class ShadowSQLiteDatabase extends ShadowSQLiteClosable {

  public static final android.database.sqlite.SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = new SQLiteDatabase.CursorFactory() {
    @Override
    public Cursor newCursor(SQLiteDatabase db,
                SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
      return new SQLiteCursor(db, masterQuery, editTable, query);
    }

  };

  private static HashMap<String, SQLiteDatabase> dbMap = new HashMap<String, SQLiteDatabase>();

  @RealObject  SQLiteDatabase realSQLiteDatabase;
  private final ReentrantLock mLock = new ReentrantLock(true);
  private boolean mLockingEnabled = true;
  private WeakHashMap<SQLiteClosable, Object> mPrograms;
  private Transaction transaction;
  private boolean throwOnInsert;
  private Set<Cursor> cursors = new HashSet<Cursor>();
  private List<String> querySql = new ArrayList<String>();

  private boolean isOpen, isFileConnection;
  private String path;
  private final static Object connectionLock = new Object();
  private Connection connection;

  @Implementation
  public void setLockingEnabled(boolean lockingEnabled) {
    mLockingEnabled = lockingEnabled;
  }

  public void lock() {
    if (!mLockingEnabled) return;
    mLock.lock();
  }
  
  @Implementation
  public boolean isDbLockedByCurrentThread(){
    if(!mLockingEnabled) return true;
    return mLock.isHeldByCurrentThread();
  }

  public void unlock() {
    if (!mLockingEnabled) return;
    mLock.unlock();
  }

  private void init(String path) {
    this.path = path;
    isOpen = true;
  }

  public void setThrowOnInsert(boolean throwOnInsert) {
    this.throwOnInsert = throwOnInsert;
  }

  @Implementation
  public static SQLiteDatabase openDatabase(String path, SQLiteDatabase.CursorFactory factory, int flags) {
    if (path == null) throw new IllegalArgumentException("path cannot be null");

    SQLiteDatabase db = dbMap.get(path);
    if (db == null) {
      db = newInstanceOf(SQLiteDatabase.class);
      shadowOf(db).init(path);
      dbMap.put(path, db);
    }
    return db;
  }

  @Implementation
  public static SQLiteDatabase create(SQLiteDatabase.CursorFactory factory) {
    SQLiteDatabase db = newInstanceOf(SQLiteDatabase.class);
    shadowOf(db).init(null);
    return db;
  }

  public static void reset() {
    try {
      synchronized (connectionLock) {
        for (SQLiteDatabase db : dbMap.values()) {
          ShadowSQLiteDatabase shadowDb = shadowOf(db);
          if (shadowDb.connection != null) {
              shadowDb.connection.close();
          }
          shadowDb.connection = null;
        }
        dbMap.clear();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Allows test cases access to the underlying JDBC connection, for use in
   * setup or assertions.
   *
   * @return the connection
   */
  public Connection getConnection() {
    synchronized (connectionLock) {
      if (connection == null) {
        if (path != null) {
          connection = DatabaseConfig.getFileConnection(new File(path));
          isFileConnection = true;
        } else {
          connection = DatabaseConfig.getMemoryConnection();
        }
      }
    }
    return connection;
  }

  @Implementation
  public String getPath() {
    return path;
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
      PreparedStatement insert = getConnection().prepareStatement(sqlInsertString.sql, Statement.RETURN_GENERATED_KEYS);
      Iterator<Object> columns = sqlInsertString.columnValues.iterator();
      int i = 1;
      while (columns.hasNext()) {
        insert.setObject(i++, columns.next());
      }
      insert.executeUpdate();
      return fetchGeneratedKey(insert.getGeneratedKeys());
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
      Statement statement = getConnection().createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
      resultSet = statement.executeQuery(sql);
    } catch (SQLException e) {
      throw new SQLiteException("SQL exception in query", e);
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
      PreparedStatement statement = getConnection().prepareStatement(sqlUpdateString.sql);
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
      return getConnection().prepareStatement(sql).executeUpdate();
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
      getConnection().createStatement().execute(sql);
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

    SQLiteStatement statement = null;
      try {
        statement = compileStatement(sql);
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
  public Cursor rawQuery (String sql, String[] selectionArgs) {
    return rawQueryWithFactory(DEFAULT_CURSOR_FACTORY, sql, selectionArgs, null );
  }

  @Implementation
  public Cursor rawQueryWithFactory (SQLiteDatabase.CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
    String sqlBody = sql;
    if (sql != null) {
      sqlBody = buildWhereClause(sql, selectionArgs);
    }

    if(cursorFactory == null){
      cursorFactory = DEFAULT_CURSOR_FACTORY;
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
        throw new RuntimeException("SQL exception in rawQueryWithFactory", e);
      }
      //TODO: assert rawquery with args returns actual values

    SQLiteCursor cursor = (SQLiteCursor) cursorFactory.newCursor(null, null, null, null);
    shadowOf(cursor).setResultSet(resultSet, sqlBody);
    cursors.add(cursor);
    return cursor;
  }

  @Implementation
  public Cursor queryWithFactory(SQLiteDatabase.CursorFactory cursorFactory,
            boolean distinct, String table, String[] columns,
            String selection, String[] selectionArgs, String groupBy,
            String having, String orderBy, String limit) {
    String sql = SQLiteQueryBuilder.buildQueryString(
        distinct, table, columns, selection, groupBy, having, orderBy, limit);

    return rawQueryWithFactory(cursorFactory, sql, selectionArgs, findEditTable(table));
  }

  @Implementation
  public static String findEditTable(String tables) {
    if (!TextUtils.isEmpty(tables)) {
      // find the first word terminated by either a space or a comma
      int spacepos = tables.indexOf(' ');
      int commapos = tables.indexOf(',');

      if (spacepos > 0 && (spacepos < commapos || commapos < 0)) {
        return tables.substring(0, spacepos);
      } else if (commapos > 0 && (commapos < spacepos || spacepos < 0)) {
        return tables.substring(0, commapos);
      }
      return tables;
    } else {
      throw new IllegalStateException("Invalid tables");
    }
  }

  @Implementation
  public boolean isOpen() {
    return isOpen;
  }

  @Implementation
  public void close() {
    isOpen = false;
    dbMap.remove(path);
    // never close in-memory connections bc that deletes the in-memory db
    if (isFileConnection) {
      try {
        connection.close();
      } catch (SQLException ignored) {
      }
    }
  }

  @Implementation
  public boolean isReadOnly() {
    return false;
  }

  @Implementation
  public void beginTransaction() {
    try {
      getConnection().setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException("SQL exception in beginTransaction", e);
    }

    if (transaction == null) {
      transaction = new Transaction();
    } else {
      transaction = new Transaction(transaction);
    }
  }

  @Implementation
  public void setTransactionSuccessful() {
    if (!isOpen()) {
      throw new IllegalStateException("connection is not opened");
    } else if (transaction.success) {
      throw new IllegalStateException("transaction already successfully");
    }
    transaction.success = true;
  }

  @Implementation
  public void endTransaction() {
    if (transaction.parent != null) {
      transaction.parent.descendantsSuccess &= transaction.success;
      transaction = transaction.parent;
    } else {
      try {
        if (transaction.success && transaction.descendantsSuccess) {
          getConnection().commit();
        } else {
          getConnection().rollback();
        }
          getConnection().setAutoCommit(true);
      } catch (SQLException e) {
        throw new RuntimeException("SQL exception in endTransaction", e);
      }
      transaction = null;
    }
  }

  @Implementation
  public boolean inTransaction() {
    return transaction != null;
  }

  /**
   * Allows tests cases to query the transaction state
   * @return
   */
  public boolean isTransactionSuccess() {
    return transaction != null && transaction.success && transaction.descendantsSuccess;
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

  private static class Transaction {
    final Transaction parent;
    boolean success;
    boolean descendantsSuccess = true;

    Transaction(Transaction parent) {
      this.parent = parent;
    }

    Transaction() {
      this.parent = null;
    }
  }

}
