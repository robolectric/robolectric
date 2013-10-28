package org.robolectric.shadows;

import android.database.sqlite.SQLiteConnection;
import android.database.sqlite.SQLiteCustomFunction;
import android.database.sqlite.SQLiteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.DatabaseConfig;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shadows Android native SQLite connection with JDBC.
 */
@Implements(SQLiteConnection.class)
public class ShadowSQLiteConnection {

  private static final String IN_MEMORY_PATH = ":memory:";

  /** Statements with such pointer must be ignored. */
  private static final int PTR_IGNORED_STMT = -2;

  private static final StmtRecord IGNORED_STATEMENT = new StmtRecord(new IgnoredStatement(), "ignore");

  private static final AtomicInteger POINTER_COUNTER = new AtomicInteger(0);

  private static final ConcurrentHashMap<Integer, Connection> CONNECTIONS_MAP = new ConcurrentHashMap<Integer, Connection>();
  private static final ConcurrentHashMap<Integer, StmtRecord> STATEMENTS_MAP = new ConcurrentHashMap<Integer, StmtRecord>();


  private static Connection connection(final int pointer) {
    Connection jdbcConnection = CONNECTIONS_MAP.get(pointer);
    if (jdbcConnection == null) {
      throw new IllegalArgumentException("Illegal JDBC connection pointer: " + pointer + ". Current pointers: " + CONNECTIONS_MAP.keySet());
    }
    return jdbcConnection;
  }

  /**
   * @param pointer statement pointer
   * @return statement instance or null if statement must be ignored
   * @throws IllegalArgumentException if pointer is invalid
   */
  private static StmtRecord stmtRecord(final int pointer) {
    if (pointer == PTR_IGNORED_STMT) { return IGNORED_STATEMENT; }
    StmtRecord stmt = STATEMENTS_MAP.get(pointer);
    if (stmt == null) {
      throw new IllegalArgumentException("Invalid prepared statement pointer: " + pointer + ". Current pointers: " + STATEMENTS_MAP.keySet());
    }
    return stmt;
  }

  @Implementation
  public static int nativeOpen(String path, int openFlags, String label, boolean enableTrace, boolean enableProfile) {
    Connection jdbcConnection = IN_MEMORY_PATH.equals(path)
        ? DatabaseConfig.getMemoryConnection()
        : DatabaseConfig.getFileConnection(new File(path));

    if (jdbcConnection == null) {
      throw new SQLiteException("Cannot open JDBC connection");
    }

    int pointer = POINTER_COUNTER.incrementAndGet();
    CONNECTIONS_MAP.put(pointer, jdbcConnection);
    return pointer;
  }

  @Implementation
  public static int nativePrepareStatement(int connectionPtr, String sql) {
    if ("REINDEX LOCALIZED".equals(sql.toUpperCase(Locale.ROOT))) {
      // see #nativeRegisterLocalizedCollators
      return PTR_IGNORED_STMT;
    }

    Connection jdbcConnection = connection(connectionPtr);
    try {
      final PreparedStatement stmt = jdbcConnection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
      int pointer = POINTER_COUNTER.incrementAndGet();
      STATEMENTS_MAP.put(pointer, new StmtRecord(stmt, sql.trim()));
      return pointer;
    } catch (SQLException e) {
      throw new SQLiteException("Cannot prepare statement " + sql, e);
    }
  }

  @Implementation
  public static void nativeClose(int connectionPtr) {
    Connection jdbcConnection = connection(connectionPtr);
    try {
      jdbcConnection.close();
    } catch (SQLException e) {
      throw new SQLiteException("Cannot close JDBC connection", e);
    }
  }

  @Implementation
  public static int nativeGetParameterCount(int connectionPtr, int statementPtr) {
    PreparedStatement stmt = stmtRecord(statementPtr).stmt;
    try {
      return stmt.getParameterMetaData().getParameterCount();
    } catch (SQLException e) {
      throw new SQLiteException("Cannot get parameters count in prepared statement", e);
    }
  }

  @Implementation
  public static boolean nativeIsReadOnly(int connectionPtr, int statementPtr) {
    // TODO: cannot figure out how to check it in other way
    String sql = stmtRecord(statementPtr).sql;
    final int commandLen = 3;
    String command = sql.substring(0, commandLen).toUpperCase(Locale.ROOT);
    return !"UPD".equals(command) && !"DEL".equals(command) && !"INS".equals(command);
  }

  @Implementation
  public static long nativeExecuteForLong(int connectionPtr, int statementPtr) {
    StmtRecord stmtRecord = stmtRecord(statementPtr);
    try {
      ResultSet result = stmtRecord.stmt.executeQuery();
      if (!result.next()) {
        throw new SQLiteException("Cannot get long result for " + stmtRecord.sql);
      }
      return result.getLong(1);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute for long", e);
    }
  }

  @Implementation
  public static void nativeExecute(int connectionPtr, int statementPtr) {
    StmtRecord stmtRecord = stmtRecord(statementPtr);
    try {
      stmtRecord.stmt.execute();
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute", e);
    }
  }

  @Implementation
  public static String nativeExecuteForString(int connectionPtr, int statementPtr) {
    StmtRecord stmtRecord = stmtRecord(statementPtr);
    try {
      ResultSet result = stmtRecord.stmt.executeQuery();
      if (!result.next()) {
        throw new SQLiteException("Cannot get string result for " + stmtRecord.sql);
      }
      return result.getString(1);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute for string", e);
    }
  }

  @Implementation
  public static void nativeFinalizeStatement(int connectionPtr, int statementPtr) {
    try {
      stmtRecord(statementPtr).stmt.close();
      STATEMENTS_MAP.remove(statementPtr);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot close prepared statement", e);
    }
  }

  @Implementation
  public static int nativeGetColumnCount(int connectionPtr, int statementPtr) {
    // TODO: in order to implement it in a correct way we should switch from JDBC to some SQLite Java wrapper
    String sql = stmtRecord(statementPtr).sql.toUpperCase(Locale.ROOT);
    if (!sql.startsWith("SELECT")) {
      return 0;
    }
    int fromIndex = sql.indexOf("FROM");
    return sql.substring(0, fromIndex != -1 ? fromIndex : sql.length()).split(",").length;
  }

  @Implementation
  public static String nativeGetColumnName(int connectionPtr, int statementPtr, int index) {
    try {
      return stmtRecord(statementPtr).stmt.getMetaData().getColumnName(index + 1);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot get column name for index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindNull(int connectionPtr, int statementPtr, int index) {
    PreparedStatement stmt = stmtRecord(statementPtr).stmt;
    try {
      stmt.setNull(index, stmt.getParameterMetaData().getParameterType(index));
    } catch (SQLException e) {
      throw new SQLiteException("Cannot bind null for index " + index, e);
    }
  }

  @Implementation
  public static void nativeBindLong(int connectionPtr, int statementPtr, int index, long value) {
    try {
      stmtRecord(statementPtr).stmt.setLong(index, value);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot bind long " + value + " for index" + index, e);
    }
  }

  @Implementation
  public static void nativeBindDouble(int connectionPtr, int statementPtr, int index, double value) {
    try {
      stmtRecord(statementPtr).stmt.setDouble(index, value);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot bind double " + value + " for index" + index, e);
    }
  }

  @Implementation
  public static void nativeBindString(int connectionPtr, int statementPtr, int index, String value) {
    try {
      stmtRecord(statementPtr).stmt.setString(index, value);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot bind string '" + value + "' for index" + index, e);
    }
  }

  @Implementation
  public static void nativeBindBlob(int connectionPtr, int statementPtr, int index, byte[] value) {
    try {
      stmtRecord(statementPtr).stmt.setBytes(index, value);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot bind blob of length " + value.length + " for index" + index, e);
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
    PreparedStatement stmt = stmtRecord(statementPtr).stmt;
    try {
      return stmt.executeUpdate();
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute for changed row count", e);
    }
  }

  @Implementation
  public static long nativeExecuteForLastInsertedRowId(int connectionPtr, int statementPtr) {
    PreparedStatement stmt = stmtRecord(statementPtr).stmt;
    try {
      stmt.execute();
      ResultSet keys = stmt.getGeneratedKeys();
      keys.next();
      return keys.getLong(1);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute for last inserted row ID", e);
    }
  }

  @Implementation
  public static long nativeExecuteForCursorWindow(int connectionPtr, int statementPtr, int windowPtr,
      int startPos, int requiredPos, boolean countAllRows) {
    PreparedStatement stmt = stmtRecord(statementPtr).stmt;

    if (ShadowCursorWindow.hasResultSetFor(windowPtr)) {
      return ShadowCursorWindow.getCount(windowPtr);
    }

    try {
      ShadowCursorWindow.setData(windowPtr, stmt.executeQuery());
      return ShadowCursorWindow.getCount(windowPtr);
    } catch (SQLException e) {
      throw new SQLiteException("Cannot execute for cursor window", e);
    }
  }

  private static native void nativeRegisterCustomFunction(int connectionPtr,
                                                          SQLiteCustomFunction function);
  private static native void nativeResetStatementAndClearBindings(
      int connectionPtr, int statementPtr);
  private static native int nativeExecuteForBlobFileDescriptor(
      int connectionPtr, int statementPtr);
  private static native int nativeGetDbLookaside(int connectionPtr);
  private static native void nativeCancel(int connectionPtr);
  private static native void nativeResetCancel(int connectionPtr, boolean cancelable);

  private static class StmtRecord {
    final PreparedStatement stmt;
    final String sql;

    public StmtRecord(final PreparedStatement stmt, final String sql) {
      this.stmt = stmt;
      this.sql = sql;
    }
  }

  private static class IgnoredStatement implements PreparedStatement {
    @Override
    public ResultSet executeQuery() throws SQLException {
      return null;
    }
    @Override
    public int executeUpdate() throws SQLException {
      return 0;
    }
    @Override
    public void setNull(int i, int i2) throws SQLException { }
    @Override
    public void setBoolean(int i, boolean b) throws SQLException { }
    @Override
    public void setByte(int i, byte b) throws SQLException { }
    @Override
    public void setShort(int i, short i2) throws SQLException { }
    @Override
    public void setInt(int i, int i2) throws SQLException { }
    @Override
    public void setLong(int i, long l) throws SQLException { }
    @Override
    public void setFloat(int i, float v) throws SQLException { }
    @Override
    public void setDouble(int i, double v) throws SQLException { }
    @Override
    public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException { }
    @Override
    public void setString(int i, String s) throws SQLException { }
    @Override
    public void setBytes(int i, byte[] bytes) throws SQLException { }
    @Override
    public void setDate(int i, Date date) throws SQLException { }
    @Override
    public void setTime(int i, Time time) throws SQLException { }
    @Override
    public void setTimestamp(int i, Timestamp timestamp) throws SQLException { }
    @Override
    public void setAsciiStream(int i, InputStream inputStream, int i2) throws SQLException { }
    @Override
    public void setUnicodeStream(int i, InputStream inputStream, int i2) throws SQLException { }
    @Override
    public void setBinaryStream(int i, InputStream inputStream, int i2) throws SQLException { }
    @Override
    public void clearParameters() throws SQLException { }
    @Override
    public void setObject(int i, Object o, int i2) throws SQLException { }
    @Override
    public void setObject(int i, Object o) throws SQLException { }
    @Override
    public boolean execute() throws SQLException {
      return false;
    }
    @Override
    public void addBatch() throws SQLException { }
    @Override
    public void setCharacterStream(int i, Reader reader, int i2) throws SQLException { }
    @Override
    public void setRef(int i, Ref ref) throws SQLException { }
    @Override
    public void setBlob(int i, Blob blob) throws SQLException { }
    @Override
    public void setClob(int i, Clob clob) throws SQLException { }
    @Override
    public void setArray(int i, Array array) throws SQLException { }
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
      return null;
    }
    @Override
    public void setDate(int i, Date date, Calendar calendar) throws SQLException { }
    @Override
    public void setTime(int i, Time time, Calendar calendar) throws SQLException { }
    @Override
    public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException { }
    @Override
    public void setNull(int i, int i2, String s) throws SQLException { }
    @Override
    public void setURL(int i, URL url) throws SQLException { }
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
      return new ParameterMetaData() {
        @Override
        public int getParameterCount() throws SQLException {
          return 0;
        }
        @Override
        public int isNullable(int i) throws SQLException {
          return 0;
        }
        @Override
        public boolean isSigned(int i) throws SQLException {
          return false;
        }
        @Override
        public int getPrecision(int i) throws SQLException {
          return 0;
        }
        @Override
        public int getScale(int i) throws SQLException {
          return 0;
        }
        @Override
        public int getParameterType(int i) throws SQLException {
          return 0;
        }
        @Override
        public String getParameterTypeName(int i) throws SQLException {
          return null;
        }
        @Override
        public String getParameterClassName(int i) throws SQLException {
          return null;
        }

        @Override
        public int getParameterMode(int i) throws SQLException {
          return 0;
        }

        @Override
        public <T> T unwrap(Class<T> tClass) throws SQLException {
          return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> aClass) throws SQLException {
          return false;
        }
      };
    }
    @Override
    public void setRowId(int i, RowId rowId) throws SQLException { }
    @Override
    public void setNString(int i, String s) throws SQLException { }
    @Override
    public void setNCharacterStream(int i, Reader reader, long l) throws SQLException { }
    @Override
    public void setNClob(int i, NClob nClob) throws SQLException { }
    @Override
    public void setClob(int i, Reader reader, long l) throws SQLException { }
    @Override
    public void setBlob(int i, InputStream inputStream, long l) throws SQLException { }
    @Override
    public void setNClob(int i, Reader reader, long l) throws SQLException { }
    @Override
    public void setSQLXML(int i, SQLXML sqlxml) throws SQLException { }
    @Override
    public void setObject(int i, Object o, int i2, int i3) throws SQLException { }
    @Override
    public void setAsciiStream(int i, InputStream inputStream, long l) throws SQLException { }
    @Override
    public void setBinaryStream(int i, InputStream inputStream, long l) throws SQLException { }
    @Override
    public void setCharacterStream(int i, Reader reader, long l) throws SQLException { }
    @Override
    public void setAsciiStream(int i, InputStream inputStream) throws SQLException { }
    @Override
    public void setBinaryStream(int i, InputStream inputStream) throws SQLException { }
    @Override
    public void setCharacterStream(int i, Reader reader) throws SQLException { }
    @Override
    public void setNCharacterStream(int i, Reader reader) throws SQLException { }
    @Override
    public void setClob(int i, Reader reader) throws SQLException { }
    @Override
    public void setBlob(int i, InputStream inputStream) throws SQLException { }
    @Override
    public void setNClob(int i, Reader reader) throws SQLException { }
    @Override
    public ResultSet executeQuery(String s) throws SQLException {
      return null;  
    }
    @Override
    public int executeUpdate(String s) throws SQLException {
      return 0;  
    }
    @Override
    public void close() throws SQLException { }
    @Override
    public int getMaxFieldSize() throws SQLException {
      return 0;  
    }
    @Override
    public void setMaxFieldSize(int i) throws SQLException { }
    @Override
    public int getMaxRows() throws SQLException {
      return 0;  
    }
    @Override
    public void setMaxRows(int i) throws SQLException { }
    @Override
    public void setEscapeProcessing(boolean b) throws SQLException { }
    @Override
    public int getQueryTimeout() throws SQLException {
      return 0;  
    }
    @Override
    public void setQueryTimeout(int i) throws SQLException { }
    @Override
    public void cancel() throws SQLException { }
    @Override
    public SQLWarning getWarnings() throws SQLException {
      return null;  
    }
    @Override
    public void clearWarnings() throws SQLException { }
    @Override
    public void setCursorName(String s) throws SQLException { }
    @Override
    public boolean execute(String s) throws SQLException {
      return false;  
    }
    @Override
    public ResultSet getResultSet() throws SQLException {
      return null;  
    }
    @Override
    public int getUpdateCount() throws SQLException {
      return 0;  
    }
    @Override
    public boolean getMoreResults() throws SQLException {
      return false;  
    }
    @Override
    public void setFetchDirection(int i) throws SQLException { }
    @Override
    public int getFetchDirection() throws SQLException {
      return 0;  
    }
    @Override
    public void setFetchSize(int i) throws SQLException { }
    @Override
    public int getFetchSize() throws SQLException {
      return 0;  
    }
    @Override
    public int getResultSetConcurrency() throws SQLException {
      return 0;  
    }
    @Override
    public int getResultSetType() throws SQLException {
      return 0;  
    }
    @Override
    public void addBatch(String s) throws SQLException { }
    @Override
    public void clearBatch() throws SQLException { }
    @Override
    public int[] executeBatch() throws SQLException {
      return new int[0];  
    }
    @Override
    public Connection getConnection() throws SQLException {
      return null;  
    }
    @Override
    public boolean getMoreResults(int i) throws SQLException {
      return false;  
    }
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
      return null;  
    }
    @Override
    public int executeUpdate(String s, int i) throws SQLException {
      return 0;  
    }
    @Override
    public int executeUpdate(String s, int[] ints) throws SQLException {
      return 0;  
    }
    @Override
    public int executeUpdate(String s, String[] strings) throws SQLException {
      return 0;  
    }
    @Override
    public boolean execute(String s, int i) throws SQLException {
      return false;
    }
    @Override
    public boolean execute(String s, int[] ints) throws SQLException {
      return false;
    }
    @Override
    public boolean execute(String s, String[] strings) throws SQLException {
      return false;
    }
    @Override
    public int getResultSetHoldability() throws SQLException {
      return 0;
    }
    @Override
    public boolean isClosed() throws SQLException {
      return false;
    }
    @Override
    public void setPoolable(boolean b) throws SQLException { }
    @Override
    public boolean isPoolable() throws SQLException {
      return false;
    }
    @Override
    public <T> T unwrap(Class<T> tClass) throws SQLException {
      return null;
    }
    @Override
    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
      return false;
    }
  }

}
