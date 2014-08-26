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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class SimpleTestCursorTest {
  private Uri uri;
  private SimpleTestCursor cursor;
  private ContentResolver contentResolver;

  @Before
  public void setup() throws Exception {
    contentResolver = Robolectric.application.getContentResolver();
    ShadowContentResolver shadowContentResolver = shadowOf(contentResolver);
    uri = Uri.parse("http://foo");
    cursor = new SimpleTestCursor();
    shadowContentResolver.setCursor(uri, cursor);
    ArrayList<String> columnNames = new ArrayList<String>();
    columnNames.add("stringColumn");
    columnNames.add("longColumn");
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
    cursor.setResults(new Object[][]{new Object[]{"aString", 1234L}});
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("aString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(1234L);
  }

  @Test
  public void moveToNextAdvancesToNextRow() throws Exception {
    cursor.setResults(new Object[][] { new Object[] { "aString", 1234L }, new Object[] { "anotherString", 5678L }});
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getString(cursor.getColumnIndex("stringColumn"))).isEqualTo("anotherString");
    assertThat(cursor.getLong(cursor.getColumnIndex("longColumn"))).isEqualTo(5678L);
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
