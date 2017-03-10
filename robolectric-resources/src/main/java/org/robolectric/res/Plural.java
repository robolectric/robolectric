package org.robolectric.res;

import java.io.Serializable;

public class Plural implements Serializable {
  private static final long serialVersionUID = 42L;

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

  @Override
  public String toString() {
    return quantity + "(" + num + "): " + string;
  }
}
