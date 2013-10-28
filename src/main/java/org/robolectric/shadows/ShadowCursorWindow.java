package org.robolectric.shadows;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Implements(value = CursorWindow.class)
public class ShadowCursorWindow {

  private static final AtomicInteger WINDOW_PTR_COUNTER = new AtomicInteger(0);

  private static final ConcurrentHashMap<Integer, Data> RESULT_SETS_MAP = new ConcurrentHashMap<Integer, Data>();

  private static Data data(final int ptr) {
    Data data = RESULT_SETS_MAP.get(ptr);
    if (data == null) {
      throw new IllegalArgumentException("Invalid window pointer: " + ptr + "; current pointers: " + RESULT_SETS_MAP.keySet());
    }
    return data;
  }

  static boolean hasResultSetFor(final int ptr) {
    return RESULT_SETS_MAP.contains(ptr);
  }

  static int getCount(final int ptr) {
    return data(ptr).rows.size();
  }

  static void setData(final int ptr, final ResultSet rs) throws SQLException {
    if (RESULT_SETS_MAP.contains(ptr)) {
      throw new IllegalStateException("Already have data for window " + ptr);
    }
    RESULT_SETS_MAP.put(ptr, new Data(rs));
  }

  private static void close(final int ptr) {
    Data data = RESULT_SETS_MAP.get(ptr);
    if (data != null) {
      try {
        data.rs.close();
      } catch (SQLException e) {
        throw new SQLiteException("Cannot close cursor data", e);
      }
      RESULT_SETS_MAP.remove(ptr);
    }
  }

  @Implementation
  public static int nativeCreate(String name, int cursorWindowSize) {
    return WINDOW_PTR_COUNTER.incrementAndGet();
  }

  @Implementation
  public static void nativeDispose(int windowPtr) {
    close(windowPtr);
  }

  @Implementation
  public static byte[] nativeGetBlob(int windowPtr, int row, int column) {
    Data data = data(windowPtr);
    return (byte[])data.getValue(row, column);
  }

  @Implementation
  public static String nativeGetString(int windowPtr, int row, int column) {
    return (String) data(windowPtr).getValue(row, column);
  }

  @Implementation
  public static long nativeGetLong(int windowPtr, int row, int column) {
    return ((Number) data(windowPtr).getValue(row, column)).longValue();
  }

  @Implementation
  public static double nativeGetDouble(int windowPtr, int row, int column) {
    return ((Number) data(windowPtr).getValue(row, column)).doubleValue();
  }

  private static class Data {
    String[] columnNameArray;

    final LinkedHashMap<Integer, Map<String, Object>> rows = new LinkedHashMap<Integer, Map<String, Object>>();
    final ResultSet rs;

    public Data(final ResultSet rs) throws SQLException {
      this.rs = rs;
      cacheColumnNames();
      fillRows();
    }

    public Object getValue(final int rowN, final int colN) {
      Map<String, Object> row = rows.get(rowN);
      if (row == null) {
        throw new IllegalArgumentException("Bad row number: " + rowN + ", count: " + rows.size());
      }
      String col = columnNameArray[colN];
      return row.get(col);
    }

    private Map<String, Object> fillRowValues(ResultSet rs) throws SQLException {
      Map<String, Object> row = new HashMap<String, Object>();
      for (String s : columnNameArray) {
        row.put(s, rs.getObject(s));
      }
      return row;
    }

    private void fillRows() throws SQLException {
      //ResultSets in SQLite\Android are only TYPE_FORWARD_ONLY. Android caches results in the WindowedCursor to allow moveToPrevious() to function.
      //Robolectric will have to cache the results too. In the rows map.
      int count = 0;
      if (rs.next()) {
        do {
          Map<String, Object> row = fillRowValues(rs);
          rows.put(count, row);
          count++;
        } while (rs.next());
      } else {
        rs.close();
      }
    }

    /**
     * Stores the column names so they are retrievable after the resultSet has closed
     */
    private void cacheColumnNames() throws SQLException {
      ResultSetMetaData metaData = rs.getMetaData();
      int columnCount = metaData.getColumnCount();
      columnNameArray = new String[columnCount];
      for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
        String cName = metaData.getColumnName(columnIndex).toLowerCase();
        this.columnNameArray[columnIndex - 1] = cName;
      }
    }

  }

}
