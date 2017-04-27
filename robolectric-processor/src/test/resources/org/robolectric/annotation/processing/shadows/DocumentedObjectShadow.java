package org.robolectric.annotation.processing.shadows;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

import java.util.Map;

/**
 * Robolectric Javadoc goes here!
 */
@Implements(value = Robolectric.DocumentedObject.class)
public class DocumentedObjectShadow {
  /**
   * Docs for shadow method go here!
   */
  @Implementation
  public String getSomething(int index, Map<String, String> defaultValue) {
    return null;
  }

  public enum SomeEnum {
    VALUE1, VALUE2
  }
}
