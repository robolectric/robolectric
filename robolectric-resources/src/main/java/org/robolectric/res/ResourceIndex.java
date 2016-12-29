package org.robolectric.res;

public interface ResourceIndex {
  Integer getResourceId(ResName resName);

  ResName getResName(int resourceId);

  void dump();
}
