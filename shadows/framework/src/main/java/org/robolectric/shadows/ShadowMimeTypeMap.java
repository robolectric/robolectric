package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow for webkit's MimeTypeMap.
 *
 * <p>Only needed for APIs < 31. Above that the real mime type mappings are used.
 *
 * @deprecated run on Android APIs >= S, which allows the real Android mime type data to be used
 */
@Deprecated
@Implements(value = MimeTypeMap.class)
public class ShadowMimeTypeMap {

  private static final String LOG_TAG = "ShadowMimeTypeMap";
  private final Map<String, String> extensionToMimeTypeMap = new HashMap<>();
  private final Map<String, String> mimeTypeToExtensionMap = new HashMap<>();
  private static volatile MimeTypeMap singleton = null;
  private static final Object singletonLock = new Object();

  @ReflectorObject private MimeTypeMapReflector mimeTypeMapReflector;
  private volatile boolean hasClearedMappings = false;

  @Implementation
  protected static MimeTypeMap getSingleton() {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R) {
      if (singleton == null) {
        synchronized (singletonLock) {
          if (singleton == null) {
            singleton = Shadow.newInstanceOf(MimeTypeMap.class);
          }
        }
        ShadowMimeTypeMap shadowMimeTypeMap = Shadow.extract(singleton);
        // add some basic mime type maps for now
        shadowMimeTypeMap.addExtensionMimeTypeMapping("3gp", "video/3gpp");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("3gpp", "video/3gpp");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("mp4", "video/mp4");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("jpg", "image/jpeg");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("jpeg", "image/jpeg");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("gif", "image/gif");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("png", "image/png");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("m4a", "audio/mpeg");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("mp3", "audio/mpeg");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("zip", "application/zip");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("txt", "text/plain");
        shadowMimeTypeMap.addExtensionMimeTypeMapping("pdf", "application/pdf");
        shadowMimeTypeMap.addExtensionMimeTypeMapping(
            "apk", "application/vnd.android.package-archive");
      }

      return singleton;
    }
    return reflector(MimeTypeMapReflector.class).getSingleton();
  }

  @Implementation
  protected String getMimeTypeFromExtension(String extension) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R || hasClearedMappings) {
      return extensionToMimeTypeMap.get(extension);
    } else {
      return mimeTypeMapReflector.getMimeTypeFromExtension(extension);
    }
  }

  @Implementation
  protected String getExtensionFromMimeType(String mimeType) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R || hasClearedMappings) {
      return mimeTypeToExtensionMap.get(mimeType);
    } else {
      return mimeTypeMapReflector.getExtensionFromMimeType(mimeType);
    }
  }

  /**
   * Adds a mapping between an extension and a mime type.
   *
   * <p>Will be ignored when running on SDKs >= S, where a full mime type mapping is already
   * present.
   */
  public void addExtensionMimeTypeMapping(String extension, String mimeType) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R || hasClearedMappings) {
      extensionToMimeTypeMap.put(extension, mimeType);
      mimeTypeToExtensionMap.put(mimeType, extension);
    } else {
      Log.w(
          LOG_TAG,
          String.format(
              "Ignoring addExtensionMimeTypeMapping, API %d has full MimeTypeMap support",
              RuntimeEnvironment.getApiLevel()));
    }
  }

  /**
   * Will clear all built-in mime type mappings, so future calls to APIS like
   * getExtensionFromMimeType will only return data provided manually via
   * addExtensionMimeTypeMapping.
   *
   * <p>Use of this API is not recommended! It is only present to support legacy, improperly written
   * tests that fail with valid mime type mappings.
   */
  public void clearMappings() {
    hasClearedMappings = true;
    extensionToMimeTypeMap.clear();
    mimeTypeToExtensionMap.clear();
  }

  @Implementation
  protected boolean hasExtension(String extension) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R || hasClearedMappings) {
      return extensionToMimeTypeMap.containsKey(extension);
    } else {
      return mimeTypeMapReflector.hasExtension(extension);
    }
  }

  @Implementation
  protected boolean hasMimeType(String mimeType) {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.R || hasClearedMappings) {
      return mimeTypeToExtensionMap.containsKey(mimeType);
    } else {
      return mimeTypeMapReflector.hasMimeType(mimeType);
    }
  }

  @ForType(MimeTypeMap.class)
  private interface MimeTypeMapReflector {

    @Static
    @Direct
    MimeTypeMap getSingleton();

    @Direct
    String getMimeTypeFromExtension(String extension);

    @Direct
    String getExtensionFromMimeType(String mimeType);

    @Direct
    boolean hasExtension(String extension);

    @Direct
    boolean hasMimeType(String mimeType);
  }
}
