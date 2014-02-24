package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public class IntentFilterData {
  private final List<String> actions;
  private final List<String> categories;

  public IntentFilterData(List<String> actions, List<String> categories) {
    this.actions = actions;
    this.categories = new ArrayList<String>(categories);
  }

  public List<String> getActions() {
    return actions;
  }

  public List<String> getCategories() {
    return categories;
  }
}
