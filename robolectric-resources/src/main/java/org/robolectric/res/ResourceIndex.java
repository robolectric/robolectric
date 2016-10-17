package org.robolectric.res;

import java.util.Collection;
import java.util.Map;

public interface ResourceIndex {
  Integer getResourceId(ResName resName);

  ResName getResName(int resourceId);

  Collection<String> getPackages();

  Map<ResName, Integer> getAllIdsByResName();

  Map<Integer, ResName> getAllResNamesById();
}
