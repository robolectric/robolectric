package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;

public class ActivityData {
  private final String name;
  private final String themeRef;
  private final List<String> intentFilterActions = new ArrayList<String>();

  public ActivityData(String name, String themeRef) {
    this.name = name;
    this.themeRef = themeRef;
  }

  public String getName() {
    return name;
  }

  public String getThemeRef() {
    return themeRef;
  }

  public List<String> getIntentFilterActions() {
    return intentFilterActions;
  }
}
