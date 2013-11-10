package org.robolectric.shadows;

import android.database.Cursor;
import android.database.CursorWindow;
import com.almworks.sqlite4java.SQLiteConstants;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Implements(value = CursorWindow.class)
public class ShadowCursorWindow {

  private static final AtomicInteger WINDOW_PTR_COUNTER = new AtomicInteger(0);

  private static final ConcurrentHashMap<Integer, Data> DATA_MAP = new ConcurrentHashMap<Integer, Data>();

  private static Data data(final int ptr) {
    Data data = DATA_MAP.get(ptr);
    if (data == null) {
      throw new IllegalArgumentException("Invalid window pointer: " + ptr + "; current pointers: " + DATA_MAP.keySet());
    }
    return data;
  }

  static int getCount(final int ptr) {
    return data(ptr).rows.size();
  }

  static void setData(final int ptr, final SQLiteStatement stmt) throws SQLiteException {
    if (DATA_MAP.contains(ptr)) {
      throw new IllegalStateException("Already have data for window " + ptr);
    }
    DATA_MAP.put(ptr, new Data(stmt));
  }

  private static void close(final int ptr) {
    DATA_MAP.remove(ptr);
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
    return (byte[])data.value(row, column).value;
  }

  @Implementation
  public static String nativeGetString(int windowPtr, int row, int column) {
    Value val = data(windowPtr).value(row, column);
    if (val.type == Cursor.FIELD_TYPE_BLOB) {
      throw new android.database.sqlite.SQLiteException("Getting string when column is blob. Row " + row + ", col " + column);
    }
    Object value = val.value;
    return value == null ? null : String.valueOf(value);
  }

  @Implementation
  public static long nativeGetLong(int windowPtr, int row, int column) {
    return nativeGetNumber(windowPtr, row, column).longValue();
  }

  @Implementation
  public static double nativeGetDouble(int windowPtr, int row, int column) {
    return nativeGetNumber(windowPtr, row, column).doubleValue();
  }

  @Implementation
  public static int nativeGetType(int windowPtr, int row, int column) {
    Value val = data(windowPtr).value(row, column);
    return val.value == null ? Cursor.FIELD_TYPE_NULL : val.type;
  }

  // https://github.com/android/platform_frameworks_base/blob/master/core/jni/android_database_CursorWindow.cpp#L364
  private static Number nativeGetNumber(int windowPtr, int row, int column) {
    Value value = data(windowPtr).value(row, column);
    switch (value.type) {
      case Cursor.FIELD_TYPE_NULL:
      case SQLiteConstants.SQLITE_NULL:
        return 0;
      case Cursor.FIELD_TYPE_INTEGER:
      case Cursor.FIELD_TYPE_FLOAT:
        return (Number) value.value;
      case Cursor.FIELD_TYPE_STRING: {
        try {
          return Double.parseDouble((String) value.value);
        } catch (NumberFormatException e) {
          return 0;
        }
      }
      case Cursor.FIELD_TYPE_BLOB:
        throw new android.database.sqlite.SQLiteException("could not convert "+value);
      default:
        throw new android.database.sqlite.SQLiteException("unknown type: "+value.type);
    }
  }

  private static class Data {
    String[] columnNameArray;

    final ArrayList<Map<String, Value>> rows = new ArrayList<Map<String, Value>>();
    final SQLiteStatement stmt;

    public Data(final SQLiteStatement stmt) throws SQLiteException {
      this.stmt = stmt;
      cacheColumnNames();
      fillRows();
    }

    public Value value(int rowN, int colN) {
      Map<String, Value> row = rows.get(rowN);
      if (row == null) {
        throw new IllegalArgumentException("Bad row number: " + rowN + ", count: " + rows.size());
      }
      String col = columnNameArray[colN];
      return row.get(col);
    }

    private Map<String, Value> fillRowValues(SQLiteStatement stmt) throws SQLiteException {
      Map<String, Value> row = new HashMap<String, Value>();
      int index = 0;
      for (String s : columnNameArray) {
        row.put(s, new Value(stmt.columnValue(index), stmt.columnType(index)));
        index++;
      }
      return row;
    }

    private void fillRows() throws SQLiteException {
      //Android caches results in the WindowedCursor to allow moveToPrevious() to function.
      //Robolectric will have to cache the results too. In the rows map.
      while (stmt.step()) {
        Map<String, Value> row = fillRowValues(stmt);
        rows.add(row);
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

  private static class Value {
    final Object value;
    final int type;

    public Value(final Object value, final int type) {
      this.value = value;
      this.type = type;
    }
  }

  /* TODO:

  private static native int nativeCreateFromParcel(Parcel parcel);
  private static native void nativeWriteToParcel(int windowPtr, Parcel parcel);

  private static native void nativeClear(int windowPtr);

  private static native int nativeGetNumRows(int windowPtr);
  private static native boolean nativeSetNumColumns(int windowPtr, int columnNum);
  private static native boolean nativeAllocRow(int windowPtr);
  private static native void nativeFreeLastRow(int windowPtr);

  private static native void nativeCopyStringToBuffer(int windowPtr, int row, int column,
                                                      CharArrayBuffer buffer);

  private static native boolean nativePutBlob(int windowPtr, byte[] value, int row, int column);
  private static native boolean nativePutString(int windowPtr, String value, int row, int column);
  private static native boolean nativePutLong(int windowPtr, long value, int row, int column);
  private static native boolean nativePutDouble(int windowPtr, double value, int row, int column);
  private static native boolean nativePutNull(int windowPtr, int row, int column);

  private static native String nativeGetName(int windowPtr);
   */

}
