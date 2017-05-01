package org.robolectric.shadows;


import libcore.io.IoUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Implements(value = IoUtils.class, isInAndroidSdk = false)
public class ShadowIoUtils {

  @Implementation
  public static String readFileAsString(String absolutePath) throws IOException {
    return new String(Files.readAllBytes(Paths.get(absolutePath)));
  }
}
