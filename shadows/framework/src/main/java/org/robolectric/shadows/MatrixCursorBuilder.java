package org.robolectric.shadows;

import android.database.MatrixCursor;
import com.google.common.collect.ImmutableMap;

/**
 * Builder for {@link MatrixCursor} that is more fluent than the framework version and is available
 * for SDK < 19.
 */
public final class MatrixCursorBuilder {

  private final MatrixCursor matrixCursor;
  private final ImmutableMap<String, Integer> columnNameMap;

  public MatrixCursorBuilder(String... projection) {
    matrixCursor = new MatrixCursor(projection);
    ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
    for (int i = 0; i < projection.length; i++) {
      builder.put(projection[i], i);
    }
    columnNameMap = builder.build();
  }

  public RowBuilder newRow() {
    return new RowBuilder();
  }

  public void addRow(RowBuilder rowBuilder) {
    matrixCursor.addRow(rowBuilder.getColumnValues());
  }

  public MatrixCursor build() {
    return matrixCursor;
  }

  /** Offers similar functionality to MatrixCursor.RowBuilder (only available in API 19+) */
  public final class RowBuilder {

    private final Object[] columnValues;

    RowBuilder() {
      columnValues = new Object[columnNameMap.size()];
    }

    /**
     * Sets the specific column to the specified value. Attempts to set a value for an unrecognized
     * column are ignored.
     *
     * @param columnName The column name to set the value for.
     * @param value The value to set.
     */
    public RowBuilder set(String columnName, String value) {
      return setInternal(columnName, value);
    }

    /**
     * Sets the specific column to the specified value. Attempts to set a value for an unrecognized
     * column are ignored.
     *
     * @param columnName The column name to set the value for.
     * @param value The value to set.
     */
    public RowBuilder set(String columnName, Integer value) {
      return setInternal(columnName, value);
    }

    /**
     * Sets the specific column to the specified value. Attempts to set a value for an unrecognized
     * column are ignored.
     *
     * @param columnName The column name to set the value for.
     * @param value The value to set.
     */
    public RowBuilder set(String columnName, Long value) {
      return setInternal(columnName, value);
    }

    /**
     * Sets the specific column to the specified value. Attempts to set a value for an unrecognized
     * column are ignored.
     *
     * @param columnName The column name to set the value for.
     * @param value The value to set.
     */
    public RowBuilder set(String columnName, byte[] value) {
      return setInternal(columnName, value);
    }

    private RowBuilder setInternal(String columnName, Object value) {
      Integer columnIndex = columnNameMap.get(columnName);
      if (columnIndex != null) {
        columnValues[columnIndex] = value;
      }
      return this;
    }

    /** Includes the given value in columnValues if the given columnName is in the projection */
    public RowBuilder add(String columnName, Object value) {
      String[] cursorColumnNames = matrixCursor.getColumnNames();
      for (int i = 0; i < cursorColumnNames.length; i++) {
        if (columnName.equals(cursorColumnNames[i])) {
          columnValues[i] = value;
        }
      }
      return this;
    }

    Object[] getColumnValues() {
      return columnValues;
    }
  }
}
