package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQuery;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.os.CancellationSignal;
import android.text.TextUtils;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.DatabaseConfig;
import org.robolectric.util.SQLite.SQLStringAndBindings;

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
import java.util.concurrent.locks.ReentrantLock;

import static android.database.sqlite.SQLiteDatabase.CursorFactory;
import static org.robolectric.Robolectric.newInstanceOf;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.SQLite.buildDeleteString;
import static org.robolectric.util.SQLite.buildInsertString;
import static org.robolectric.util.SQLite.buildUpdateString;
import static org.robolectric.util.SQLite.buildWhereClause;
import static org.robolectric.util.SQLite.fetchGeneratedKey;

/**
 * Shadow for {@code SQLiteDatabase} that simulates the movement of a {@code Cursor} through database tables.
 * Implemented as a wrapper around an embedded SQL database, accessed via JDBC. The JDBC connection is
 * made available to test cases for use in fixture setup and assertions.
 */
@Implements(value = SQLiteDatabase.class, inheritImplementationMethods = true)
public class ShadowSQLiteDatabase extends ShadowSQLiteClosable {

  public static final CursorFactory DEFAULT_CURSOR_FACTORY = new CursorFactory() {
    @Override public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery, String editTable, SQLiteQuery query) {
      return new SQLiteCursor(masterQuery, editTable, query);
    }
  };
  private static final Object connectionLock = new Object();
  private static final HashMap<File, SQLiteDatabase> dbMap = new HashMap<File, SQLiteDatabase>();

  private @RealObject SQLiteDatabase realSQLiteDatabase;
  private final ReentrantLock lock = new ReentrantLock(true);
  private boolean lockingEnabled = true;
  private Transaction transaction;
  private boolean throwOnInsert;
  private Set<Cursor> cursors = new HashSet<Cursor>();
  private List<String> querySql = new ArrayList<String>();
  private boolean isOpen, isFileConnection;
  private File path;
  private Connection connection;

  @Implementation
  public static SQLiteDatabase openDatabase(String path, CursorFactory factory, int flags, DatabaseErrorHandler errorHandler) {
    if (path == null) throw new IllegalArgumentException("path cannot be null");
    final File file = new File(path);

    SQLiteDatabase db = dbMap.get(file);
    if (db == null) {
      db = newInstanceOf(SQLiteDatabase.class);
      dbMap.put(file, db);
    }
    shadowOf(db).open(file, flags, errorHandler);
    return db;
  }

  private void open(File path, int flags, DatabaseErrorHandler errorHandler) {
    this.path = path;
    final boolean createIfNecessary = (flags & SQLiteDatabase.CREATE_IF_NECESSARY) != 0;

    if (createIfNecessary || path.exists()) {
      isFileConnection = path.exists();
      isOpen = true;
    } else if (!isOpen) {
      throw new SQLiteException("Could not open " + path);
    }
  }

  @Implementation
  public void setLockingEnabled(boolean lockingEnabled) {
    this.lockingEnabled = lockingEnabled;
  }

  @Implementation
  public boolean isDbLockedByCurrentThread(){
    return !lockingEnabled || lock.isHeldByCurrentThread();
  }

  @Implementation
  public String getPath() {
    return path.getAbsolutePath();
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

    SQLiteCursor cursor = new SQLiteCursor(null, null, null);
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
        int numArgs = bindArgs.length;
        for (int i = 0; i < numArgs; i++) {
          DatabaseUtils.bindObjectToProgram(statement, i + 1, bindArgs[i]);
        }
        statement.execute();
    } finally {
      if (statement != null) {
        statement.close();
      }
    }
  }

  @Implementation
  public Cursor rawQuery (String sql, String[] selectionArgs) {
    return rawQueryWithFactory(null, sql, selectionArgs, null);
  }

  @Implementation
  public Cursor rawQueryWithFactory(CursorFactory cursorFactory, String sql, String[] selectionArgs, String editTable) {
    String sqlBody = sql;
    if (sql != null) {
      sqlBody = buildWhereClause(sql, selectionArgs);
    }

    if (cursorFactory == null) {
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
  public Cursor rawQueryWithFactory(
      CursorFactory cursorFactory, String sql, String[] selectionArgs,
      String editTable, CancellationSignal cancellationSignal) {
    return rawQueryWithFactory(cursorFactory, sql, selectionArgs, editTable);
  }

  @Implementation
  public Cursor queryWithFactory(CursorFactory cursorFactory,
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

    // never close in-memory connections bc that deletes the in-memory db
    if (isFileConnection) {
      dbMap.remove(path);

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

  public void setThrowOnInsert(boolean throwOnInsert) {
    this.throwOnInsert = throwOnInsert;
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
        if (isFileConnection) {
          connection = DatabaseConfig.getFileConnection(path);
        } else {
          connection = DatabaseConfig.getMemoryConnection();
        }
      }
    }
    return connection;
  }

  public boolean isTransactionSuccess() {
    return transaction != null && transaction.success && transaction.descendantsSuccess;
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

  protected void lock() {
    if (lockingEnabled) {
      lock.lock();
    }
  }

  protected void unlock() {
    if (lockingEnabled) {
      lock.unlock();
    }
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
