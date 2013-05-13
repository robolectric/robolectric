package org.robolectric.res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewNode {
  private final String name;
  private final List<Attribute> attributes;
  private final XmlLoader.XmlContext xmlContext;

  private final List<ViewNode> children;
  private boolean requestFocusOverride = false;

  public ViewNode(String name, List<Attribute> attributes, XmlLoader.XmlContext xmlContext) {
    this(name, attributes, xmlContext, new ArrayList<ViewNode>(), false);
  }

  public ViewNode(String name, List<Attribute> attributes, XmlLoader.XmlContext xmlContext,
          List<ViewNode> children, boolean requestFocusOverride) {
    this.name = name;
    this.attributes = Collections.unmodifiableList(attributes);
    this.xmlContext = xmlContext;
    this.children = children;
    this.requestFocusOverride = requestFocusOverride;
  }

  public String getName() {
    return name;
  }

  public List<ViewNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public XmlLoader.XmlContext getXmlContext() {
    return xmlContext;
  }

  public void addChild(ViewNode viewNode) {
    children.add(viewNode);
  }

  public boolean isInclude() {
    return name.equals("include");
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return "ViewNode{" +
        "name='" + name + '\'' +
        '}';
  }

  void focusRequested() {
    requestFocusOverride = true;
  }

  public boolean shouldRequestFocusOverride() {
    return requestFocusOverride;
  }
}
