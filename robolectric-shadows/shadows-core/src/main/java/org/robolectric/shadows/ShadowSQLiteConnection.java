package org.robolectric.shadows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteAccessPermException;
import android.database.sqlite.SQLiteBindOrColumnIndexOutOfRangeException;
import android.database.sqlite.SQLiteBlobTooBigException;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteCustomFunction;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteDatatypeMismatchException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteMisuseException;
import android.database.sqlite.SQLiteOutOfMemoryException;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.database.sqlite.SQLiteTableLockedException;
import android.os.OperationCanceledException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteConstants;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import com.google.common.util.concurrent.Uninterruptibles;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.util.SQLiteLibraryLoader;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.castNativePtr;

/**
 * Shadow for {@link android.database.sqlite.SQLiteConnection}.
 */
@Implements(value = android.database.sqlite.SQLiteConnection.class, isInAndroidSdk = false)
public class ShadowSQLiteConnection {

  private static final String IN_MEMORY_PATH = ":memory:";
  private static final Connections CONNECTIONS = new Connections();
  private static final Pattern COLLATE_LOCALIZED_UNICODE_PATTERN =
      Pattern.compile("\\s+COLLATE\\s+(LOCALIZED|UNICODE)", Pattern.CASE_INSENSITIVE);

  // indicates an ignored statement
  private static final int IGNORED_REINDEX_STMT = -2;

  private static boolean useInMemoryDatabase;

  private static SQLiteConnection connection(long pointer) {
    return CONNECTIONS.getConnection(pointer);
  }

  private static SQLiteStatement stmt(long connectionPtr, long pointer) {
    return CONNECTIONS.getStatement(connectionPtr, pointer);
  }

  public static void setUseInMemoryDatabase(boolean value) {
    useInMemoryDatabase = value;
  }

  @Implementation
  public static Number nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    SQLiteLibraryLoader.load();
    return castNativePtr(CONNECTIONS.open(path));
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativePrepareStatement(int connectionPtr, String sql) {
    return (int) nativePrepareStatement((long) connectionPtr, sql);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static long nativePrepareStatement(long connectionPtr, String sql) {
    final String newSql = convertSQLWithLocalizedUnicodeCollator(sql);
    return CONNECTIONS.prepareStatement(connectionPtr, newSql);
  }

  /**
   * Convert SQL with phrase COLLATE LOCALIZED or COLLATE UNICODE to COLLATE NOCASE.
   */
  static String convertSQLWithLocalizedUnicodeCollator(String sql) {
    Matcher matcher = COLLATE_LOCALIZED_UNICODE_PATTERN.matcher(sql);
    return matcher.replaceAll(" COLLATE NOCASE");
  }

  @Resetter
  public static void reset() {
    CONNECTIONS.reset();
    useInMemoryDatabase = false;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeClose(int connectionPtr) {
    nativeClose((long) connectionPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeClose(long connectionPtr) {
    CONNECTIONS.close(connectionPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeFinalizeStatement(int connectionPtr, int statementPtr) {
    nativeFinalizeStatement((long) connectionPtr, statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeFinalizeStatement(long connectionPtr, long statementPtr) {
    CONNECTIONS.finalizeStmt(connectionPtr, statementPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeGetParameterCount(int connectionPtr, int statementPtr) {
    return nativeGetParameterCount((long) connectionPtr, statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeGetParameterCount(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return 0; }
    return CONNECTIONS.execute("get parameters count in prepared statement", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getBindParameterCount();
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static boolean nativeIsReadOnly(int connectionPtr, int statementPtr) {
    return nativeIsReadOnly((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static boolean nativeIsReadOnly(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return true; }
    return CONNECTIONS.execute("call isReadOnly", new Callable<Boolean>() {
      @Override
      public Boolean call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.isReadOnly();
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static long nativeExecuteForLong(int connectionPtr, int statementPtr) {
    return nativeExecuteForLong((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static long nativeExecuteForLong(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for long", new Callable<Long>() {
      @Override
      public Long call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        if (!stmt.step()) {
          throw new SQLiteException(SQLiteConstants.SQLITE_DONE, "No rows returned from query");
        }
        return stmt.columnLong(0);
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeExecute(int connectionPtr, int statementPtr) {
    nativeExecute((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeExecute(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return; }
    CONNECTIONS.execute("execute", new Callable<Object>() {
      @Override
      public Object call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeExecuteForString(int connectionPtr, int statementPtr) {
    return nativeExecuteForString((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String nativeExecuteForString(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for string", new Callable<String>() {
      @Override
      public String call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        if (!stmt.step()) {
          throw new SQLiteException(SQLiteConstants.SQLITE_DONE, "No rows returned from query");
        }
        return stmt.columnString(0);
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeGetColumnCount(int connectionPtr, int statementPtr) {
    return nativeGetColumnCount((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeGetColumnCount(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("get columns count", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.columnCount();
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static String nativeGetColumnName(int connectionPtr, int statementPtr, int index) {
    return nativeGetColumnName((long) connectionPtr, (long) statementPtr, index);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static String nativeGetColumnName(final long connectionPtr, final long statementPtr, final int index) {
    return CONNECTIONS.execute("get column name at index " + index, new Callable<String>() {
      @Override
      public String call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getColumnName(index);
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeBindNull(int connectionPtr, int statementPtr, int index) {
    nativeBindNull((long) connectionPtr, (long) statementPtr, index);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeBindNull(final long connectionPtr, final long statementPtr, final int index) {
    CONNECTIONS.execute("bind null at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bindNull(index);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeBindLong(int connectionPtr, int statementPtr, int index, long value) {
    nativeBindLong((long) connectionPtr, (long) statementPtr, index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeBindLong(final long connectionPtr, final long statementPtr, final int index, final long value) {
    CONNECTIONS.execute("bind long at index " + index + " with value " + value, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeBindDouble(int connectionPtr, int statementPtr, int index, double value) {
    nativeBindDouble((long) connectionPtr, (long) statementPtr, index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeBindDouble(final long connectionPtr, final long statementPtr, final int index, final double value) {
    CONNECTIONS.execute("bind double at index " + index + " with value " + value, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeBindString(int connectionPtr, int statementPtr, int index, String value) {
    nativeBindString((long) connectionPtr, (long) statementPtr, index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeBindString(final long connectionPtr, final long statementPtr, final int index, final String value) {
    CONNECTIONS.execute("bind string at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeBindBlob(int connectionPtr, int statementPtr, int index, byte[] value) {
    nativeBindBlob((long) connectionPtr, (long) statementPtr, index, value);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeBindBlob(final long connectionPtr, final long statementPtr, final int index, final byte[] value) {
    CONNECTIONS.execute("bind blob at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeRegisterLocalizedCollators(int connectionPtr, String locale) {
    nativeRegisterLocalizedCollators((long) connectionPtr, locale);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeRegisterLocalizedCollators(long connectionPtr, String locale) {
    // TODO: find a way to create a collator
    // http://www.sqlite.org/c3ref/create_collation.html
    // xerial jdbc driver does not have a Java method for sqlite3_create_collation
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeExecuteForChangedRowCount(int connectionPtr, int statementPtr) {
    return nativeExecuteForChangedRowCount((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeExecuteForChangedRowCount(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for changed row count", new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return connection(connectionPtr).getChanges();
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static long nativeExecuteForLastInsertedRowId(int connectionPtr, int statementPtr) {
    return nativeExecuteForLastInsertedRowId((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static long nativeExecuteForLastInsertedRowId(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for last inserted row ID", new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return connection(connectionPtr).getLastInsertId();
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static long nativeExecuteForCursorWindow(int connectionPtr, int statementPtr, int windowPtr,
                                                  int startPos, int requiredPos, boolean countAllRows) {
    return nativeExecuteForCursorWindow((long) connectionPtr, (long) statementPtr, (long) windowPtr,
        startPos, requiredPos, countAllRows);
}

  @Implementation(minSdk = LOLLIPOP)
  public static long nativeExecuteForCursorWindow(final long connectionPtr, final long statementPtr, final long windowPtr,
                                                  final int startPos, final int requiredPos, final boolean countAllRows) {

    return CONNECTIONS.execute("execute for cursor window", new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return ShadowCursorWindow.setData(windowPtr, stmt);
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeResetStatementAndClearBindings(int connectionPtr, int statementPtr) {
    nativeResetStatementAndClearBindings((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeResetStatementAndClearBindings(final long connectionPtr, final long statementPtr) {
    CONNECTIONS.execute("reset statement", new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.reset(true);
        return null;
      }
    });
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeCancel(int connectionPtr) {
    nativeCancel((long) connectionPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeCancel(long connectionPtr) {
    CONNECTIONS.cancel(connectionPtr);
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeResetCancel(int connectionPtr, boolean cancelable) {
    nativeResetCancel((long) connectionPtr, cancelable);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeResetCancel(long connectionPtr, boolean cancelable) {
    // handled in com.almworks.sqlite4java.SQLiteConnection#exec
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static void nativeRegisterCustomFunction(int connectionPtr, SQLiteCustomFunction function) {
    nativeRegisterCustomFunction((long) connectionPtr, function);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static void nativeRegisterCustomFunction(long connectionPtr, SQLiteCustomFunction function) {
    // not supported
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeExecuteForBlobFileDescriptor(int connectionPtr, long statementPtr) {
    return nativeExecuteForBlobFileDescriptor((long) connectionPtr, (long) statementPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeExecuteForBlobFileDescriptor(long connectionPtr, long statementPtr) {
    // impossible to support without native code?
    return -1;
  }

  @Implementation(maxSdk = KITKAT_WATCH)
  public static int nativeGetDbLookaside(int connectionPtr) {
    return nativeGetDbLookaside((long) connectionPtr);
  }

  @Implementation(minSdk = LOLLIPOP)
  public static int nativeGetDbLookaside(long connectionPtr) {
    // not supported by sqlite4java
    return 0;
  }
// VisibleForTesting
static class Connections {
  private final AtomicLong pointerCounter = new AtomicLong(0);
  private final Map<Long, SQLiteStatement> statementsMap = new ConcurrentHashMap<>();
  private final Map<Long, SQLiteConnection> connectionsMap = new ConcurrentHashMap<>();
  private ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

  public SQLiteConnection getConnection(final long pointer) {
    SQLiteConnection connection = connectionsMap.get(pointer);
    if (connection == null) {
      throw new IllegalStateException("Illegal connection pointer " + pointer
          + ". Current pointers for thread " + Thread.currentThread() + " " + connectionsMap.keySet());
    }
    return connection;
  }

  public SQLiteStatement getStatement(final long connectionPtr, final long pointer) {
    // ensure connection is ok
    getConnection(connectionPtr);

    SQLiteStatement stmt = statementsMap.get(pointer);
    if (stmt == null) {
      throw new IllegalArgumentException("Invalid prepared statement pointer: " + pointer + ". Current pointers: " + statementsMap.keySet());
    }
    if (stmt.isDisposed()) {
      throw new IllegalStateException("Statement " + pointer + " " + stmt + " is disposed");
    }
    return stmt;
  }

  public long open(final String path) {
    SQLiteConnection dbConnection = execute("open SQLite connection", new Callable<SQLiteConnection>() {
      @Override
      public SQLiteConnection call() throws Exception {
        SQLiteConnection connection = useInMemoryDatabase || IN_MEMORY_PATH.equals(path)
            ? new SQLiteConnection()
            : new SQLiteConnection(new File(path));

        connection.open();
        return connection;
      }
    });

    long ptr = pointerCounter.incrementAndGet();
    connectionsMap.put(ptr, dbConnection);
    return ptr;
  }

  public long prepareStatement(final long connectionPtr, final String sql) {
    // TODO: find a way to create collators
    if ("REINDEX LOCALIZED".equals(sql)) {
      return IGNORED_REINDEX_STMT;
    }

    SQLiteStatement stmt = execute("prepare statement", new Callable<SQLiteStatement>() {
      @Override
      public SQLiteStatement call() throws Exception {
        SQLiteConnection connection = getConnection(connectionPtr);
        return connection.prepare(sql);
      }
    });

    long pointer = pointerCounter.incrementAndGet();
    statementsMap.put(pointer, stmt);
    return pointer;
  }

  public void close(final long ptr) {
    execute("close connection", new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteConnection connection = getConnection(ptr);
        connection.dispose();
        return null;
      }
    });
  }

  public void reset() {
    for (long connectionPtr : connectionsMap.keySet()) {
      close(connectionPtr);
    }
    dbExecutor.shutdown();
    try {
      dbExecutor.awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    dbExecutor = Executors.newSingleThreadExecutor();
    connectionsMap.clear();
    statementsMap.clear();
  }

  public void finalizeStmt(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) {
      return;
    }
    execute("finalize statement", new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = getStatement(connectionPtr, statementPtr);
        statementsMap.remove(statementPtr);
        stmt.dispose();
        return null;
      }
    });
  }

  public void cancel(long connectionPtr) {
    getConnection(connectionPtr); // check connection

    execute("cancel", new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement statement = statementsMap.get(pointerCounter.get());
        if (statement != null) {
          statement.cancel();
        }
        return null;
      }
    });
  }

  public <T> T execute(final String comment, final Callable<T> work) {
    try {
      return Uninterruptibles.getUninterruptibly(dbExecutor.submit(work));
      // No need to catch cancellationexception - we never cancel these futures
    } catch (ExecutionException e) {
      Throwable t = e.getCause();
      if (t instanceof SQLiteException) {
        RuntimeException sqlException = getSqliteException("Cannot " + comment,
            ((SQLiteException) t).getBaseErrorCode());
        sqlException.initCause(e);
        throw sqlException;
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  private RuntimeException getSqliteException(String message, int baseErrorCode) {
    // Mapping is from throw_sqlite3_exception in android_database_SQLiteCommon.cpp
    switch (baseErrorCode) {
      case SQLiteConstants.SQLITE_ABORT: return new SQLiteAbortException(message);
      case SQLiteConstants.SQLITE_PERM: return new SQLiteAccessPermException(message);
      case SQLiteConstants.SQLITE_RANGE: return new SQLiteBindOrColumnIndexOutOfRangeException(message);
      case SQLiteConstants.SQLITE_TOOBIG: return new SQLiteBlobTooBigException(message);
      case SQLiteConstants.SQLITE_CANTOPEN: return new SQLiteCantOpenDatabaseException(message);
      case SQLiteConstants.SQLITE_CONSTRAINT: return new SQLiteConstraintException(message);
      case SQLiteConstants.SQLITE_NOTADB: // fall through
      case SQLiteConstants.SQLITE_CORRUPT: return new SQLiteDatabaseCorruptException(message);
      case SQLiteConstants.SQLITE_BUSY: return new SQLiteDatabaseLockedException(message);
      case SQLiteConstants.SQLITE_MISMATCH: return new SQLiteDatatypeMismatchException(message);
      case SQLiteConstants.SQLITE_IOERR: return new SQLiteDiskIOException(message);
      case SQLiteConstants.SQLITE_DONE: return new SQLiteDoneException(message);
      case SQLiteConstants.SQLITE_FULL: return new SQLiteFullException(message);
      case SQLiteConstants.SQLITE_MISUSE: return new SQLiteMisuseException(message);
      case SQLiteConstants.SQLITE_NOMEM: return new SQLiteOutOfMemoryException(message);
      case SQLiteConstants.SQLITE_READONLY: return new SQLiteReadOnlyDatabaseException(message);
      case SQLiteConstants.SQLITE_LOCKED: return new SQLiteTableLockedException(message);
      case SQLiteConstants.SQLITE_INTERRUPT: return new OperationCanceledException(message);
      default: return new android.database.sqlite.SQLiteException(message
          + ", base error code: " + baseErrorCode);
    }
  }
}
}
