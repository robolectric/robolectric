package org.robolectric.doclet;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import com.sun.tools.doclets.standard.Standard;

import java.util.Map;

public class RoboTaglet implements Taglet {
  /**
   * Register this Taglet.
   * @param tagletMap  the map to register this tag to.
   */
  public static void register(Map tagletMap) {
    RoboTaglet tag = new RoboTaglet();
    Taglet t = (Taglet) tagletMap.get(tag.getName());
    if (t != null) {
      tagletMap.remove(tag.getName());
    }
    tagletMap.put(tag.getName(), tag);
  }

  @Override
  public boolean inField() {
    return false;
  }

  @Override
  public boolean inConstructor() {
    return false;
  }

  @Override
  public boolean inMethod() {
    return false;
  }

  @Override
  public boolean inOverview() {
    return false;
  }

  @Override
  public boolean inPackage() {
    return false;
  }

  @Override
  public boolean inType() {
    return false;
  }

  @Override
  public boolean isInlineTag() {
    return false;
  }

  @Override
  public String getName() {
    return "strategy";
  }

  @Override
  public String toString(Tag tag) {
    return "YAARRRR";
  }

  @Override
  public String toString(Tag[] tags) {
    return "YO)ZRZ";
  }
}
