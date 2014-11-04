package org.robolectric.res;

import com.google.android.collect.Lists;
import org.robolectric.AndroidManifest;

import java.util.List;

public class BroadcastReceiverData {
  private final List<String> actions;
  private final String className;
  private final AndroidManifest.MetaData metaData;

  public BroadcastReceiverData(String className, AndroidManifest.MetaData metaData) {
    this.actions = Lists.newArrayList();
    this.className = className;
    this.metaData = metaData;
  }

  public String getClassName() {
    return className;
  }

  public List<String> getActions() {
    return actions;
  }

  public AndroidManifest.MetaData getMetaData() {
    return metaData;
  }

  public void addAction(String action) {
    this.actions.add(action);
  }
}
