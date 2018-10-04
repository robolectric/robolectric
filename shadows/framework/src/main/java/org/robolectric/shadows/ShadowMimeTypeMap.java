package org.robolectric.shadows;

import android.webkit.MimeTypeMap;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

@Implements(MimeTypeMap.class)
public class ShadowMimeTypeMap {
  private final Map<String, String> extensionToMimeTypeMap = new HashMap<>();
  private final Map<String, String> mimeTypeToExtensionMap = new HashMap<>();
  private static volatile MimeTypeMap singleton = null;
  private static final Object singletonLock = new Object();

  @Implementation
  protected static MimeTypeMap getSingleton() {
    if (singleton == null) {
      synchronized (singletonLock) {
        if (singleton == null) {
          singleton = Shadow.newInstanceOf(MimeTypeMap.class);
        }
      }
    }

    return singleton;
  }

  @Resetter
  public static void reset() {
    if (singleton != null) {
      ShadowMimeTypeMap shadowMimeTypeMap = Shadow.extract(getSingleton());
      shadowMimeTypeMap.clearMappings();
    }
  }

  @Implementation
  protected String getMimeTypeFromExtension(String extension) {
    if (extensionToMimeTypeMap.containsKey(extension))
      return extensionToMimeTypeMap.get(extension);

    return null;
  }

  @Implementation
  protected String getExtensionFromMimeType(String mimeType) {
    if (mimeTypeToExtensionMap.containsKey(mimeType))
      return mimeTypeToExtensionMap.get(mimeType);

    return null;
  }

  public void addExtensionMimeTypMapping(String extension, String mimeType) {
    extensionToMimeTypeMap.put(extension, mimeType);
    mimeTypeToExtensionMap.put(mimeType, extension);
  }

  public void clearMappings() {
    extensionToMimeTypeMap.clear();
    mimeTypeToExtensionMap.clear();
  }

  @Implementation
  protected boolean hasExtension(String extension) {
    return extensionToMimeTypeMap.containsKey(extension);
  }

  @Implementation
  protected boolean hasMimeType(String mimeType) {
    return mimeTypeToExtensionMap.containsKey(mimeType);
  }
}
