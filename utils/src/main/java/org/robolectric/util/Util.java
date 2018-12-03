package org.robolectric.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic collection of utility methods.
 */
public class Util {

  public static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8196];
    int len;
    try {
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
    } finally {
      in.close();
    }
  }

  /**
   * This method consumes an input stream and returns its content.
   *
   * @param is The input stream to read from.
   * @return The bytes read from the stream.
   * @throws IOException Error reading from stream.
   */
  public static byte[] readBytes(InputStream is) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available())) {
      copy(is, bos);
      return bos.toByteArray();
    }
  }

  public static <T> T[] reverse(T[] array) {
    for (int i = 0; i < array.length / 2; i++) {
      int destI = array.length - i - 1;
      T o = array[destI];
      array[destI] = array[i];
      array[i] = o;
    }
    return array;
  }

  public static File file(String... pathParts) {
    return file(new File("."), pathParts);
  }

  public static File file(File f, String... pathParts) {
    for (String pathPart : pathParts) {
      f = new File(f, pathPart);
    }

    String dotSlash = "." + File.separator;
    if (f.getPath().startsWith(dotSlash)) {
      f = new File(f.getPath().substring(dotSlash.length()));
    }

    return f;
  }

  private static final Pattern WINDOWS_UNC_RE =
      Pattern.compile("^\\\\\\\\(?<host>[^\\\\]+)\\\\(?<path>.*)$");
  private static final Pattern WINDOWS_LOCAL_RE =
      Pattern.compile("^(?<volume>[A-Za-z]:)\\\\(?<path>.*)$");

  @SuppressWarnings("NewApi")
  public static URL url(String osPath) throws MalformedURLException {
    // We should just use Paths.get(path).toUri().toURL() here, but impossible to test Windows'
    // behavior on Linux (for CI), and this code is going away soon anyway so who cares.

    // Starts with double backslash, is likely a UNC path
    Matcher windowsUncMatcher = WINDOWS_UNC_RE.matcher(osPath);
    if (windowsUncMatcher.find()) {
      String host = windowsUncMatcher.group("host");
      String path = windowsUncMatcher.group("path").replace('\\', '/');
      return new URL("file://" + host + "/" + path.replace(" ", "%20"));
    }

    Matcher windowsLocalMatcher = WINDOWS_LOCAL_RE.matcher(osPath);
    if (windowsLocalMatcher.find()) {
      String volume = windowsLocalMatcher.group("volume");
      String path = windowsLocalMatcher.group("path").replace('\\', '/');
      // this doesn't correspend to what M$ says, but, again, who cares.
      return new URL("file:" + volume + "/" + path.replace(" ", "%20"));
    }

    return new URL("file:/" + (osPath.startsWith("/") ? "/" + osPath : osPath));
  }

  public static List<Integer> intArrayToList(int[] ints) {
    List<Integer> youSuckJava = new ArrayList<>();
    for (int attr1 : ints) {
      youSuckJava.add(attr1);
    }
    return youSuckJava;
  }

  public static int parseInt(String valueFor) {
    if (valueFor.startsWith("0x")) {
      return Integer.parseInt(valueFor.substring(2), 16);
    } else {
      return Integer.parseInt(valueFor, 10);
    }
  }
}
