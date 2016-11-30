package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ResBunch {
  private final Map<String, ResBundle> types = new LinkedHashMap<>();

  public void put(String attrType, String name, TypedResource value) {
    ResBundle bundle = getBundle(attrType);
    bundle.put(attrType, name, value);
  }

  private ResBundle getBundle(String attrType) {
    ResBundle bundle = types.get(attrType);
    if (bundle == null) {
      bundle = new ResBundle();
      types.put(attrType, bundle);
    }
    return bundle;
  }

  public TypedResource get(@NotNull ResName resName, String qualifiers) {
    ResBundle bundle = getBundle(resName.type);
    return bundle.get(resName, qualifiers);
  }

  public int size() {
    int size = 0;
    for (ResBundle bundle : types.values()) {
      size += bundle.size();
    }
    return size;
  }

  public void makeImmutable() {
    for (ResBundle bundle : types.values()) {
      bundle.makeImmutable();
    }
  }

  public void mergeLibraryStyle(ResBunch fromResBundle, String packageName) {
    for (Map.Entry<String, ResBundle> entry : fromResBundle.types.entrySet()) {
      getBundle(entry.getKey()).mergeLibraryStyle(entry.getValue(), packageName);
    }
  }

  public void receive(ResourceLoader.Visitor visitor) {
    for (ResBundle resBundle : types.values()) {
      resBundle.receive(visitor);
    }
  }
}
