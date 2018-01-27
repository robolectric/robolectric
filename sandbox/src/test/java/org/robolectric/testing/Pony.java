package org.robolectric.testing;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;

@Instrument
public class Pony {
  public Pony() {
  }

  public String ride(String where) {
    return "Whinny! You're on my " + where + "!";
  }

  public static String prance(String where) {
    return "I'm prancing to " + where + "!";
  }

  public String saunter(String where) {
    return "Off I saunter to " + where + "!";
  }

  @Implements(Pony.class)
  public static class ShadowPony {
    @Implementation
    public String ride(String where) {
      return "Fake whinny! You're on my " + where + "!";
    }

    @Implementation
    protected static String prance(String where) {
      return "I'm shadily prancing to " + where + "!";
    }
  }
}
