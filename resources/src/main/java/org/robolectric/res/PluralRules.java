package org.robolectric.res;

import java.util.List;

public class PluralRules extends TypedResource<List<Plural>> {
  public PluralRules(List<Plural> data, ResType resType, XmlContext xmlContext) {
    super(data, resType, xmlContext);
  }

  public Plural find(int quantity) {
    for (Plural p : getData()) {
      if (p.num == quantity && p.usedInEnglish) return p;
    }
    for (Plural p : getData()) {
      if (p.num == -1) return p;
    }
    return null;
  }
}
