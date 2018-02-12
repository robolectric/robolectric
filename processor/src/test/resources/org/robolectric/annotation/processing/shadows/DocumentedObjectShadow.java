package org.robolectric.annotation.processing.shadows;

import java.util.Map;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric Javadoc goes here!
 */
@Implements(value = Robolectric.DocumentedObject.class)
public class DocumentedObjectShadow {
  /**
   * Docs for shadow method go here!
   */
  @Implementation
  protected String getSomething(int index, Map<String, String> defaultValue) {
    return null;
  }

  public enum SomeEnum {
    VALUE1, VALUE2
  }
}
