package org.robolectric.shadows;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static android.app.DownloadManager.COLUMN_URI;
import static android.app.DownloadManager.Request;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowDownloadManager.ShadowRequest;

@RunWith(TestRunners.WithDefaults.class)
public class DownloadManagerTest {

  private final Uri uri = Uri.parse("http://example.com/foo.mp4");
  private final Uri dest = Uri.parse("file:///storage/foo.mp4");
  private final Request request = new Request(Uri.parse("http://example.com/foo.mp4"));
  private final ShadowRequest shadow = shadowOf(request);

  @Test
  public void request_shouldGetUri() throws Exception {
    assertThat(shadow.getUri().toString()).isEqualTo("http://example.com/foo.mp4");
  }

  @Test
  public void request_shouldGetDestinationUri() throws Exception {
    request.setDestinationUri(Uri.parse("/storage/media/foo.mp4"));
    assertThat(shadow.getDestination().toString()).isEqualTo("/storage/media/foo.mp4");
  }

  @Test
  public void request_shouldGetTitle() throws Exception {
    request.setTitle("Title");
    assertThat(shadow.getTitle()).isEqualTo("Title");
  }

  @Test
  public void request_shouldGetDescription() throws Exception {
    request.setDescription("Description");
    assertThat(shadow.getDescription()).isEqualTo("Description");
  }

  @Test
  public void query_shouldReturnCursor() throws Exception {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(new Request(uri));

    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
  }

  @Test
  public void query_shouldReturnColumnIndexes() throws Exception {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(new Request(uri).setDestinationUri(dest));
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).isNotNegative();
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).isNotNegative();
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)).isNotNegative();
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).isNotNegative();
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)).isNotNegative();
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).isNotNegative();
  }

  @Test
  public void query_shouldReturnColumnValues() throws Exception {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(new Request(uri).setDestinationUri(dest));
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))).isEqualTo(uri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).isEqualTo(dest.toString());
  }
}
