package org.robolectric.res;

public class ActivityData {
  private final String name;
  private final String themeRef;

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
}
