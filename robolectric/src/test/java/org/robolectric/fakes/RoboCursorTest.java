package org.robolectric.fakes;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.fakes.RoboCursor;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class RoboCursorTest {
  private Uri uri;
  private RoboCursor cursor;
  private ContentResolver contentResolver;

  @Before
  public void setup() throws Exception {
    contentResolver = RuntimeEnvironment.application.getContentResolver();
    ShadowContentResolver shadowContentResolver = shadowOf(contentResolver);
    uri = Uri.parse("http://foo");
    cursor = new RoboCursor();
    shadowContentResolver.setCursor(uri, cursor);
    ArrayList<String> columnNames = new ArrayList<String>();
    columnNames.add("stringColumn");
    columnNames.add("longColumn");
    columnNames.add("intColumn");
    cursor.setColumnNames(columnNames);
  }

  @Test
  public void doingQueryShouldMakeQueryParamsAvailable() throws Exception {
    contentResolver.query(uri, new String[]{"projection"}, "selection", new String[]{"selection"}, "sortOrder");
    assertThat(cursor.uri).isEqualTo(uri);
    assertThat(cursor.projection[0]).isEqualTo("projection");
    assertThat(cursor.selection).isEqualTo("selection");
    assertThat(cursor.selectionArgs[0]).isEqualTo("selection");
    assertThat(cursor.sortOrder).isEqualTo("sortOrder");
  }

  @Test
  public void canGetStringsAndLongs() throws Exception {
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
  public void moveToNextAdvancesToNextRow() throws Exception {
    cursor.setResults(new Object[][] { new Object[] { "aString", 1234L, 41 },
            new Object[] { "anotherString", 5678L, 42 }});
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
  public void closeIsRemembered() throws Exception {
    cursor.close();
    assertThat(cursor.getCloseWasCalled()).isTrue();
  }

  @Test
  public void getColumnIndex(){
    assertThat(cursor.getColumnIndex("invalidColumn")).isEqualTo(-1);
    assertThat(cursor.getColumnIndex("stringColumn")).isEqualTo(0);
    assertThat(cursor.getColumnIndexOrThrow("stringColumn")).isEqualTo(0);
    try{
      cursor.getColumnIndexOrThrow("invalidColumn");
      fail();
    }catch (IllegalArgumentException ex){}
  }
}
