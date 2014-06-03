package org.robolectric.shadows;

import java.util.Map;
import java.util.HashMap;
import com.google.android.collect.Maps;
import android.media.MediaMetadataRetriever;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;

@Implements(MediaMetadataRetriever.class)
public class ShadowMediaMetadataRetriever {
  private String path;
  private static final Map<String, HashMap<Integer, String>> metadata = Maps.newHashMap();

  @Implementation
  public void setDataSource(String path) {
    this.path = path;
  }

  @Implementation
  public String extractMetadata(int keyCode) {
    if (metadata.containsKey(path)) {
      return metadata.get(path).get(keyCode);
    }
    return null;
  }

  public static void addMetadata(String path, int keyCode, String value) {
    if (!metadata.containsKey(path)) {
      metadata.put(path, new HashMap<Integer, String>());
    }
    metadata.get(path).put(keyCode, value);
  }

  public static void reset() {
    metadata.clear();
  }
}
