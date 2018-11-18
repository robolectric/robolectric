package org.robolectric.shadows.util;

import android.content.Context;
import android.net.Uri;
import java.io.FileDescriptor;
import java.util.Map;

/**
 * Opaque class for uniquely identifying a media data source,
 * as used by {@link org.robolectric.shadows.ShadowMediaPlayer}
 * and {@link org.robolectric.shadows.ShadowMediaMetadataRetriever}.
 *
 * @author Fr Jeremy Krieg
 */
public class DataSource {
  private String dataSource;

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

  public static DataSource toDataSource(String uri, Map<String, String> headers) {
    return toDataSource(uri);
  }

  public static DataSource toDataSource(FileDescriptor fd) {
    return toDataSource(fd, 0, 0);
  }

  @SuppressWarnings("ObjectToString")
  public static DataSource toDataSource(FileDescriptor fd, long offset, long length) {
    return toDataSource(fd.toString() + offset);
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
