package org.robolectric.res;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ResourceIndex {
  protected final Map<ResName, Integer> resourceNameToId = new HashMap<>();
  protected final Map<Integer, ResName> resourceIdToResName = new HashMap<>();

  public abstract Integer getResourceId(ResName resName);

  public abstract ResName getResName(int resourceId);

  public abstract Collection<String> getPackages();
}
