package org.robolectric.fakes;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class RoboCursorTest {
  private final Uri uri = Uri.parse("http://foo");
  private final RoboCursor cursor = new RoboCursor();
  private ContentResolver contentResolver;

  @Before
  public void setup() throws Exception {
    contentResolver = RuntimeEnvironment.application.getContentResolver();
    shadowOf(contentResolver).setCursor(uri, cursor);

    final ArrayList<String> columnNames = new ArrayList<>();
    columnNames.add("stringColumn");
    columnNames.add("longColumn");
    columnNames.add("intColumn");
    cursor.setColumnNames(columnNames);
  }

  @Test
  public void query_shouldMakeQueryParamsAvailable() throws Exception {
    contentResolver.query(uri, new String[]{"projection"}, "selection", new String[]{"selection"}, "sortOrder");
    assertThat(cursor.uri).isEqualTo(uri);
    assertThat(cursor.projection[0]).isEqualTo("projection");
    assertThat(cursor.selection).isEqualTo("selection");
    assertThat(cursor.selectionArgs[0]).isEqualTo("selection");
    assertThat(cursor.sortOrder).isEqualTo("sortOrder");
  }

  @Test
  public void get_shouldReturnColumnValue() throws Exception {
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L, 42}});
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.getColumnCount()).isEqualTo(3);
    assertThat(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_STRING);
    assertThat(cursor.getColumnName(0)).isEqualTo("stringColumn");
    assertThat(cursor.getColumnName(1)).isEqualTo("longColumn");
    assertThat(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("aString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(1234L);
    assertThat(cursor.getInt(cursor.getColumnIndex("intColumn"))).isEqualTo(42);
  }

  @Test
  public void moveToNext_advancesToNextRow() throws Exception {
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L, 41}, new Object[]{"anotherString", 5678L, 42}});

    assertThat(cursor.getCount()).isEqualTo(2);
    assertThat(cursor.getColumnCount()).isEqualTo(3);
    assertThat(cursor.getType(0)).isEqualTo(Cursor.FIELD_TYPE_STRING);
    assertThat(cursor.getType(1)).isEqualTo(Cursor.FIELD_TYPE_INTEGER);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getColumnName(0)).isEqualTo("stringColumn");
    assertThat(cursor.getColumnName(1)).isEqualTo("longColumn");
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("anotherString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(5678L);
    assertThat(cursor.getInt(cursor.getColumnIndex("intColumn"))).isEqualTo(42);
  }

  @Test
  public void moveToPosition_movesToAppropriateRow() throws Exception {
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L, 41}, new Object[]{"anotherString", 5678L, 42}});

    assertThat(cursor.moveToPosition(1)).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("anotherString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(5678L);
    assertThat(cursor.getInt(cursor.getColumnIndex("intColumn"))).isEqualTo(42);

    assertThat(cursor.moveToPosition(0)).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("aString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(1234L);
    assertThat(cursor.getInt(cursor.getColumnIndex("intColumn"))).isEqualTo(41);
  }

  @Test
  public void moveToPosition_checksBounds() {
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L, 41}, new Object[]{"anotherString", 5678L, 42}});
    assertThat(cursor.moveToPosition(2)).isFalse();
    assertThat(cursor.moveToPosition(-1)).isFalse();
  }

  @Test
  public void getCount_shouldReturnNumberOfRows() {
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L, 41}, new Object[]{"anotherString", 5678L, 42}});
    assertThat(cursor.getCount()).isEqualTo(2);
  }

  @Test
  public void close_isRemembered() throws Exception {
    cursor.close();
    assertThat(cursor.getCloseWasCalled()).isTrue();
  }

  @Test
  public void getColumnIndex_shouldReturnColumnIndex() {
    assertThat(cursor.getColumnIndex("invalidColumn")).isEqualTo(-1);
    assertThat(cursor.getColumnIndex("stringColumn")).isEqualTo(0);
    assertThat(cursor.getColumnIndexOrThrow("stringColumn")).isEqualTo(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getColumnIndexOrThrow_shouldThrowException() {
    cursor.getColumnIndexOrThrow("invalidColumn");
  }

  @Test
  public void isBeforeFirst_shouldReturnTrueForPosition() {
    cursor.setResults(new Object[][]{new Object[]{"Foo"}});

    assertThat(cursor.isBeforeFirst()).isTrue();
    cursor.moveToPosition(0);
    assertThat(cursor.isBeforeFirst()).isFalse();
  }

  @Test
  public void isAfterLast_shouldReturnTrueForPosition() {
    cursor.setResults(new Object[][]{new Object[]{"Foo"}});

    assertThat(cursor.isAfterLast()).isFalse();
    cursor.moveToPosition(1);
    assertThat(cursor.isAfterLast()).isTrue();
  }

  @Test
  public void isFirst_shouldReturnTrueForPosition() {
    cursor.setResults(new Object[][]{new Object[]{"Foo"}, new Object[]{"Bar"}});

    cursor.moveToPosition(0);
    assertThat(cursor.isFirst()).isTrue();

    cursor.moveToPosition(1);
    assertThat(cursor.isFirst()).isFalse();
  }

  @Test
  public void isLast_shouldReturnTrueForPosition() {
    cursor.setResults(new Object[][]{new Object[]{"Foo"}, new Object[]{"Bar"}});

    cursor.moveToPosition(0);
    assertThat(cursor.isLast()).isFalse();

    cursor.moveToPosition(1);
    assertThat(cursor.isLast()).isTrue();
  }
}
