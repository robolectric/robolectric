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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shadows Android native SQLite connection.
 */
@Implements(android.database.sqlite.SQLiteConnection.class)
public class ShadowSQLiteConnection {

  private static final String IN_MEMORY_PATH = ":memory:";

  private static final AtomicInteger POINTER_COUNTER = new AtomicInteger(0);

  private static final ConcurrentHashMap<Integer, ConnectionData> CONNECTIONS_MAP = new ConcurrentHashMap<Integer, ConnectionData>();
  private static final ConcurrentHashMap<Integer, SQLiteStatement> STATEMENTS_MAP = new ConcurrentHashMap<Integer, SQLiteStatement>();

  static {
    SQLiteLibraryLoader.load();
  }

  private static SQLiteConnection connection(final int pointer) {
    ConnectionData data = CONNECTIONS_MAP.get(pointer);
    if (data == null) {
      throw new IllegalArgumentException("Illegal SQLite connection pointer: " + pointer + ". Current pointers: " + CONNECTIONS_MAP.keySet());
    }
    if (Thread.currentThread() != data.user) {
      throw new IllegalStateException("Connection is created in " + data.user + " but accessed in " + Thread.currentThread());
    }
    return data.connection;
  }

  private static SQLiteStatement stmt(final int connectionPtr, final int pointer) {
    // ensure connection is ok
    connection(connectionPtr);

    SQLiteStatement stmt = STATEMENTS_MAP.get(pointer);
    if (stmt == null) {
      throw new IllegalArgumentException("Invalid prepared statement pointer: " + pointer + ". Current pointers: " + STATEMENTS_MAP.keySet());
    }
    if (stmt.isDisposed()) {
      throw new IllegalStateException("Statement " + pointer + " " + stmt + " is disposed");
    }
    return stmt;
  }

  private static void rethrow(final String message, final SQLiteException e) {
    throw new android.database.sqlite.SQLiteException(message + ", base error code: " + e.getBaseErrorCode(), e);
  }

  @Implementation
  public static int nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    SQLiteConnection sqliteConnection = IN_MEMORY_PATH.equals(path)
        ? new SQLiteConnection()
        : new SQLiteConnection(new File(path));

    try {
      sqliteConnection.open();
    } catch (SQLiteException e) {
      rethrow("Cannot open SQLite connection", e);
    }

    int pointer = POINTER_COUNTER.incrementAndGet();
    CONNECTIONS_MAP.put(pointer, new ConnectionData(sqliteConnection, Thread.currentThread()));
    return pointer;
  }

  @Implementation
  public static int nativePrepareStatement(int connectionPtr, String sql) {
    // TODO: find a way to create collators
    if ("REINDEX LOCALIZED".equals(sql)) {
      return -2;
    }

    SQLiteConnection connection = connection(connectionPtr);
    try {
      SQLiteStatement stmt = connection.prepare(sql);
      int pointer = POINTER_COUNTER.incrementAndGet();
      STATEMENTS_MAP.put(pointer, stmt);
      return pointer;
    } catch (SQLiteException e) {
      rethrow("Cannot prepare statement " + sql, e);
      return 0;
    }
  }

  @Implementation
  public static void nativeClose(int connectionPtr) {
    SQLiteConnection connection = connection(connectionPtr);
    CONNECTIONS_MAP.remove(connectionPtr);
    connection.dispose();
  }

  @Implementation
  public static int nativeGetParameterCount(int connectionPtr, int statementPtr) {
    if (statementPtr == -2) { return 0; } // TODO
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      return stmt.getBindParameterCount();
    } catch (SQLiteException e) {
      rethrow("Cannot get parameters count in prepared statement", e);
      return 0;
    }
  }

  @Implementation
  public static boolean nativeIsReadOnly(int connectionPtr, int statementPtr) {
    if (statementPtr == -2) { return true; } // TODO
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      return stmt.isReadOnly();
    } catch (SQLiteException e) {
      rethrow("Cannot call isReadOnly", e);
      return false;
    }
  }

  @Implementation
  public static long nativeExecuteForLong(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      if (!stmt.step()) {
        throw new SQLiteDoneException();
      }
      return stmt.columnLong(0);
    } catch (SQLiteException e) {
      rethrow("Cannot execute for long", e);
      return -1;
    }
  }

  @Implementation
  public static void nativeExecute(int connectionPtr, int statementPtr) {
    if (statementPtr == -2) { return; }
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.stepThrough();
    } catch (SQLiteException e) {
      rethrow("Cannot execute", e);
    }
  }

  @Implementation
  public static String nativeExecuteForString(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      if (!stmt.step()) {
        throw new SQLiteDoneException();
      }
      return stmt.columnString(0);
    } catch (SQLiteException e) {
      rethrow("Cannot execute for string", e);
      return null;
    }
  }

  @Implementation
  public static void nativeFinalizeStatement(int connectionPtr, int statementPtr) {
    if (statementPtr == -2) { return; } // TODO
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    STATEMENTS_MAP.remove(statementPtr);
    stmt.dispose();
  }

  @Implementation
  public static int nativeGetColumnCount(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      return stmt.columnCount();
    } catch (SQLiteException e) {
      rethrow("Cannot get columns count", e);
      return 0;
    }
  }

  @Implementation
  public static String nativeGetColumnName(int connectionPtr, int statementPtr, int index) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      return stmt.getColumnName(index);
    } catch (SQLiteException e) {
      rethrow("Cannot get column name at index " + index, e);
      return null;
    }
  }

  @Implementation
  public static void nativeBindNull(int connectionPtr, int statementPtr, int index) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.bindNull(index);
    } catch (SQLiteException e) {
      rethrow("Cannot bind null at index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindLong(int connectionPtr, int statementPtr, int index, long value) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.bind(index, value);
    } catch (SQLiteException e) {
      rethrow("Cannot bind long at index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindDouble(int connectionPtr, int statementPtr, int index, double value) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.bind(index, value);
    } catch (SQLiteException e) {
      rethrow("Cannot bind double at index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindString(int connectionPtr, int statementPtr, int index, String value) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.bind(index, value);
    } catch (SQLiteException e) {
      rethrow("Cannot bind string at index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindBlob(int connectionPtr, int statementPtr, int index, byte[] value) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.bind(index, value);
    } catch (SQLiteException e) {
      rethrow("Cannot bind blob at index " + index, e);
    }
  }

  @Implementation
  public static void nativeRegisterLocalizedCollators(int connectionPtr, String locale) {
    // TODO: find a way to create a collator
    // http://www.sqlite.org/c3ref/create_collation.html
    // xerial jdbc driver does not have a Java method for sqlite3_create_collation
  }

  @Implementation
  public static int nativeExecuteForChangedRowCount(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.stepThrough();
      return connection(connectionPtr).getChanges();
    } catch (SQLiteException e) {
      rethrow("Cannot execute for changed row count", e);
      return 0;
    }
  }

  @Implementation
  public static long nativeExecuteForLastInsertedRowId(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.stepThrough();
      return connection(connectionPtr).getLastInsertId();
    } catch (SQLiteException e) {
      rethrow("Cannot execute for last inserted row ID", e);
      return 0;
    }
  }

  @Implementation
  public static long nativeExecuteForCursorWindow(int connectionPtr, int statementPtr, int windowPtr,
      int startPos, int requiredPos, boolean countAllRows) {

    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      ShadowCursorWindow.setData(windowPtr, stmt);
      return ShadowCursorWindow.getCount(windowPtr);
    } catch (SQLiteException e) {
      rethrow("Cannot execute for cursor window", e);
      return 0;
    }
  }

  @Implementation
  public static void nativeResetStatementAndClearBindings(int connectionPtr, int statementPtr) {
    SQLiteStatement stmt = stmt(connectionPtr, statementPtr);
    try {
      stmt.reset(true);
    } catch (SQLiteException e) {
      rethrow("Cannot reset statement", e);
    }
  }

   @Implementation
   public static void nativeCancel(int connectionPtr) {
     SQLiteStatement statement = STATEMENTS_MAP.get(POINTER_COUNTER.get());
     if (statement != null) {
        statement.cancel();
     }
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
   private static int nativeExecuteForBlobFileDescriptor(int connectionPtr, int statementPtr) {
     // impossible to support without native code?
     return -1;
   }

   @Implementation
   private static int nativeGetDbLookaside(int connectionPtr) {
     // not supported by litedb
     return 0;
   }


  private static class ConnectionData {
    final SQLiteConnection connection;
    final Thread user;

    public ConnectionData(SQLiteConnection connection, Thread user) {
      this.connection = connection;
      this.user = user;
    }
  }

}
