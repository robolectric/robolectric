package org.robolectric.shadows.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.net.Uri;
import java.io.FileDescriptor;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

/**
 * Opaque class for uniquely identifying a media data source, as used by {@link
 * org.robolectric.shadows.ShadowMediaPlayer}, {@link
 * org.robolectric.shadows.ShadowMediaMetadataRetriever}, and {@link
 * org.robolectric.shadows.ShadowMediaExtractor}
 *
 * @author Fr Jeremy Krieg
 */
public class DataSource {
  private String dataSource;

  @SuppressWarnings("ObjectToString")
  private static final FileDescriptorTransform DEFAULT_FD_TRANSFORM =
      (fd, offset) -> fd.toString() + offset;

  private static FileDescriptorTransform fdTransform = DEFAULT_FD_TRANSFORM;

  /** Transform a {@link FileDescriptor} to a string. */
  public interface FileDescriptorTransform {
    String toString(FileDescriptor fd, long offset);
  }

  /**
   * Optional transformation for {@link FileDescriptor}.
   *
   * <p>Helpful for associating a real test file to the data source used by shadow objects in
   * stubbed methods.
   */
  public static void setFileDescriptorTransform(FileDescriptorTransform transform) {
    fdTransform = transform;
  }

  private DataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public static DataSource toDataSource(String path) {
    return new DataSource(path);
  }

  public static DataSource toDataSource(Context context, Uri uri) {
    return toDataSource(uri.toString());
  }

  public static DataSource toDataSource(Context context, Uri uri, Map<String, String> headers) {
    return toDataSource(context, uri);
  }

  public static DataSource toDataSource(
      Context context, Uri uri, Map<String, String> headers, List<HttpCookie> cookies) {
    return toDataSource(context, uri, headers);
  }

  public static DataSource toDataSource(String uri, Map<String, String> headers) {
    return toDataSource(uri);
  }

  public static DataSource toDataSource(FileDescriptor fd) {
    return toDataSource(fd, 0, 0);
  }

  public static DataSource toDataSource(MediaDataSource mediaDataSource) {
    return toDataSource("MediaDataSource");
  }

  public static DataSource toDataSource(AssetFileDescriptor assetFileDescriptor) {
    return toDataSource(
        "AssetFileDescriptor"
            + assetFileDescriptor.getStartOffset()
            + assetFileDescriptor.getLength());
  }

  public static DataSource toDataSource(FileDescriptor fd, long offset, long length) {
    return toDataSource(fdTransform.toString(fd, offset));
  }

  public static void reset() {
    fdTransform = DEFAULT_FD_TRANSFORM;
  }

  @Override
  public int hashCode() {
    return ((dataSource == null) ? 0 : dataSource.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || !(obj instanceof DataSource)) {
      return false;
    }
    final DataSource other = (DataSource) obj;
    if (dataSource == null) {
      if (other.dataSource != null) {
        return false;
      }
    } else if (!dataSource.equals(other.dataSource)) {
      return false;
    }
    return true;
  }
}
