package org.robolectric.res;

public class Plural {
  final String quantity, string;
  final int num;

  Plural(String quantity, String string) {
    this.quantity = quantity;
    this.string = string;
    if ("zero".equals(quantity)) {
      num = 0;
    } else if ("one".equals(quantity)) {
      num = 1;
    } else if ("two".equals(quantity)) {
      num = 2;
    } else if ("other".equals(quantity)) {
      num = -1;
    } else {
      num = -1;
    }
  }

  public String getString() {
    return string;
  }
}
