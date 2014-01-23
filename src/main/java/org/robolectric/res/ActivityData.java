package org.robolectric.res;

import java.util.List;

public class ActivityData {
  private final String name;
  private final String label;
  private final String themeRef;
  private final List<IntentFilterData> intentFilter;

  public ActivityData(String name, String label, String themeRef, List<IntentFilterData> intentFilterData) {
    this.name = name;
    this.label = label;
    this.themeRef = themeRef;
    this.intentFilter = intentFilterData;
  }

  public String getName() {
    return name;
  }

  public String getLabel() {
    return label;
  }

  public String getThemeRef() {
    return themeRef;
  }

  public List<IntentFilterData> getIntentFilters() {
    return intentFilter;
  }
}
