package org.robolectric.res;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ResourceIndex {
  protected final Map<ResName, Integer> resourceNameToId = new HashMap<ResName, Integer>();
  protected final Map<Integer, ResName> resourceIdToResName = new HashMap<Integer, ResName>();

  public abstract Integer getResourceId(ResName resName);

  public String getResourceName(int resourceId) {
    ResName resName = getResName(resourceId);
    return (resName != null) ? resName.getFullyQualifiedName() : null;
  }

  public abstract ResName getResName(int resourceId);

  public abstract Collection<String> getPackages();
}
