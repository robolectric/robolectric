package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.database.MatrixCursor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** tests for {@link MatrixCursorBuilder}. */
@RunWith(AndroidJUnit4.class)
public class MatrixCursorBuilderTest {
  @Test
  public void build() {
    MatrixCursorBuilder builder = new MatrixCursorBuilder("column_1", "column_2");

    builder.addRow(
        builder
            .newRow()
            .set("column_1", "foo")
            .set("column_2", 2)
            .set("unsupported_column", "value_to_be_ignored"));

    builder.addRow(builder.newRow().set("column_1", "bar").set("column_2", 3));

    MatrixCursor cursor = builder.build();

    cursor.moveToFirst();
    assertThat(cursor.getString(cursor.getColumnIndexOrThrow("column_1"))).isEqualTo("foo");
    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("column_2"))).isEqualTo(2);
    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndexOrThrow("column_1"))).isEqualTo("bar");
    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow("column_2"))).isEqualTo(3);
    assertThat(cursor.isLast()).isTrue();
  }
}
