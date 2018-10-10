package org.robolectric.annotation.processing.shadows;

import java.util.Map;
import org.robolectric.DocumentedObjectOuter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** DocumentedObjectOuter Javadoc goes here! */
@Implements(value = DocumentedObjectOuter.DocumentedObject.class)
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
