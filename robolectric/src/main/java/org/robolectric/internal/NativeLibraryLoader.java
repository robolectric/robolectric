package org.robolectric.internal;

/**
 * Helper class to load a native library
 *
 * This class is intended to be used when you want to run System.loadLibrary on a different
 * classloader
 */
public class NativeLibraryLoader {

  private static String loadedLibrary;

  public NativeLibraryLoader(String libraryName) {
    if (loadedLibrary != null) {
      return;
    }
    System.loadLibrary(libraryName);
    loadedLibrary = libraryName;
  }
}
