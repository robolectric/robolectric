package org.robolectric.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.util.Logger;

public class ResBundle {
  private final ResMap valuesMap = new ResMap();

  public void put(ResName resName, TypedResource value) {
    valuesMap.put(resName, value);
  }

  public TypedResource get(ResName resName, ResTable_config config) {
    return valuesMap.pick(resName, config);
  }

  public void receive(ResourceTable.Visitor visitor) {
    for (final Map.Entry<ResName, List<TypedResource>> entry : valuesMap.map.entrySet()) {
      visitor.visit(entry.getKey(), entry.getValue());
    }
  }

  static class ResMap {
    private final Map<ResName, List<TypedResource>> map = new HashMap<>();

    public TypedResource pick(ResName resName, ResTable_config toMatch) {
      List<TypedResource> values = map.get(resName);
      if (values == null || values.size() == 0) return null;

      TypedResource bestMatchSoFar = null;
      for (TypedResource candidate : values) {
        ResTable_config candidateConfig = candidate.getConfig();
        if (candidateConfig.match(toMatch)) {
          if (bestMatchSoFar == null || candidateConfig.isBetterThan(bestMatchSoFar.getConfig(), toMatch)) {
            bestMatchSoFar = candidate;
          }
        }
      }

      if (Logger.loggingEnabled()) {
        Logger.debug("Picked '%s' for %s for qualifiers '%s' (%d candidates)",
            bestMatchSoFar == null ? "<none>" : bestMatchSoFar.getXmlContext().getQualifiers().toString(),
            resName.getFullyQualifiedName(),
            toMatch,
            values.size());
      }
      return bestMatchSoFar;
    }

    public void put(ResName resName, TypedResource value) {
      if (!map.containsKey(resName)) {
        map.put(resName, new ArrayList<>());
      }

      map.get(resName).add(value);
    }

    public int size() {
      return map.size();
    }
  }
}
