package org.robolectric.res;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.util.Logger;

public class ResBundle {
  private final ResMap valuesMap = new ResMap();

  public void put(ResName resName, TypedResource value) {
    valuesMap.put(resName, value);
  }

  public TypedResource get(ResName resName, String qualifiers) {
    return valuesMap.pick(resName, qualifiers);
  }

  public void receive(ResourceTable.Visitor visitor) {
    for (final Map.Entry<ResName, List<TypedResource>> entry : valuesMap.map.entrySet()) {
      visitor.visit(entry.getKey(), entry.getValue());
    }
  }

  static class ResMap {
    private final Map<ResName, List<TypedResource>> map = new HashMap<>();

    public TypedResource pick(ResName resName, String qualifiersStr) {
      List<TypedResource> values = map.get(resName);
      if (values == null || values.size() == 0) return null;

      ResTable_config toMatch = new ResTable_config();
      if (!Strings.isNullOrEmpty(qualifiersStr) &&
          !new ConfigDescription().parse(qualifiersStr, toMatch, false)) {
        throw new IllegalArgumentException("Invalid qualifiers \"" + qualifiersStr + "\"");
      }

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
            bestMatchSoFar == null ? "<none>" : bestMatchSoFar.getXmlContext().getQualifiers(),
            resName.getFullyQualifiedName(),
            qualifiersStr,
            values.size());
      }
      return bestMatchSoFar;
    }

    public void put(ResName resName, TypedResource value) {
      List<TypedResource> values = map.computeIfAbsent(resName, k -> new ArrayList<>());
      values.add(value);
    }

    public int size() {
      return map.size();
    }
  }
}
