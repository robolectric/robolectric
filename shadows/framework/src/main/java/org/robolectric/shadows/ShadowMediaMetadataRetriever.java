package org.robolectric.shadows;

import static org.robolectric.shadows.util.DataSource.toDataSource;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.util.DataSource;

@Implements(MediaMetadataRetriever.class)
public class ShadowMediaMetadataRetriever {
  private DataSource dataSource;
  private static final Map<DataSource, Map<Integer, String>> metadata = new HashMap<>();
  private static final Map<DataSource, Map<Long, Bitmap>> frames = new HashMap<>();
  private static final Map<DataSource, RuntimeException> exceptions = new HashMap<>();

  public void setDataSource(DataSource dataSource) {
    RuntimeException e = exceptions.get(dataSource);
    if (e != null) {
      e.fillInStackTrace();
      throw e;
    }
    this.dataSource = dataSource;
  }

  @Implementation
  protected void setDataSource(String path) {
    setDataSource(toDataSource(path));
  }

  @Implementation
  protected void setDataSource(Context context, Uri uri) {
    setDataSource(toDataSource(context, uri));
  }

  @Implementation
  protected void setDataSource(String uri, Map<String, String> headers) {
    setDataSource(toDataSource(uri, headers));
  }

  @Implementation
  protected void setDataSource(FileDescriptor fd, long offset, long length) {
    setDataSource(toDataSource(fd, offset, length));
  }

  @Implementation
  protected String extractMetadata(int keyCode) {
    if (metadata.containsKey(dataSource)) {
      return metadata.get(dataSource).get(keyCode);
    }
    return null;
  }

  @Implementation
  protected Bitmap getFrameAtTime(long timeUs, int option) {
    return (frames.containsKey(dataSource) ?
            frames.get(dataSource).get(timeUs) : null);
  }

  /**
   * Configures an exception to be thrown when {@link #setDataSource}
   * is called for the given data source.
   *
   * @param ds the data source that will trigger an exception
   * @param e the exception to trigger, or <tt>null</tt> to
   * avoid throwing an exception.
   */
  public static void addException(DataSource ds, RuntimeException e) {
    exceptions.put(ds, e);
  }

  public static void addMetadata(DataSource ds, int keyCode, String value) {
    if (!metadata.containsKey(ds)) {
      metadata.put(ds, new HashMap<Integer, String>());
    }
    metadata.get(ds).put(keyCode, value);
  }

  /**
   * Adds the given keyCode/value pair for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(String)} and
   * then calls {@link #addMetadata(DataSource, int, String)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addMetadata(DataSource, int, String)} directly.
   *
   * @param path the path to the data source whose metadata is being set.
   * @param keyCode the keyCode for the metadata being set, as used by {@link MediaMetadataRetriever#extractMetadata(int)}.
   * @param value the value for the specified metadata.
   */
  public static void addMetadata(String path, int keyCode, String value) {
    addMetadata(toDataSource(path), keyCode, value);
  }

  public static void addFrame(DataSource ds, long time, Bitmap bitmap) {
    if (!frames.containsKey(ds)) {
      frames.put(ds, new HashMap<Long, Bitmap>());
    }
    frames.get(ds).put(time, bitmap);
  }

  /**
   * Adds the given bitmap at the given time for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(String)} and
   * then calls {@link #addFrame(DataSource, long, Bitmap)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addFrame(DataSource, long, Bitmap)} directly.
   *
   * @param path the path to the data source.
   * @param time the playback time at which the specified bitmap
   * should be retrieved.
   * @param bitmap the bitmap to retrieve.
   */
  public static void addFrame(String path, long time, Bitmap bitmap) {
    addFrame(toDataSource(path), time, bitmap);
  }

  /**
   * Adds the given bitmap at the given time for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(Context, Uri)} and
   * then calls {@link #addFrame(DataSource, long, Bitmap)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addFrame(DataSource, long, Bitmap)} directly.
   *
   * @param context the Context object to match on the data source.
   * @param uri the Uri of the data source.
   * @param time the playback time at which the specified bitmap
   * should be retrieved.
   * @param bitmap the bitmap to retrieve.
   */
  public static void addFrame(Context context, Uri uri, long time, Bitmap bitmap) {
    addFrame(toDataSource(context, uri), time, bitmap);
  }

  /**
   * Adds the given bitmap at the given time for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(String, Map)} and
   * then calls {@link #addFrame(DataSource, long, Bitmap)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addFrame(DataSource, long, Bitmap)} directly.
   *
   * @param uri the Uri of the data source.
   * @param headers the headers to use when requesting the specified uri.
   * @param time the playback time at which the specified bitmap
   * should be retrieved.
   * @param bitmap the bitmap to retrieve.
   */
  public static void addFrame(String uri, Map<String, String> headers, long time, Bitmap bitmap) {
    addFrame(toDataSource(uri, headers), time, bitmap);
  }

  /**
   * Adds the given bitmap at the given time for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(FileDescriptor)} and
   * then calls {@link #addFrame(DataSource, long, Bitmap)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addFrame(DataSource, long, Bitmap)} directly.
   *
   * @param fd file descriptor of the data source.
   * @param time the playback time at which the specified bitmap
   * should be retrieved.
   * @param bitmap the bitmap to retrieve.
   */
  public static void addFrame(FileDescriptor fd, long time, Bitmap bitmap) {
    addFrame(toDataSource(fd), time, bitmap);
  }

  /**
   * Adds the given bitmap at the given time for the given data source.
   * Uses <tt>path</tt> to call {@link org.robolectric.shadows.util.DataSource#toDataSource(FileDescriptor, long, long)} and
   * then calls {@link #addFrame(DataSource, long, Bitmap)}. This
   * method is retained mostly for backwards compatibility;
   * you can call {@link #addFrame(DataSource, long, Bitmap)} directly.
   *
   * @param fd file descriptor of the data source.
   * @param offset the byte offset within the specified file from which to start reading the data.
   * @param length the number of bytes to read from the file.
   * @param time the playback time at which the specified bitmap
   * should be retrieved.
   * @param bitmap the bitmap to retrieve.
   */
  public static void addFrame(FileDescriptor fd, long offset, long length,
                              long time, Bitmap bitmap) {
    addFrame(toDataSource(fd, offset, length), time, bitmap);
  }

  @Resetter
  public static void reset() {
    metadata.clear();
    frames.clear();
    exceptions.clear();
  }
}
