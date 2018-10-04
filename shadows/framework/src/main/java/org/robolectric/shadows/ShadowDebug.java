package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.L;
import static android.os.Build.VERSION_CODES.M;

import android.os.Debug;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(Debug.class)
public class ShadowDebug {

  private static boolean tracingStarted = false;
  private static String tracingFilename;

  @Implementation
  protected static void __staticInitializer__() {
    // Avoid calling Environment.getLegacyExternalStorageDirectory()
  }

  @Implementation
  protected static long getNativeHeapAllocatedSize() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  @Implementation(minSdk = M)
  protected static Map<String, String> getRuntimeStats() {
    return ImmutableMap.<String, String>builder().build();
  }

  @Implementation
  protected static void startMethodTracing() {
    internalStartTracing(fixTracePath(null));
  }

  @Implementation
  protected static void startMethodTracing(String tracePath, int bufferSize, int flags) {
    internalStartTracing(fixTracePath(tracePath));
  }

  @Implementation
  protected static void startMethodTracing(String tracePath) {
    internalStartTracing(fixTracePath(tracePath));
  }

  @Implementation
  protected static void startMethodTracing(String tracePath, int bufferSize) {
    internalStartTracing(fixTracePath(tracePath));
  }

  @Implementation(minSdk = L)
  protected static void startMethodTracingSampling(String tracePath, int bufferSize, int intervalUs) {
    internalStartTracing(fixTracePath(tracePath));
  }

  @Implementation
  protected static void stopMethodTracing() {
    if (!tracingStarted) {
      throw new RuntimeException("Tracing is not started.");
    }

    try {
      Files.asCharSink(new File(tracingFilename), Charset.forName("UTF-8")).write("trace data");
    } catch (IOException e) {
      throw new RuntimeException("Writing trace file failed", e);
    }
    tracingStarted = false;
    tracingFilename = null;
  }

  private static void internalStartTracing(String tracePath) {
    if (tracingStarted) {
      throw new RuntimeException("Tracing is already started.");
    }
    tracingStarted = true;
    tracingFilename = tracePath;
  }

  @Resetter
  public static void reset() {
    tracingStarted = false;
    tracingFilename = null;
  }

  // Forked from android.os.Debug
  private static String fixTracePath(String tracePath) {
    String defaultTraceBody = "dmtrace";
    String defaultTraceExtension = ".trace";

    if (tracePath == null || tracePath.charAt(0) != '/') {
      final File dir = RuntimeEnvironment.application.getExternalFilesDir(null);
      if (tracePath == null) {
        tracePath = new File(dir, defaultTraceBody).getAbsolutePath();
      } else {
        tracePath = new File(dir, tracePath).getAbsolutePath();
      }
    }
    if (!tracePath.endsWith(defaultTraceExtension)) {
      tracePath += defaultTraceExtension;
    }
    return tracePath;
  }
}
