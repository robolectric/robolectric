package org.robolectric.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.List;

/** Pretty prints an ascii table of Strings */
final class AsciiTable {

  private final List<List<String>> rows = new ArrayList<>();
  private int numColumns = 0;

  AsciiTable() {}

  public void addRow(Object... values) {
    List<String> row = new ArrayList<>();
    for (Object value : values) {
      row.add(value.toString());
    }
    if (numColumns == 0) {
      numColumns = row.size();
    } else {
      checkState(
          row.size() == numColumns,
          String.format(
              "All rows must have the same number of columns. Expected: %d, received %d",
              numColumns, row.size()));
    }
    rows.add(row);
  }

  public void print() {
    int numColumns = rows.get(0).size();
    StringBuilder formatRow = new StringBuilder();
    for (int i = 0; i < numColumns; i++) {
      int maxWidth = 0;
      for (List<String> row : rows) {
        maxWidth = Math.max(maxWidth, row.get(i).length());
      }
      formatRow.append("%-");
      formatRow.append(maxWidth);
      formatRow.append("s ");
    }

    for (List<String> row : rows) {
      System.out.printf(formatRow.toString(), row.toArray());
      System.out.println();
    }
  }
}
