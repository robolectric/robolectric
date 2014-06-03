package org.robolectric.shadows;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static android.app.DownloadManager.Request;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowDownloadManager.ShadowRequest;

@RunWith(TestRunners.WithDefaults.class)
public class DownloadManagerTest {

  private final Uri uri = Uri.parse("http://example.com/foo.mp4");
  private final Uri destination = Uri.parse("file:///storage/foo.mp4");
  private final Request request = new Request(uri);
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
  public void request_shouldGetMimeType() throws Exception {
    request.setMimeType("application/json");
    assertThat(shadow.getMimeType()).isEqualTo("application/json");
  }

  @Test
  public void request_shouldGetNotificationVisibility() throws Exception {
    request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
    assertThat(shadow.getNotificationVisibility()).isEqualTo(Request.VISIBILITY_VISIBLE);
  }

  @Test
  public void request_shouldGetAllowedNetworkTypes() throws Exception {
    request.setAllowedNetworkTypes(Request.NETWORK_BLUETOOTH);
    assertThat(shadow.getAllowedNetworkTypes()).isEqualTo(Request.NETWORK_BLUETOOTH);
  }

  @Test
  public void request_shouldGetAllowedOverRoaming() throws Exception {
    request.setAllowedOverRoaming(true);
    assertThat(shadow.getAllowedOverRoaming()).isTrue();
  }

  @Test
  public void request_shouldGetAllowedOverMetered() throws Exception {
    request.setAllowedOverMetered(true);
    assertThat(shadow.getAllowedOverMetered()).isTrue();
  }

  @Test
  public void request_shouldGetVisibleInDownloadsUi() throws Exception {
    request.setVisibleInDownloadsUi(true);
    assertThat(shadow.getVisibleInDownloadsUi()).isTrue();
  }

  @Test
  public void enqueue_shouldAddRequest() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request);

    assertThat(manager.getRequestCount()).isEqualTo(1);
    assertThat(manager.getRequest(id)).isEqualTo(request);
  }

  @Test
  public void query_shouldReturnCursor() throws Exception {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request);

    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
  }

  @Test
  public void query_shouldReturnColumnIndexes() throws Exception {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request.setDestinationUri(destination));
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
    long id = manager.enqueue(request.setDestinationUri(destination));
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))).isEqualTo(uri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).isEqualTo(destination.toString());
  }
}
