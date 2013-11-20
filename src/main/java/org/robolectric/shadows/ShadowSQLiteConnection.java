package org.robolectric.shadows;

import android.database.sqlite.SQLiteCustomFunction;
import android.database.sqlite.SQLiteDoneException;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.SQLiteLibraryLoader;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shadows Android native SQLite connection.
 */
@Implements(android.database.sqlite.SQLiteConnection.class)
public class ShadowSQLiteConnection {

  private static final String IN_MEMORY_PATH = ":memory:";

  private static final Connections CONNECTIONS = new Connections();

  // indicates an ignored statement
  private static final int IGNORED_REINDEX_STMT = -2;

  static {
    SQLiteLibraryLoader.load();
  }

  private static SQLiteConnection connection(final int pointer) {
    return CONNECTIONS.getConnection(pointer);
  }

  private static SQLiteStatement stmt(final int connectionPtr, final int pointer) {
    return CONNECTIONS.getStatement(connectionPtr, pointer);
  }

  private static void rethrow(final String message, final SQLiteException e) {
    throw new android.database.sqlite.SQLiteException(message + ", base error code: " + e.getBaseErrorCode(), e);
  }

  @Implementation
  public static int nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    return CONNECTIONS.open(path);
  }

  @Implementation
  public static int nativePrepareStatement(int connectionPtr, String sql) {
    return CONNECTIONS.prepareStatement(connectionPtr, sql);
  }

  @Implementation
  public static void nativeClose(int connectionPtr) {
    CONNECTIONS.close(connectionPtr);
  }

  @Implementation
  public static void nativeFinalizeStatement(int connectionPtr, int statementPtr) {
    CONNECTIONS.finalizeStmt(connectionPtr, statementPtr);
  }

  @Implementation
  public static int nativeGetParameterCount(final int connectionPtr, final int statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return 0; } // TODO
    return CONNECTIONS.execute("get parameters count in prepared statement", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getBindParameterCount();
      }
    });
  }

  @Implementation
  public static boolean nativeIsReadOnly(final int connectionPtr, final int statementPtr) {
    if (statementPtr == IGNORED_REINDEX_STMT) { return true; } // TODO
    return CONNECTIONS.execute("call isReadOnly", new Callable<Boolean>() {
      @Override
      public Boolean call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.isReadOnly();
      }
    });
  }

  @Implementation
  public static long nativeExecuteForLong(final int connectionPtr, final int statementPtr) {
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

  @Implementation
  public static void nativeExecute(final int connectionPtr, final int statementPtr) {
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

  @Implementation
  public static String nativeExecuteForString(final int connectionPtr, final int statementPtr) {
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

  @Implementation
  public static int nativeGetColumnCount(final int connectionPtr, final int statementPtr) {
    return CONNECTIONS.execute("get columns count", new Callable<Integer>() {
      @Override
      public Integer call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.columnCount();
      }
    });
  }

  @Implementation
  public static String nativeGetColumnName(final int connectionPtr, final int statementPtr, final int index) {
    return CONNECTIONS.execute("get column name at index " + index, new Callable<String>() {
      @Override
      public String call() throws SQLiteException {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return stmt.getColumnName(index);
      }
    });
  }

  @Implementation
  public static void nativeBindNull(final int connectionPtr, final int statementPtr, final int index) {
    CONNECTIONS.execute("bind null at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bindNull(index);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeBindLong(final int connectionPtr, final int statementPtr, final int index, final long value) {
    CONNECTIONS.execute("bind long at index " + index + " with value " + value, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeBindDouble(final int connectionPtr, final int statementPtr, final int index, final double value) {
    CONNECTIONS.execute("bind double at index " + index + " with value " + value, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeBindString(final int connectionPtr, final int statementPtr, final int index, final String value) {
    CONNECTIONS.execute("bind string at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeBindBlob(final int connectionPtr, final int statementPtr, final int index, final byte[] value) {
    CONNECTIONS.execute("bind blob at index " + index, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.bind(index, value);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeRegisterLocalizedCollators(int connectionPtr, String locale) {
    // TODO: find a way to create a collator
    // http://www.sqlite.org/c3ref/create_collation.html
    // xerial jdbc driver does not have a Java method for sqlite3_create_collation
  }

  @Implementation
  public static int nativeExecuteForChangedRowCount(final int connectionPtr, final int statementPtr) {
    return CONNECTIONS.execute("execute for changed row count", new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return connection(connectionPtr).getChanges();
      }
    });
  }

  @Implementation
  public static long nativeExecuteForLastInsertedRowId(final int connectionPtr, final int statementPtr) {
    return CONNECTIONS.execute("execute for last inserted row ID", new Callable<Long>() {
      @Override
      public Long call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.stepThrough();
        return connection(connectionPtr).getLastInsertId();
      }
    });
  }

  @Implementation
  public static long nativeExecuteForCursorWindow(final int connectionPtr, final int statementPtr, final int windowPtr,
                                                  final int startPos, final int requiredPos, final boolean countAllRows) {

    return CONNECTIONS.execute("execute for cursor window", new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        return ShadowCursorWindow.setData(windowPtr, stmt);
      }
    });

  }

  @Implementation
  public static void nativeResetStatementAndClearBindings(final int connectionPtr, final int statementPtr) {
    CONNECTIONS.execute("reset statement", new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
        stmt.reset(true);
        return null;
      }
    });
  }

  @Implementation
  public static void nativeCancel(int connectionPtr) {
    CONNECTIONS.cancel(connectionPtr);
  }

  @Implementation
  public static void nativeResetCancel(int connectionPtr, boolean cancelable) {
    // handled in com.almworks.sqlite4java.SQLiteConnection#exec
  }

  @Implementation
  public static void nativeRegisterCustomFunction(int connectionPtr, SQLiteCustomFunction function) {
    // not supported
  }

  @Implementation
  public static int nativeExecuteForBlobFileDescriptor(int connectionPtr, int statementPtr) {
    // impossible to support without native code?
    return -1;
  }

  @Implementation
  public static int nativeGetDbLookaside(int connectionPtr) {
    // not supported by sqlite4java
    return 0;
  }


  private static class Connections {

    private final AtomicInteger pointerCounter = new AtomicInteger(0);

    private final Map<Integer, SQLiteStatement> statementsMap = new ConcurrentHashMap<Integer, SQLiteStatement>();
    private final Map<Integer, SQLiteConnection> connectionsMap = new ConcurrentHashMap<Integer, SQLiteConnection>();

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public SQLiteConnection getConnection(final int pointer) {
      SQLiteConnection connection = connectionsMap.get(pointer);
      if (connection == null) {
        throw new IllegalStateException("Illegal connection pointer " + pointer
            + ". Current pointers for thread " + Thread.currentThread() + " " + connectionsMap.keySet());
      }
      return connection;
    }

    public SQLiteStatement getStatement(final int connectionPtr, final int pointer) {
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

    public int open(final String path) {

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

      int ptr = pointerCounter.incrementAndGet();
      connectionsMap.put(ptr, dbConnection);
      return ptr;
    }

    public int prepareStatement(final int connectionPtr, final String sql) {
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

      int pointer = pointerCounter.incrementAndGet();
      statementsMap.put(pointer, stmt);
      return pointer;
    }

    public void close(final int ptr) {
      execute("close connection", new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          SQLiteConnection connection = getConnection(ptr);
          connection.dispose();
          return null;
        }
      });
    }

    public void finalizeStmt(final int connectionPtr, final int statementPtr) {
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

    public void cancel(int connectionPtr) {
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
      final T value;
      final Exception error;

      DbOperationResult(T value, Exception error) {
        this.value = value;
        this.error = error;
      }
    }
  }

}
