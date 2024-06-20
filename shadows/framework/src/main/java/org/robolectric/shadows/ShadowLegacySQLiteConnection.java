package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;

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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.util.SQLiteLibraryLoader;
import org.robolectric.util.PerfStatsCollector;

/** Shadow for {@link android.database.sqlite.SQLiteConnection} that is backed by sqlite4java. */
@Implements(value = android.database.sqlite.SQLiteConnection.class, isInAndroidSdk = false)
public class ShadowLegacySQLiteConnection extends ShadowSQLiteConnection {

  private static final String IN_MEMORY_PATH = ":memory:";
  private static final Connections CONNECTIONS = new Connections();
  private static final Pattern COLLATE_LOCALIZED_UNICODE_PATTERN =
      Pattern.compile("\\s+COLLATE\\s+(LOCALIZED|UNICODE)", Pattern.CASE_INSENSITIVE);

  // indicates an ignored statement
  private static final int IGNORED_REINDEX_STMT = -2;

  @Implementation(maxSdk = O)
  protected static long nativeOpen(
      String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    SQLiteLibraryLoader.load();
    return CONNECTIONS.open(path);
  }

  @Implementation(minSdk = O_MR1)
  protected static long nativeOpen(
      String path,
      int openFlags,
      String label,
      boolean enableTrace,
      boolean enableProfile,
      int lookasideSlotSize,
      int lookasideSlotCount) {
    return nativeOpen(path, openFlags, label, enableTrace, enableProfile);
  }

  @Implementation
  protected static long nativePrepareStatement(long connectionPtr, String sql) {
    final String newSql = convertSQLWithLocalizedUnicodeCollator(sql);
    return CONNECTIONS.prepareStatement(connectionPtr, newSql);
  }

  /** Convert SQL with phrase COLLATE LOCALIZED or COLLATE UNICODE to COLLATE NOCASE. */
  static String convertSQLWithLocalizedUnicodeCollator(String sql) {
    Matcher matcher = COLLATE_LOCALIZED_UNICODE_PATTERN.matcher(sql);
    return matcher.replaceAll(" COLLATE NOCASE");
  }

  @Resetter
  public static void reset() {
    CONNECTIONS.reset();
  }

  @Implementation
  protected static void nativeClose(long connectionPtr) {
    CONNECTIONS.close(connectionPtr);
  }

  @Implementation
  protected static void nativeFinalizeStatement(long connectionPtr, long statementPtr) {
    CONNECTIONS.finalizeStmt(connectionPtr, statementPtr);
  }

  @Implementation
  protected static int nativeGetParameterCount(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.getParameterCount(connectionPtr, statementPtr);
  }

  @Implementation
  protected static boolean nativeIsReadOnly(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.isReadOnly(connectionPtr, statementPtr);
  }

  @Implementation
  protected static long nativeExecuteForLong(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.executeForLong(connectionPtr, statementPtr);
  }

  @Implementation(maxSdk = S_V2)
  protected static void nativeExecute(final long connectionPtr, final long statementPtr) {
    CONNECTIONS.executeStatement(connectionPtr, statementPtr);
  }

  @Implementation(minSdk = TIRAMISU)
  protected static void nativeExecute(
      final long connectionPtr, final long statementPtr, boolean isPragmaStmt) {
    CONNECTIONS.executeStatement(connectionPtr, statementPtr);
  }

  @Implementation
  protected static String nativeExecuteForString(
      final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.executeForString(connectionPtr, statementPtr);
  }

  @Implementation
  protected static int nativeGetColumnCount(final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.getColumnCount(connectionPtr, statementPtr);
  }

  @Implementation
  protected static String nativeGetColumnName(
      final long connectionPtr, final long statementPtr, final int index) {
    return CONNECTIONS.getColumnName(connectionPtr, statementPtr, index);
  }

  @Implementation
  protected static void nativeBindNull(
      final long connectionPtr, final long statementPtr, final int index) {
    CONNECTIONS.bindNull(connectionPtr, statementPtr, index);
  }

  @Implementation
  protected static void nativeBindLong(
      final long connectionPtr, final long statementPtr, final int index, final long value) {
    CONNECTIONS.bindLong(connectionPtr, statementPtr, index, value);
  }

  @Implementation
  protected static void nativeBindDouble(
      final long connectionPtr, final long statementPtr, final int index, final double value) {
    CONNECTIONS.bindDouble(connectionPtr, statementPtr, index, value);
  }

  @Implementation
  protected static void nativeBindString(
      final long connectionPtr, final long statementPtr, final int index, final String value) {
    CONNECTIONS.bindString(connectionPtr, statementPtr, index, value);
  }

  @Implementation
  protected static void nativeBindBlob(
      final long connectionPtr, final long statementPtr, final int index, final byte[] value) {
    CONNECTIONS.bindBlob(connectionPtr, statementPtr, index, value);
  }

  @Implementation
  protected static void nativeRegisterLocalizedCollators(long connectionPtr, String locale) {
    // TODO: find a way to create a collator
    // http://www.sqlite.org/c3ref/create_collation.html
    // xerial jdbc driver does not have a Java method for sqlite3_create_collation
  }

  @Implementation
  protected static int nativeExecuteForChangedRowCount(
      final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.executeForChangedRowCount(connectionPtr, statementPtr);
  }

  @Implementation
  protected static long nativeExecuteForLastInsertedRowId(
      final long connectionPtr, final long statementPtr) {
    return CONNECTIONS.executeForLastInsertedRowId(connectionPtr, statementPtr);
  }

  @Implementation
  protected static long nativeExecuteForCursorWindow(
      final long connectionPtr,
      final long statementPtr,
      final long windowPtr,
      final int startPos,
      final int requiredPos,
      final boolean countAllRows) {
    return CONNECTIONS.executeForCursorWindow(connectionPtr, statementPtr, windowPtr);
  }

  @Implementation
  protected static void nativeResetStatementAndClearBindings(
      final long connectionPtr, final long statementPtr) {
    CONNECTIONS.resetStatementAndClearBindings(connectionPtr, statementPtr);
  }

  @Implementation
  protected static void nativeCancel(long connectionPtr) {
    CONNECTIONS.cancel(connectionPtr);
  }

  @Implementation
  protected static void nativeResetCancel(long connectionPtr, boolean cancelable) {
    // handled in com.almworks.sqlite4java.SQLiteConnection#exec
  }

  @Implementation(maxSdk = Q)
  protected static void nativeRegisterCustomFunction(
      long connectionPtr, SQLiteCustomFunction function) {
    // not supported
  }

  @Implementation
  protected static int nativeExecuteForBlobFileDescriptor(long connectionPtr, long statementPtr) {
    // impossible to support without native code?
    return -1;
  }

  @Implementation
  protected static int nativeGetDbLookaside(long connectionPtr) {
    // not supported by sqlite4java
    return 0;
  }

  // VisibleForTesting
  static class Connections {

    private final Object lock = new Object();
    private final AtomicLong pointerCounter = new AtomicLong(0);
    private final Map<Long, SQLiteStatement> statementsMap = new HashMap<>();
    private final Map<Long, SQLiteConnection> connectionsMap = new HashMap<>();
    private final Map<Long, List<Long>> statementPtrsForConnection = new HashMap<>();

    private ExecutorService dbExecutor = Executors.newSingleThreadExecutor(threadFactory());

    static ThreadFactory threadFactory() {
      ThreadFactory delegate = Executors.defaultThreadFactory();
      return r -> {
        Thread worker = delegate.newThread(r);
        worker.setName(ShadowLegacySQLiteConnection.class.getSimpleName() + " worker");
        return worker;
      };
    }

    SQLiteConnection getConnection(final long connectionPtr) {
      synchronized (lock) {
        final SQLiteConnection connection = connectionsMap.get(connectionPtr);
        if (connection == null) {
          throw new IllegalStateException(
              "Illegal connection pointer "
                  + connectionPtr
                  + ". Current pointers for thread "
                  + Thread.currentThread()
                  + " "
                  + connectionsMap.keySet());
        }
        return connection;
      }
    }

    SQLiteStatement getStatement(final long connectionPtr, final long statementPtr) {
      synchronized (lock) {
        // ensure connection is ok
        getConnection(connectionPtr);

        final SQLiteStatement statement = statementsMap.get(statementPtr);
        if (statement == null) {
          throw new IllegalArgumentException(
              "Invalid prepared statement pointer: "
                  + statementPtr
                  + ". Current pointers: "
                  + statementsMap.keySet());
        }
        if (statement.isDisposed()) {
          throw new IllegalStateException(
              "Statement " + statementPtr + " " + statement + " is disposed");
        }
        return statement;
      }
    }

    long open(final String path) {
      synchronized (lock) {
        final SQLiteConnection dbConnection =
            execute(
                new Callable<SQLiteConnection>() {
                  @Override
                  public SQLiteConnection call() throws Exception {
                    SQLiteConnection connection =
                        useInMemoryDatabase.get() || IN_MEMORY_PATH.equals(path)
                            ? new SQLiteConnection()
                            : new SQLiteConnection(new File(path));

                    connection.open();
                    return connection;
                  }
                });

        final long connectionPtr = pointerCounter.incrementAndGet();
        connectionsMap.put(connectionPtr, dbConnection);
        statementPtrsForConnection.put(connectionPtr, new ArrayList<>());
        return connectionPtr;
      }
    }

    long prepareStatement(final long connectionPtr, final String sql) {
      // TODO: find a way to create collators
      if ("REINDEX LOCALIZED".equals(sql)) {
        return IGNORED_REINDEX_STMT;
      }

      synchronized (lock) {
        final SQLiteConnection connection = getConnection(connectionPtr);
        final SQLiteStatement statement =
            execute(
                new Callable<SQLiteStatement>() {
                  @Override
                  public SQLiteStatement call() throws Exception {
                    return connection.prepare(sql);
                  }
                });

        final long statementPtr = pointerCounter.incrementAndGet();
        statementsMap.put(statementPtr, statement);
        statementPtrsForConnection.get(connectionPtr).add(statementPtr);
        return statementPtr;
      }
    }

    void close(final long connectionPtr) {
      synchronized (lock) {
        final SQLiteConnection connection = getConnection(connectionPtr);
        execute(
            () -> {
              connection.dispose();
              return null;
            });
        connectionsMap.remove(connectionPtr);
        statementPtrsForConnection.remove(connectionPtr);
      }
    }

    void reset() {
      ExecutorService oldDbExecutor;
      Collection<SQLiteConnection> openConnections;

      synchronized (lock) {
        oldDbExecutor = dbExecutor;
        openConnections = new ArrayList<>(connectionsMap.values());

        dbExecutor = Executors.newSingleThreadExecutor(threadFactory());
        connectionsMap.clear();
        statementsMap.clear();
        statementPtrsForConnection.clear();
      }

      shutdownDbExecutor(oldDbExecutor, openConnections);
    }

    private static void shutdownDbExecutor(
        ExecutorService executorService, Collection<SQLiteConnection> connections) {
      for (final SQLiteConnection connection : connections) {
        getFuture(
            executorService.submit(
                () -> {
                  connection.dispose();
                  return null;
                }));
      }

      executorService.shutdown();
      try {
        executorService.awaitTermination(30, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    void finalizeStmt(final long connectionPtr, final long statementPtr) {
      if (statementPtr == IGNORED_REINDEX_STMT) {
        return;
      }

      synchronized (lock) {
        final SQLiteStatement statement = getStatement(connectionPtr, statementPtr);
        statementsMap.remove(statementPtr);

        execute(
            () -> {
              statement.dispose();
              return null;
            });
      }
    }

    void cancel(final long connectionPtr) {
      synchronized (lock) {
        getConnection(connectionPtr); // check connection

        for (Long statementPtr : statementPtrsForConnection.get(connectionPtr)) {
          final SQLiteStatement statement = statementsMap.get(statementPtr);
          if (statement != null) {
            execute(
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    statement.cancel();
                    return null;
                  }
                });
          }
        }
      }
    }

    int getParameterCount(final long connectionPtr, final long statementPtr) {
      if (statementPtr == IGNORED_REINDEX_STMT) {
        return 0;
      }

      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Integer>() {
            @Override
            public Integer call(final SQLiteStatement statement) throws Exception {
              return statement.getBindParameterCount();
            }
          });
    }

    boolean isReadOnly(final long connectionPtr, final long statementPtr) {
      if (statementPtr == IGNORED_REINDEX_STMT) {
        return true;
      }

      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Boolean>() {
            @Override
            public Boolean call(final SQLiteStatement statement) throws Exception {
              return statement.isReadOnly();
            }
          });
    }

    long executeForLong(final long connectionPtr, final long statementPtr) {
      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Long>() {
            @Override
            public Long call(final SQLiteStatement statement) throws Exception {
              if (!statement.step()) {
                throw new SQLiteException(
                    SQLiteConstants.SQLITE_DONE, "No rows returned from query");
              }
              return statement.columnLong(0);
            }
          });
    }

    void executeStatement(final long connectionPtr, final long statementPtr) {
      if (statementPtr == IGNORED_REINDEX_STMT) {
        return;
      }

      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.stepThrough();
              return null;
            }
          });
    }

    String executeForString(final long connectionPtr, final long statementPtr) {
      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<String>() {
            @Override
            public String call(final SQLiteStatement statement) throws Exception {
              if (!statement.step()) {
                throw new SQLiteException(
                    SQLiteConstants.SQLITE_DONE, "No rows returned from query");
              }
              return statement.columnString(0);
            }
          });
    }

    int getColumnCount(final long connectionPtr, final long statementPtr) {
      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Integer>() {
            @Override
            public Integer call(final SQLiteStatement statement) throws Exception {
              return statement.columnCount();
            }
          });
    }

    String getColumnName(final long connectionPtr, final long statementPtr, final int index) {
      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<String>() {
            @Override
            public String call(final SQLiteStatement statement) throws Exception {
              return statement.getColumnName(index);
            }
          });
    }

    void bindNull(final long connectionPtr, final long statementPtr, final int index) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.bindNull(index);
              return null;
            }
          });
    }

    void bindLong(
        final long connectionPtr, final long statementPtr, final int index, final long value) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.bind(index, value);
              return null;
            }
          });
    }

    void bindDouble(
        final long connectionPtr, final long statementPtr, final int index, final double value) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.bind(index, value);
              return null;
            }
          });
    }

    void bindString(
        final long connectionPtr, final long statementPtr, final int index, final String value) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.bind(index, value);
              return null;
            }
          });
    }

    void bindBlob(
        final long connectionPtr, final long statementPtr, final int index, final byte[] value) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.bind(index, value);
              return null;
            }
          });
    }

    int executeForChangedRowCount(final long connectionPtr, final long statementPtr) {
      synchronized (lock) {
        final SQLiteConnection connection = getConnection(connectionPtr);
        final SQLiteStatement statement = getStatement(connectionPtr, statementPtr);

        return execute(
            new Callable<Integer>() {
              @Override
              public Integer call() throws Exception {
                if (statement.step()) {
                  throw new android.database.sqlite.SQLiteException(
                      "Queries can be performed using SQLiteDatabase query or rawQuery methods"
                          + " only.");
                }
                return connection.getChanges();
              }
            });
      }
    }

    long executeForLastInsertedRowId(final long connectionPtr, final long statementPtr) {
      synchronized (lock) {
        final SQLiteConnection connection = getConnection(connectionPtr);
        final SQLiteStatement statement = getStatement(connectionPtr, statementPtr);

        return execute(
            new Callable<Long>() {
              @Override
              public Long call() throws Exception {
                statement.stepThrough();
                return connection.getChanges() > 0 ? connection.getLastInsertId() : -1L;
              }
            });
      }
    }

    long executeForCursorWindow(
        final long connectionPtr, final long statementPtr, final long windowPtr) {
      return executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Integer>() {
            @Override
            public Integer call(final SQLiteStatement statement) throws Exception {
              return ShadowLegacyCursorWindow.setData(windowPtr, statement);
            }
          });
    }

    void resetStatementAndClearBindings(final long connectionPtr, final long statementPtr) {
      executeStatementOperation(
          connectionPtr,
          statementPtr,
          new StatementOperation<Void>() {
            @Override
            public Void call(final SQLiteStatement statement) throws Exception {
              statement.reset(true);
              return null;
            }
          });
    }

    interface StatementOperation<T> {
      T call(final SQLiteStatement statement) throws Exception;
    }

    private <T> T executeStatementOperation(
        final long connectionPtr,
        final long statementPtr,
        final StatementOperation<T> statementOperation) {
      synchronized (lock) {
        final SQLiteStatement statement = getStatement(connectionPtr, statementPtr);
        return execute(
            () -> {
              return statementOperation.call(statement);
            });
      }
    }

    /**
     * Any Callable passed in to execute must not synchronize on lock, as this will result in a
     * deadlock
     */
    private <T> T execute(final Callable<T> work) {
      synchronized (lock) {
        return PerfStatsCollector.getInstance()
            .measure("sqlite", () -> getFuture(dbExecutor.submit(work)));
      }
    }

    private static <T> T getFuture(final Future<T> future) {
      try {
        return Uninterruptibles.getUninterruptibly(future);
        // No need to catch cancellationexception - we never cancel these futures
      } catch (ExecutionException e) {
        Throwable t = e.getCause();
        if (t instanceof SQLiteException) {
          SQLiteException sqliteException = (SQLiteException) t;
          final RuntimeException sqlException =
              getSqliteException(sqliteException.getMessage(), sqliteException.getErrorCode());
          sqlException.initCause(e);
          throw sqlException;
        } else if (t instanceof android.database.sqlite.SQLiteException) {
          throw (android.database.sqlite.SQLiteException) t;
        } else {
          throw new RuntimeException(e);
        }
      }
    }

    // These are from android_database_SQLiteCommon.cpp
    private static final ImmutableMap<Integer, String> ERROR_CODE_MAP =
        new ImmutableMap.Builder<Integer, String>()
            .put(4, "SQLITE_ABORT")
            .put(23, "SQLITE_AUTH")
            .put(5, "SQLITE_BUSY")
            .put(14, "SQLITE_CANTOPEN")
            .put(19, "SQLITE_CONSTRAINT")
            .put(11, "SQLITE_CORRUPT")
            .put(101, "SQLITE_DONE")
            .put(16, "SQLITE_EMPTY")
            .put(1, "SQLITE_ERROR")
            .put(24, "SQLITE_FORMAT")
            .put(13, "SQLITE_FULL")
            .put(2, "SQLITE_INTERNAL")
            .put(9, "SQLITE_INTERRUPT")
            .put(10, "SQLITE_IOERR")
            .put(6, "SQLITE_LOCKED")
            .put(20, "SQLITE_MISMATCH")
            .put(21, "SQLITE_MISUSE")
            .put(22, "SQLITE_NOLFS")
            .put(7, "SQLITE_NOMEM")
            .put(26, "SQLITE_NOTADB")
            .put(12, "SQLITE_NOTFOUND")
            .put(27, "SQLITE_NOTICE")
            .put(0, "SQLITE_OK")
            .put(3, "SQLITE_PERM")
            .put(15, "SQLITE_PROTOCOL")
            .put(25, "SQLITE_RANGE")
            .put(8, "SQLITE_READONLY")
            .put(100, "SQLITE_ROW")
            .put(17, "SQLITE_SCHEMA")
            .put(18, "SQLITE_TOOBIG")
            .put(28, "SQLITE_WARNING")
            // Extended Result Code List
            .put(516, "SQLITE_ABORT_ROLLBACK")
            .put(261, "SQLITE_BUSY_RECOVERY")
            .put(517, "SQLITE_BUSY_SNAPSHOT")
            .put(1038, "SQLITE_CANTOPEN_CONVPATH")
            .put(782, "SQLITE_CANTOPEN_FULLPATH")
            .put(526, "SQLITE_CANTOPEN_ISDIR")
            .put(270, "SQLITE_CANTOPEN_NOTEMPDIR")
            .put(275, "SQLITE_CONSTRAINT_CHECK")
            .put(531, "SQLITE_CONSTRAINT_COMMITHOOK")
            .put(787, "SQLITE_CONSTRAINT_FOREIGNKEY")
            .put(1043, "SQLITE_CONSTRAINT_FUNCTION")
            .put(1299, "SQLITE_CONSTRAINT_NOTNULL")
            .put(1555, "SQLITE_CONSTRAINT_PRIMARYKEY")
            .put(2579, "SQLITE_CONSTRAINT_ROWID")
            .put(1811, "SQLITE_CONSTRAINT_TRIGGER")
            .put(2067, "SQLITE_CONSTRAINT_UNIQUE")
            .put(2323, "SQLITE_CONSTRAINT_VTAB")
            .put(267, "SQLITE_CORRUPT_VTAB")
            .put(3338, "SQLITE_IOERR_ACCESS")
            .put(2826, "SQLITE_IOERR_BLOCKED")
            .put(3594, "SQLITE_IOERR_CHECKRESERVEDLOCK")
            .put(4106, "SQLITE_IOERR_CLOSE")
            .put(6666, "SQLITE_IOERR_CONVPATH")
            .put(2570, "SQLITE_IOERR_DELETE")
            .put(5898, "SQLITE_IOERR_DELETE_NOENT")
            .put(4362, "SQLITE_IOERR_DIR_CLOSE")
            .put(1290, "SQLITE_IOERR_DIR_FSYNC")
            .put(1802, "SQLITE_IOERR_FSTAT")
            .put(1034, "SQLITE_IOERR_FSYNC")
            .put(6410, "SQLITE_IOERR_GETTEMPPATH")
            .put(3850, "SQLITE_IOERR_LOCK")
            .put(6154, "SQLITE_IOERR_MMAP")
            .put(3082, "SQLITE_IOERR_NOMEM")
            .put(2314, "SQLITE_IOERR_RDLOCK")
            .put(266, "SQLITE_IOERR_READ")
            .put(5642, "SQLITE_IOERR_SEEK")
            .put(5130, "SQLITE_IOERR_SHMLOCK")
            .put(5386, "SQLITE_IOERR_SHMMAP")
            .put(4618, "SQLITE_IOERR_SHMOPEN")
            .put(4874, "SQLITE_IOERR_SHMSIZE")
            .put(522, "SQLITE_IOERR_SHORT_READ")
            .put(1546, "SQLITE_IOERR_TRUNCATE")
            .put(2058, "SQLITE_IOERR_UNLOCK")
            .put(778, "SQLITE_IOERR_WRITE")
            .put(262, "SQLITE_LOCKED_SHAREDCACHE")
            .put(539, "SQLITE_NOTICE_RECOVER_ROLLBACK")
            .put(283, "SQLITE_NOTICE_RECOVER_WAL")
            .put(256, "SQLITE_OK_LOAD_PERMANENTLY")
            .put(520, "SQLITE_READONLY_CANTLOCK")
            .put(1032, "SQLITE_READONLY_DBMOVED")
            .put(264, "SQLITE_READONLY_RECOVERY")
            .put(776, "SQLITE_READONLY_ROLLBACK")
            .put(284, "SQLITE_WARNING_AUTOINDEX")
            .build();

    private static RuntimeException getSqliteException(
        final String sqliteErrorMessage, final int errorCode) {
      final int baseErrorCode = errorCode & 0xff;
      // Remove redundant error code prefix from sqlite4java. The error code is added
      // as a suffix below.
      String errorMessageWithoutCode = sqliteErrorMessage.replaceAll("^\\[\\d+\\] ?", "");
      StringBuilder fullMessage = new StringBuilder(errorMessageWithoutCode);
      fullMessage.append(" (code ");
      fullMessage.append(errorCode);
      String errorCodeMessage = ERROR_CODE_MAP.getOrDefault(errorCode, "");
      if (MoreObjects.firstNonNull(errorCodeMessage, "").length() > 0) {
        fullMessage.append(" ").append(errorCodeMessage);
      }
      fullMessage.append(")");
      String message = fullMessage.toString();
      // Mapping is from throw_sqlite3_exception in android_database_SQLiteCommon.cpp
      switch (baseErrorCode) {
        case SQLiteConstants.SQLITE_ABORT:
          return new SQLiteAbortException(message);
        case SQLiteConstants.SQLITE_PERM:
          return new SQLiteAccessPermException(message);
        case SQLiteConstants.SQLITE_RANGE:
          return new SQLiteBindOrColumnIndexOutOfRangeException(message);
        case SQLiteConstants.SQLITE_TOOBIG:
          return new SQLiteBlobTooBigException(message);
        case SQLiteConstants.SQLITE_CANTOPEN:
          return new SQLiteCantOpenDatabaseException(message);
        case SQLiteConstants.SQLITE_CONSTRAINT:
          return new SQLiteConstraintException(message);
        case SQLiteConstants.SQLITE_NOTADB: // fall through
        case SQLiteConstants.SQLITE_CORRUPT:
          return new SQLiteDatabaseCorruptException(message);
        case SQLiteConstants.SQLITE_BUSY:
          return new SQLiteDatabaseLockedException(message);
        case SQLiteConstants.SQLITE_MISMATCH:
          return new SQLiteDatatypeMismatchException(message);
        case SQLiteConstants.SQLITE_IOERR:
          return new SQLiteDiskIOException(message);
        case SQLiteConstants.SQLITE_DONE:
          return new SQLiteDoneException(message);
        case SQLiteConstants.SQLITE_FULL:
          return new SQLiteFullException(message);
        case SQLiteConstants.SQLITE_MISUSE:
          return new SQLiteMisuseException(message);
        case SQLiteConstants.SQLITE_NOMEM:
          return new SQLiteOutOfMemoryException(message);
        case SQLiteConstants.SQLITE_READONLY:
          return new SQLiteReadOnlyDatabaseException(message);
        case SQLiteConstants.SQLITE_LOCKED:
          return new SQLiteTableLockedException(message);
        case SQLiteConstants.SQLITE_INTERRUPT:
          return new OperationCanceledException(message);
        default:
          return new android.database.sqlite.SQLiteException(
              message + ", base error code: " + baseErrorCode);
      }
    }
  }
}
