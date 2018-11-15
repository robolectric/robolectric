package org.robolectric.shadows;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Build;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import libcore.io.IoUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = IoUtils.class, isInAndroidSdk = false)
public class ShadowIoUtils {

  @Implementation
  public static String readFileAsString(String absolutePath) throws IOException {
    return new String(Files.readAllBytes(Paths.get(absolutePath)), UTF_8);
  }

  // BEGIN-INTERNAL
  @Implementation(minSdk = Build.VERSION_CODES.Q)
  protected static void setFdOwner(FileDescriptor fd, Object owner) {
    // ignore, fails in JVM environment
  }
  // END-INTERNAL
}
