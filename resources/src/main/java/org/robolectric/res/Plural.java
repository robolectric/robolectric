package org.robolectric.res;

public class Plural {
  final String quantity, string;
  final int num;
  final boolean usedInEnglish;

  Plural(String quantity, String string) {
    this.quantity = quantity;
    this.string = string;
    if ("zero".equals(quantity)) {
      num = 0;
      usedInEnglish = false;
    } else if ("one".equals(quantity)) {
      num = 1;
      usedInEnglish = true;
    } else if ("two".equals(quantity)) {
      num = 2;
      usedInEnglish = false;
    } else if ("other".equals(quantity)) {
      num = -1;
      usedInEnglish = true;
    } else {
      num = -1;
      usedInEnglish = true;
    }
  }

  public String getString() {
    return string;
  }

  @Override
  public String toString() {
    return quantity + "(" + num + "): " + string;
  }
}
