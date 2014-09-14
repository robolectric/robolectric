package org.robolectric.shadows;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.google.android.collect.Maps;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;

@Implements(value = MediaMetadataRetriever.class, resetStaticState = true)
public class ShadowMediaMetadataRetriever {
  private String dataSource;
  private static final Map<String, HashMap<Integer, String>> metadata = Maps.newHashMap();
  private static final Map<String, HashMap<Long, Bitmap>> frames= Maps.newHashMap();

  @Implementation
  public void setDataSource(String path) {
    this.dataSource = path;
  }

  @Implementation
  public void setDataSource(Context context, Uri uri) {
    dataSource = uri.toString();
  }

  @Implementation
  public void setDataSource(String uri, Map<String, String> headers) {
    dataSource = uri;
  }

  @Implementation
  public void setDataSource(FileDescriptor fd, long offset, long length) {
    dataSource = fd.toString() + offset;
  }

  @Implementation
  public String extractMetadata(int keyCode) {
    if (metadata.containsKey(dataSource)) {
      return metadata.get(dataSource).get(keyCode);
    }
    return null;
  }

  @Implementation
  public Bitmap getFrameAtTime(long timeUs, int option) {
    return frames.get(dataSource).get(timeUs);
  }

  public static void addMetadata(String path, int keyCode, String value) {
    if (!metadata.containsKey(path)) {
      metadata.put(path, new HashMap<Integer, String>());
    }
    metadata.get(path).put(keyCode, value);
  }

  public static void addFrame(String path, long time, Bitmap bitmap) {
    if (!frames.containsKey(path)) {
      frames.put(path, new HashMap<Long, Bitmap>());
    }
    frames.get(path).put(time, bitmap);
  }

  public static void addFrame(Context context, Uri uri, long time, Bitmap bitmap) {
    String uriString = uri.toString();
    if (!frames.containsKey(uriString)) {
      frames.put(uriString, new HashMap<Long, Bitmap>());
    }
    frames.get(uriString).put(time, bitmap);
  }

  public static void addFrame(String uri, Map<String, String> headers, long time, Bitmap bitmap) {
    addFrame(null, Uri.parse(uri), time, bitmap);
  }

  public static void addFrame(FileDescriptor fd, long time, Bitmap bitmap) {
    addFrame(fd, 0, 0, time, bitmap);
  }

  public static void addFrame(FileDescriptor fd, long offset, long length,
                              long time, Bitmap bitmap) {
    String dataSource = fd.toString() + offset;
    if (!frames.containsKey(dataSource)) {
      frames.put(dataSource, new HashMap<Long, Bitmap>());
    }
    frames.get(dataSource).put(time, bitmap);
  }

  public static void reset() {
    metadata.clear();
    frames.clear();
  }
}
