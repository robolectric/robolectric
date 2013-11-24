package org.robolectric.res;

public class ActivityData {
  private final String name;
  private final String label;
  private final String themeRef;

  public ActivityData(String name, String label, String themeRef) {
    this.name = name;
    this.label = label;
    this.themeRef = themeRef;
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
}
