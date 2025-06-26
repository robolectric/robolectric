package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.ContentProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ContentProviderController;

/** Tests for {@link ContentProviderBuilder} */
@RunWith(RobolectricTestRunner.class)
public class ContentProviderBuilderTest {
  @Test
  public void testOpenFile() throws Exception {
    MatrixCursor matrixCursor = new MatrixCursor(new String[] {"col1", "col2"});
    matrixCursor.addRow(new String[] {"hello", "world"});
    ContentProvider contentProvider =
        ContentProviderBuilder.newBuilder()
            .addCursor("/hey", matrixCursor)
            .addFile("/myfile", new ByteArrayInputStream("Hello World".getBytes(UTF_8)))
            .build();
    ContentProviderController.of(contentProvider).create("testcont");
    Cursor result =
        RuntimeEnvironment.getApplication()
            .getContentResolver()
            .query(Uri.parse("content://testcont/hey"), null, null, null, null);
    result.moveToFirst();
    assertThat(result.getString(0)).isEqualTo("hello");
    assertThat(result.getString(1)).isEqualTo("world");

    ParcelFileDescriptor parcelFileDescriptor =
        RuntimeEnvironment.getApplication()
            .getContentResolver()
            .openFile(Uri.parse("content://testcont/myfile"), "r", null);
    byte[] contents =
        ByteStreams.toByteArray(new FileInputStream(parcelFileDescriptor.getFileDescriptor()));
    assertThat(new String(contents, UTF_8)).isEqualTo("Hello World");
  }
}
