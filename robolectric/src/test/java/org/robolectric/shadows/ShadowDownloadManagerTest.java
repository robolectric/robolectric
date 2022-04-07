package org.robolectric.shadows;

import static android.app.DownloadManager.Request;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDownloadManager.CompletedDownload;
import org.robolectric.shadows.ShadowDownloadManager.ShadowRequest;

@RunWith(AndroidJUnit4.class)
public class ShadowDownloadManagerTest {

  private final Uri uri = Uri.parse("http://example.com/foo.mp4");
  private final Uri destination = Uri.parse("file:///storage/foo.mp4");
  private final Request request = new Request(uri);
  private final ShadowRequest shadow = shadowOf(request);

  @Test
  public void request_shouldGetUri() {
    assertThat(shadow.getUri().toString()).isEqualTo("http://example.com/foo.mp4");
  }

  @Test
  public void request_shouldGetDestinationUri() {
    request.setDestinationUri(Uri.parse("/storage/media/foo.mp4"));
    assertThat(shadow.getDestination().toString()).isEqualTo("/storage/media/foo.mp4");
  }

  @Test
  public void request_shouldGetTitle() {
    request.setTitle("Title");
    assertThat(shadow.getTitle().toString()).isEqualTo("Title");
  }

  @Test
  public void request_shouldGetDescription() {
    request.setDescription("Description");
    assertThat(shadow.getDescription().toString()).isEqualTo("Description");
  }

  @Test
  public void request_shouldGetMimeType() {
    request.setMimeType("application/json");
    assertThat(shadow.getMimeType().toString()).isEqualTo("application/json");
  }

  @Test
  public void request_shouldGetRequestHeaders() {
    request.addRequestHeader("Authorization", "Bearer token");
    List<Pair<String, String>> headers = shadow.getRequestHeaders();
    assertThat(headers).hasSize(1);
    assertThat(headers.get(0).first).isEqualTo("Authorization");
    assertThat(headers.get(0).second).isEqualTo("Bearer token");
  }

  @Test
  public void request_shouldGetNotificationVisibility() {
    request.setNotificationVisibility(Request.VISIBILITY_VISIBLE);
    assertThat(shadow.getNotificationVisibility()).isEqualTo(Request.VISIBILITY_VISIBLE);
  }

  @Test
  public void request_shouldGetAllowedNetworkTypes() {
    request.setAllowedNetworkTypes(Request.NETWORK_BLUETOOTH);
    assertThat(shadow.getAllowedNetworkTypes()).isEqualTo(Request.NETWORK_BLUETOOTH);
  }

  @Test
  public void request_shouldGetAllowedOverRoaming() {
    request.setAllowedOverRoaming(true);
    assertThat(shadow.getAllowedOverRoaming()).isTrue();
  }

  @Test
  public void request_shouldGetAllowedOverMetered() {
    request.setAllowedOverMetered(true);
    assertThat(shadow.getAllowedOverMetered()).isTrue();
  }

  @Test
  public void request_shouldGetVisibleInDownloadsUi() {
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
  public void query_shouldReturnCursor() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request);

    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isTrue();
  }

  @Test
  public void query_shouldReturnColumnIndices() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request.setDestinationUri(destination));
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).isAtLeast(0);
    assertThat(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).isAtLeast(0);
  }

  @Test
  public void query_shouldReturnColumnValues() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request.setDestinationUri(destination));
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)))
        .isEqualTo(uri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
        .isEqualTo(destination.toString());
  }

  @Test
  public void query_shouldHandleEmptyIds() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    assertThat(manager.query(new DownloadManager.Query())).isNotNull();
  }

  @Test
  public void query_shouldReturnAll() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    manager.enqueue(request.setDestinationUri(destination));
    Uri secondUri = Uri.parse("http://example.com/foo2.mp4");
    Uri secondDestination = Uri.parse("file:///storage/foo2.mp4");
    Request secondRequest = new Request(secondUri);
    manager.enqueue(secondRequest.setDestinationUri(secondDestination));
    Cursor cursor = manager.query(new DownloadManager.Query());

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)))
        .isEqualTo(uri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
        .isEqualTo(destination.toString());

    cursor.moveToNext();
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)))
        .isEqualTo(secondUri.toString());
    assertThat(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)))
        .isEqualTo(secondDestination.toString());
  }

  @Test
  public void query_shouldGetTotalSizeAndBytesSoFar() {
    long currentBytes = 500L;
    long totalSize = 1000L;
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request.setDestinationUri(destination));
    shadow.setTotalSize(totalSize);
    shadow.setBytesSoFar(currentBytes);
    Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id));

    cursor.moveToNext();
    assertThat(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)))
        .isEqualTo(totalSize);
    assertThat(
            cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)))
        .isEqualTo(currentBytes);
  }

  @Test
  public void request_shouldSetDestinationInExternalPublicDir_publicDirectories() throws Exception {
    shadow.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "foo.mp4");

    assertThat(shadow.getDestination().getLastPathSegment()).isEqualTo("foo.mp4");
  }

  @Config(minSdk = Q)
  @Test(expected = IllegalStateException.class)
  public void request_shouldNotSetDestinationInExternalPublicDir_privateDirectories()
      throws Exception {
    shadow.setDestinationInExternalPublicDir("bar", "foo.mp4");
  }

  @Test
  public void getRequest_doesNotReturnRemovedRequests() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id = manager.enqueue(request);

    manager.remove(id);

    assertThat(manager.getRequest(id)).isNull();
  }

  @Test
  public void addCompletedDownload_requiresNonNullNonEmptyTitle() {
    ShadowDownloadManager manager = new ShadowDownloadManager();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                null,
                "Description",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "",
                "Description",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));
  }

  @Test
  public void addCompletedDownload_requiresNonNullNonEmptyDescription() {
    ShadowDownloadManager manager = new ShadowDownloadManager();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                null,
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));
  }

  @Test
  public void addCompletedDownload_requiresNonNullNonEmptyPath() {
    ShadowDownloadManager manager = new ShadowDownloadManager();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "Description",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                null,
                /* length= */ 1024L,
                /* showNotification= */ true));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "Description",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "",
                /* length= */ 1024L,
                /* showNotification= */ true));
  }

  @Test
  public void addCompletedDownload_requiresNonNullNonEmptyMimeType() {
    ShadowDownloadManager manager = new ShadowDownloadManager();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "Description",
                /* isMediaScannerScannable= */ true,
                null,
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "Description",
                /* isMediaScannerScannable= */ true,
                "",
                "//storage/path/to/Title.pdf",
                /* length= */ 1024L,
                /* showNotification= */ true));
  }

  @Test
  public void addCompletedDownload_requiresPositiveLength() {
    ShadowDownloadManager manager = new ShadowDownloadManager();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            manager.addCompletedDownload(
                "Title",
                "Description",
                /* isMediaScannerScannable= */ true,
                "application/pdf",
                "//storage/path/to/Title.pdf",
                /* length= */ -1L,
                /* showNotification= */ true));
  }

  @Test
  public void getCompletedDownload_returnsExactCompletedDownload() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    long id =
        manager.addCompletedDownload(
            "Title",
            "Description",
            /* isMediaScannerScannable= */ true,
            "application/pdf",
            "//storage/path/to/Title.pdf",
            /* length= */ 1024L,
            /* showNotification= */ true);

    CompletedDownload capturedDownload = manager.getCompletedDownload(id);

    assertThat(capturedDownload.getTitle()).isEqualTo("Title");
    assertThat(capturedDownload.getDescription()).isEqualTo("Description");
    assertThat(capturedDownload.isMediaScannerScannable()).isTrue();
    assertThat(capturedDownload.getMimeType()).isEqualTo("application/pdf");
    assertThat(capturedDownload.getPath()).isEqualTo("//storage/path/to/Title.pdf");
    assertThat(capturedDownload.getLength()).isEqualTo(1024L);
    assertThat(capturedDownload.showNotification()).isTrue();
  }

  @Test
  public void getRequestCount_doesNotIncludeCompletedDownloads() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    manager.addCompletedDownload(
        "Title",
        "Description",
        /* isMediaScannerScannable= */ true,
        "application/pdf",
        "//storage/path/to/Title.pdf",
        /* length= */ 1024L,
        /* showNotification= */ true);

    assertThat(manager.getRequestCount()).isEqualTo(0);
  }

  @Test
  public void getCompletedDownloadsCount_doesNotIncludeRequests() {
    ShadowDownloadManager manager = new ShadowDownloadManager();
    manager.enqueue(request);

    assertThat(manager.getCompletedDownloadsCount()).isEqualTo(0);
  }
}
