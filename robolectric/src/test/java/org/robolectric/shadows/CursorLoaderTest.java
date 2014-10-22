package org.robolectric.shadows;

import android.app.Activity;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class CursorLoaderTest {
  @Test
  public void testGetters() {
    Uri uri = Uri.parse("http://robolectric.org");
    String[] projection = new String[] { "_id", "TestColumn" };
    String selection = "_id = ?";
    String[] selectionArgs = new String[] { "5" };
    String sortOrder = "_id";
    CursorLoader cursorLoader = new CursorLoader(new Activity(),
        uri,
        projection,
        selection,
        selectionArgs,
        sortOrder);

    assertThat(cursorLoader.getUri()).isEqualTo(uri);
    assertThat(cursorLoader.getProjection()).isEqualTo(projection);
    assertThat(cursorLoader.getSelection()).isEqualTo(selection);
    assertThat(cursorLoader.getSelectionArgs()).isEqualTo(selectionArgs);
    assertThat(cursorLoader.getSortOrder()).isEqualTo(sortOrder);
  }

  @Test
  public void testSetters() {
    Uri uri = Uri.parse("http://robolectric.org");
    String[] projection = new String[] { "_id", "TestColumn" };
    String selection = "_id = ?";
    String[] selectionArgs = new String[] { "5" };
    String sortOrder = "_id";
    CursorLoader cursorLoader = new CursorLoader(new Activity());
    cursorLoader.setUri(uri);
    cursorLoader.setProjection(projection);
    cursorLoader.setSelection(selection);
    cursorLoader.setSelectionArgs(selectionArgs);
    cursorLoader.setSortOrder(sortOrder);

    assertThat(cursorLoader.getUri()).isEqualTo(uri);
    assertThat(cursorLoader.getProjection()).isEqualTo(projection);
    assertThat(cursorLoader.getSelection()).isEqualTo(selection);
    assertThat(cursorLoader.getSelectionArgs()).isEqualTo(selectionArgs);
    assertThat(cursorLoader.getSortOrder()).isEqualTo(sortOrder);
  }
}
