package org.robolectric.tester.android.database;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class SimpleTestCursorTest {
  private static final String STRING_COLUMN = "stringColumn";
  private static final String INTEGER_COLUMN = "intColumn";
  private static final String LONG_COLUMN = "longColumn";
  private static final String SHORT_COLUMN = "shortColumn";
  private static final String FLOAT_COLUMN = "floatColumn";
  private static final String DOUBLE_COLUMN = "doubleColumn";
  private static final String BLOB_COLUMN = "blobColumn";
  private static final List<String> ALL_COLUMNS = Arrays.asList(STRING_COLUMN, INTEGER_COLUMN,
      LONG_COLUMN, SHORT_COLUMN, FLOAT_COLUMN, DOUBLE_COLUMN, BLOB_COLUMN);

  private static final String PROJECTION = "projection";
  private static final String SELECTION = "selection";
  private static final String SORT_ORDER = "sortOrder";

  private static final String TEST_STRING = "aString";
  private static final String TEST_STRING_2 = "anotherString";
  private static final int TEST_INTEGER = 1234;
  private static final long TEST_LONG = 1234L;
  private static final short TEST_SHORT = (short) 1234;
  private static final float TEST_FLOAT = 1.234f;
  private static final double TEST_DOUBLE = 1.234;
  private static final byte[] TEST_BYTES = new byte[] { 0, 1, 2, 3 };
  private static final long TEST_LONG_2 = 5678L;
  private static final Uri TEST_URI = Uri.parse("http://foo");
  private SimpleTestCursor cursor;
  private ContentResolver contentResolver;

  @Before
  public void setup() throws Exception {
    contentResolver = Robolectric.application.getContentResolver();
    ShadowContentResolver shadowContentResolver = shadowOf(contentResolver);
    cursor = new SimpleTestCursor();
    shadowContentResolver.setCursor(TEST_URI, cursor);
  }

  @Test
  public void doingQueryShouldMakeQueryParamsAvailable() throws Exception {
    contentResolver.query(TEST_URI, new String[] { PROJECTION }, SELECTION,
        new String[]{ SELECTION }, SORT_ORDER);
    assertThat(cursor.uri).isEqualTo(TEST_URI);
    assertThat(cursor.projection[0]).isEqualTo(PROJECTION);
    assertThat(cursor.selection).isEqualTo(SELECTION);
    assertThat(cursor.selectionArgs[0]).isEqualTo(SELECTION);
    assertThat(cursor.sortOrder).isEqualTo(SORT_ORDER);
  }

  @Test
  public void canSetData() {
    cursor.setResults(new Object[][] { new Object[] { TEST_STRING }});
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(0)).isEqualTo(TEST_STRING);
  }

  @Test
  public void canSetColumns() {
    cursor.setColumnNames(Arrays.asList(STRING_COLUMN));
    assertThat(cursor.getColumnName(0)).isEqualTo(STRING_COLUMN);
    assertThat(cursor.getColumnCount()).isEqualTo(1);
    assertThat(cursor.getColumnNames().length).isEqualTo(1);
    assertThat(cursor.getColumnNames()[0]).isEqualTo(STRING_COLUMN);
  }

  @Test
  public void canGetAllDataTypes() throws Exception {
    cursor.setResults(ALL_COLUMNS, new Object[][] { new Object[] {
        TEST_STRING, TEST_INTEGER, TEST_LONG, TEST_SHORT, TEST_FLOAT, TEST_DOUBLE, TEST_BYTES
    }});
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex(STRING_COLUMN))).isEqualTo(TEST_STRING);
    assertThat(cursor.getInt(cursor.getColumnIndex(INTEGER_COLUMN))).isEqualTo(TEST_INTEGER);
    assertThat(cursor.getLong(cursor.getColumnIndex(LONG_COLUMN))).isEqualTo(TEST_LONG);
    assertThat(cursor.getShort(cursor.getColumnIndex(SHORT_COLUMN))).isEqualTo(TEST_SHORT);
    assertThat(cursor.getFloat(cursor.getColumnIndex(FLOAT_COLUMN))).isEqualTo(TEST_FLOAT);
    assertThat(cursor.getDouble(cursor.getColumnIndex(DOUBLE_COLUMN))).isEqualTo(TEST_DOUBLE);
    assertThat(cursor.getBlob(cursor.getColumnIndex(BLOB_COLUMN))).isEqualTo(TEST_BYTES);
  }

  @Test
  public void moveToNextAdvancesToNextRow() throws Exception {
    cursor.setResults(Arrays.asList(STRING_COLUMN, LONG_COLUMN),
        new Object[][] {
            new Object[] { TEST_STRING, TEST_LONG },
            new Object[] { TEST_STRING_2, TEST_LONG_2 }
        });
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex(STRING_COLUMN))).isEqualTo(TEST_STRING_2);
    assertThat(cursor.getLong(cursor.getColumnIndex(LONG_COLUMN))).isEqualTo(TEST_LONG_2);
  }

  @Test
  public void closeIsRemembered() throws Exception {
    cursor.close();
    assertThat(cursor.getCloseWasCalled()).isTrue();
  }

  @Test
  public void getColumnIndex(){
    List<String> columns = Arrays.asList(STRING_COLUMN, LONG_COLUMN);
    cursor.setResults(columns, new Object[0][columns.size()]);
    assertThat(cursor.getColumnIndex("invalidColumn")).isEqualTo(-1);
    assertThat(cursor.getColumnIndex(STRING_COLUMN)).isEqualTo(0);
    assertThat(cursor.getColumnIndexOrThrow(STRING_COLUMN)).isEqualTo(0);
    try {
      cursor.getColumnIndexOrThrow("invalidColumn");
      fail();
    } catch (IllegalArgumentException ignored) { }
  }

  @Test
  public void getColumnCount() {
    cursor.setResults(ALL_COLUMNS, new Object[0][ALL_COLUMNS.size()]);
    assertThat(cursor.getColumnCount()).isEqualTo(ALL_COLUMNS.size());
    cursor.setResults(new ArrayList<String>(), new Object[0][0]);
    assertThat(cursor.getColumnCount()).isEqualTo(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void tooManyColumns() {
    cursor.setResults(Arrays.asList(STRING_COLUMN),
        new Object[][] {
            new Object[] { TEST_STRING, TEST_LONG },
            new Object[] { TEST_STRING_2, TEST_LONG_2 }
        });
  }

  @Test(expected = IllegalArgumentException.class)
  public void inconsistentColumnSize() {
    cursor.setResults(Arrays.asList(STRING_COLUMN, LONG_COLUMN),
        new Object[][] {
            new Object[] { TEST_STRING, TEST_LONG },
            new Object[] { TEST_STRING_2, TEST_LONG_2, TEST_DOUBLE }
        });
  }
}
