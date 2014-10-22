package org.robolectric.res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreferenceNode {
  private final String name;
  private final List<Attribute> attributes;
  private final List<PreferenceNode> children = new ArrayList<PreferenceNode>();

  public PreferenceNode(String name, List<Attribute> attributes) {
    this.name = name;
    this.attributes = attributes;
  }

  public String getName() {
    return name;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public List<PreferenceNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public void addChild(PreferenceNode prefNode) {
    children.add(prefNode);
  }
}
