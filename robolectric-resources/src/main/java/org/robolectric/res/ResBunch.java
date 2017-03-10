package org.robolectric.res;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ResBunch implements Serializable {
  private static final long serialVersionUID = 42L;

  private final Map<String, ResBundle> types = new LinkedHashMap<>();

  public void put(ResName resName, TypedResource value) {
    ResBundle bundle = getBundle(resName.type);
    bundle.put(resName, value);
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

  void receive(ResourceTable.Visitor visitor) {
    for (ResBundle resBundle : types.values()) {
      resBundle.receive(visitor);
    }
  }
}
