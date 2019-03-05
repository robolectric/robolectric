package org.robolectric.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SandboxObjectLoader {


  public static <T> T getInSandboxClassLoader(Class<T> objectClass, Object objectInParentLoader) {
    Object objectInSandboxLoader = SandboxObjectLoader.reloadInSandboxClassLoader(objectInParentLoader);
    return objectClass.cast(objectInSandboxLoader);
  }

  private static Object reloadInSandboxClassLoader(Object objectInParentLoader) {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
      out.writeObject(objectInParentLoader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] bytes = buf.toByteArray();

    // ObjectInputStream loads classes in the current classloader by magic
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
