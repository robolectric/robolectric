package org.robolectric.internal;

import com.thoughtworks.xstream.XStream;

/**
 * The purpose of the deep cloner is to create a deep clone of an object. An
 * object can also be cloned to a different class-loader.
 *
 * Based on PowerMock's DeepCloner class.
 */
public class DeepCloner {
  private final XStream xStream;

  /**
   * Clone using the supplied ClassLoader.
   */
  public DeepCloner(ClassLoader classLoader) {
    xStream = new XStream();
    xStream.setClassLoader(classLoader);
  }

  /**
   * Clones an object.
   *
   * @return A deep clone of the object to clone.
   */
  public <T> T clone(T objectToClone) {
    final String serialized = xStream.toXML(objectToClone);
    return (T) xStream.fromXML(serialized);
  }
}
