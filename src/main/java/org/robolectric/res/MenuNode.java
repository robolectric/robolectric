package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public class MenuNode {
  private final String name;
  private final List<Attribute> attributes;
  private final List<MenuNode> children = new ArrayList<MenuNode>();

  public MenuNode(String name, List<Attribute> attributes) {
    this.name = name;
    this.attributes = attributes;
  }

  public String getName() {
    return name;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public List<MenuNode> getChildren() {
    return children;
  }

  public void addChild(MenuNode MenuNode) {
    children.add(MenuNode);
  }

  public boolean isSubMenuItem() {
    return children != null && children.size() == 1 && "menu".equals(children.get(0).name);
  }
}
