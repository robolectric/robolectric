package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import android.net.Uri;
import android.support.v4.content.CursorLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class CursorLoaderTest {
  @Test
  public void testGetters() {
    Uri uri = Uri.parse("http://robolectric.org");
    String[] projection = new String[] { "_id", "TestColumn" };
    String selection = "_id = ?";
    String[] selectionArgs = new String[] { "5" };
    String sortOrder = "_id";
    CursorLoader cursorLoader = new CursorLoader(RuntimeEnvironment.application,
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
    CursorLoader cursorLoader = new CursorLoader(RuntimeEnvironment.application);
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
