package org.robolectric.shadows;

import android.database.CursorWindow;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

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

  static void setData(final int ptr, final SQLiteStatement stmt) throws SQLiteException {
    if (RESULT_SETS_MAP.contains(ptr)) {
      throw new IllegalStateException("Already have data for window " + ptr);
    }
    RESULT_SETS_MAP.put(ptr, new Data(stmt));
  }

  private static void close(final int ptr) {
    RESULT_SETS_MAP.remove(ptr);
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
    final SQLiteStatement stmt;

    public Data(final SQLiteStatement stmt) throws SQLiteException {
      this.stmt = stmt;
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

    private Map<String, Object> fillRowValues(SQLiteStatement stmt) throws SQLiteException {
      Map<String, Object> row = new HashMap<String, Object>();
      int index = 0;
      for (String s : columnNameArray) {
        row.put(s, stmt.columnValue(index++));
      }
      return row;
    }

    private void fillRows() throws SQLiteException {
      //Android caches results in the WindowedCursor to allow moveToPrevious() to function.
      //Robolectric will have to cache the results too. In the rows map.
      int count = 0;
      while (stmt.step()) {
        Map<String, Object> row = fillRowValues(stmt);
        rows.put(count++, row);
      }
    }

    /**
     * Stores the column names so they are retrievable after the resultSet has closed
     */
    private void cacheColumnNames() throws SQLiteException {
      int columnCount = stmt.columnCount();
      columnNameArray = new String[columnCount];
      for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        String cName = stmt.getColumnName(columnIndex).toLowerCase();
        this.columnNameArray[columnIndex] = cName;
      }
    }

  }

}
