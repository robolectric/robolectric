package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.robolectric.res.android.ResTable_config;

public class ResBunch {
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

  public TypedResource get(@Nonnull ResName resName, ResTable_config config) {
    ResBundle bundle = getBundle(resName.type);
    return bundle.get(resName, config);
  }

  void receive(ResourceTable.Visitor visitor) {
    for (ResBundle resBundle : types.values()) {
      resBundle.receive(visitor);
    }
  }
}
