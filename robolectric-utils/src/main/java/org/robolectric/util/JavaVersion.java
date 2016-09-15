package org.robolectric.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaVersion implements Comparable<JavaVersion> {
  private final List<Integer> versions;

  public JavaVersion(String version) {
    versions = new ArrayList<>();
    Scanner s = new Scanner(version).useDelimiter("[^\\d]+");
    while (s.hasNext()) {
      versions.add(s.nextInt());
    }
  }

  @Override public int compareTo(JavaVersion o) {
    List<Integer> versions2 = o.versions;
    int max = Math.min(versions.size(), versions2.size());
    for (int i = 0; i < max; i++) {
      int compare = versions.get(i).compareTo(versions2.get(i));
      if (compare != 0) {
        return compare;
      }
    }

    // Assume longer is newer
    return Integer.compare(versions.size(), versions2.size());
  }
}
