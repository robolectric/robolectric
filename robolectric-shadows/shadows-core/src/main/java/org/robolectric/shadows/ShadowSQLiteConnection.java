package org.robolectric.shadows;

import android.database.sqlite.SQLiteCustomFunction;
import android.database.sqlite.SQLiteDoneException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.util.SQLiteLibraryLoader;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Implements(value = android.database.sqlite.SQLiteConnection.class, isInAndroidSdk = false)
public class ShadowSQLiteConnection {
  private static final String IN_MEMORY_PATH = ":memory:";
  private static final Connections CONNECTIONS = new Connections();

  // indicates an ignored statement
  private static final int IGNORED_REINDEX_STMT = -2;

  static {
    SQLiteLibraryLoader.load();
  }

  // TODO: Handle API 21 int -> long changes
  private static SQLiteConnection connection(final long pointer) {
    return CONNECTIONS.getConnection(pointer);
  }

  // TODO: Handle API 21 int -> long changes
  private static SQLiteStatement stmt(final long connectionPtr, final long pointer) {
    return CONNECTIONS.getStatement(connectionPtr, pointer);
  }

  private static void rethrow(final String message, final SQLiteException e) {
    throw new android.database.sqlite.SQLiteException(message + ", base error code: " + e.getBaseErrorCode(), e);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static long nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    return CONNECTIONS.open(path);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static long nativePrepareStatement(long connectionPtr, String sql) {
    return CONNECTIONS.prepareStatement(connectionPtr, sql);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeClose(long connectionPtr) {
    CONNECTIONS.close(connectionPtr);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeFinalizeStatement(long connectionPtr, long statementPtr) {
    CONNECTIONS.finalizeStmt(connectionPtr, statementPtr);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static int nativeGetParameterCount(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return 0; } // TODO
    return CONNECTIONS.execute("get parameters count in prepared statement", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getBindParameterCount();
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static boolean nativeIsReadOnly(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return true; } // TODO
    return CONNECTIONS.execute("call isReadOnly", new Callable<Boolean>() {
      @Override
      public Boolean call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.isReadOnly();
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static long nativeExecuteForLong(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for long", new Callable<Long>() {
      @Override
      public Long call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        if (!stmt.step()) {
          throw new SQLiteDoneException();
        }
        return stmt.columnLong(0);
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeExecute(final long connectionPtr, final long statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return; } // TODO
    CONNECTIONS.execute("execute", new Callable<Object>() {
      @Override
      public Object call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return null;
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static String nativeExecuteForString(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("execute for string", new Callable<String>() {
      @Override
      public String call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        if (!stmt.step()) {
          throw new SQLiteDoneException();
        }
        return stmt.columnString(0);
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static int nativeGetColumnCount(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.execute("get columns count", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.columnCount();
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static String nativeGetColumnName(final long connectionPtr, final long statementPtr, final int index) {
    return CONNECTIONS.execute("get column name at index " + index, new Callable<String>() {
      @Override
      public String call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getColumnName(index);
      }
    });
  }

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeRegisterLocalizedCollators(long connectionPtr, String locale) {
    // TODO: find a way to create a collator
    // http://www.sqlite.org/c3ref/create_collation.html
    // xerial jdbc driver does not have a Java method for sqlite3_create_collation
  }

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
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

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeCancel(long connectionPtr) {
    CONNECTIONS.cancel(connectionPtr);
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeResetCancel(long connectionPtr, boolean cancelable) {
    // handled in com.almworks.sqlite4java.SQLiteConnection#exec
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static void nativeRegisterCustomFunction(long connectionPtr, SQLiteCustomFunction function) {
    // not supported
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static int nativeExecuteForBlobFileDescriptor(long connectionPtr, long statementPtr) {
    // impossible to support without native code?
    return -1;
  }

  @Implementation // TODO: Handle API 21 int -> long changes
  public static int nativeGetDbLookaside(long connectionPtr) {
    // not supported by sqlite4java
    return 0;
  }

  // TODO: Handle API 21 int -> long changes
  private static class Connections {
    private final AtomicLong pointerCounter = new AtomicLong(0);
    private final Map<Long, SQLiteStatement> statementsMap = new ConcurrentHashMap<Long, SQLiteStatement>();
    private final Map<Long, SQLiteConnection> connectionsMap = new ConcurrentHashMap<Long, SQLiteConnection>();
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

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
          SQLiteConnection connection = IN_MEMORY_PATH.equals(path)
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

    // TODO: Handle API 21 int -> long changes
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
      Future<DbOperationResult<T>> future = dbExecutor.submit(new Callable<DbOperationResult<T>>() {
        @Override
        public DbOperationResult<T> call() throws Exception {
          T result = null;
          Exception error = null;
          try {
            result = work.call();
          } catch (Exception e) {
            error = e;
          }
          return new DbOperationResult<T>(result, error);
        }
      });

      DbOperationResult<T> execResult;
      try {
        execResult = future.get();

        if (execResult.error != null) {
          if (execResult.error instanceof SQLiteException) {
            rethrow("Cannot " + comment, (SQLiteException) execResult.error);
          } else if (execResult.error instanceof android.database.sqlite.SQLiteException) {
            throw (android.database.sqlite.SQLiteException) execResult.error;
          } else {
            throw new RuntimeException(execResult.error);
          }
        }

        return execResult.value;

      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    private static class DbOperationResult<T> {
      private final T value;
      private final Exception error;

      DbOperationResult(T value, Exception error) {
        this.value = value;
        this.error = error;
      }
    }
  }
}
