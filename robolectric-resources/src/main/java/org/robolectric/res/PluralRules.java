package org.robolectric.res;

import java.util.List;

public class PluralRules {
  private final List<Plural> data;

  public PluralRules(List<Plural> data) {
    this.data = data;
  }

  public Plural find(int quantity) {
    for (Plural p : data) {
      if (p.num == quantity) return p;
    }
    for (Plural p : data) {
      if (p.num == -1) return p;
    }
    return null;
  }
}
