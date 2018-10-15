package org.robolectric.shadows;

import static android.app.DownloadManager.Request;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowDownloadManager.ShadowRequest;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowDownloadManagerTest {

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
  public void request_shouldGetRequestHeaders() throws Exception {
    request.addRequestHeader("Authorization", "Bearer token");
    List<Pair<String, String>> headers = shadow.getRequestHeaders();
    assertThat(headers).hasSize(1);
    assertThat(headers.get(0).first).isEqualTo("Authorization");
    assertThat(headers.get(0).second).isEqualTo("Bearer token");
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

    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).isAtLeast(0);
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

  @Test
  public void query_shouldHandleEmptyIds() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    assertThat(manager.query(new DownloadManager.Query())).isNotNull();
  }

  @Test
  public void query_shouldReturnAll() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long firstId = manager.enqueue(request.setDestinationUri(destination));
    Uri secondUri = Uri.parse("http://example.com/foo2.mp4");
    Uri secondDestination = Uri.parse("file:///storage/foo2.mp4");
    Request secondRequest = new Request(secondUri);
    long secondId = manager.enqueue(secondRequest.setDestinationUri(secondDestination));
    Cursor cursor = manager.query(new DownloadManager.Query());

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))).isEqualTo(uri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).isEqualTo(destination.toString());

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI))).isEqualTo(secondUri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).isEqualTo(secondDestination.toString());
  }

}
