package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.List;

public class BroadcastReceiverData {
  private final String className;
  private final MetaData metaData;
  private final List<String> actions;

  public BroadcastReceiverData(String className, MetaData metaData) {
    this.actions = new ArrayList<>();
    this.className = className;
    this.metaData = metaData;
  }

  public String getClassName() {
    return className;
  }

  public List<String> getActions() {
    return actions;
  }

  public MetaData getMetaData() {
    return metaData;
  }

  public void addAction(String action) {
    this.actions.add(action);
  }
}
