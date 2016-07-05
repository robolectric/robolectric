package org.robolectric.res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PreferenceNode {
  private final List<PreferenceNode> children = new ArrayList<>();

  List<PreferenceNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  void addChild(PreferenceNode prefNode) {
    children.add(prefNode);
  }
}
