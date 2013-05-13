package org.robolectric.res;

import org.junit.Test;
import org.robolectric.util.Join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResourceRemapperTest {

  private ResourceRemapper resourceRemapper = new ResourceRemapper();

  @Test
  public void shouldRemapNonFinalIntsInRClasses() throws Exception {
    resetInitialState();

    resourceRemapper.remapRClass(android.R.class);
    resourceRemapper.remapRClass(org.robolectric.lib1.R.class);
    resourceRemapper.remapRClass(org.robolectric.lib2.R.class);
    resourceRemapper.remapRClass(org.robolectric.lib3.R.class);

    assertUnique(
        org.robolectric.lib1.R.id.lib_button,
        org.robolectric.lib2.R.id.lib_button,
        org.robolectric.lib3.R.id.lib_button);

    assertUnique(
        org.robolectric.lib1.R.id.lib1_button,
        org.robolectric.lib2.R.id.lib2_button,
        org.robolectric.lib3.R.id.lib3_button);

    assertUnique(
        org.robolectric.lib1.R.attr.offsetX,
        org.robolectric.lib2.R.attr.offsetX,
        org.robolectric.lib3.R.attr.offsetX);

    assertUnique(
        org.robolectric.lib1.R.attr.offsetY,
        org.robolectric.lib2.R.attr.offsetY,
        org.robolectric.lib3.R.attr.offsetY);

    assertEquals(asIntList(org.robolectric.lib1.R.styleable.Image),
        asList(org.robolectric.lib1.R.attr.offsetX, org.robolectric.lib1.R.attr.offsetY));

    assertEquals(asIntList(org.robolectric.lib2.R.styleable.Image),
        asList(org.robolectric.lib2.R.attr.offsetX, org.robolectric.lib2.R.attr.offsetY));

    assertEquals(asIntList(org.robolectric.lib3.R.styleable.Image),
        asList(org.robolectric.lib3.R.attr.offsetX, org.robolectric.lib3.R.attr.offsetY));

    assertEquals(1, org.robolectric.lib1.R.styleable.one);
    assertEquals(2, org.robolectric.lib1.R.styleable.two);
  }

  private void assertUnique(int... values) {
    HashSet<Integer> integers = new HashSet<Integer>();
    for (int value : values) {
      if (!integers.add(value)) {
        fail(Join.join(", ", asIntList(values)) + " contained " + value + " twice");
      }
    }
  }

  private void resetInitialState() {
    org.robolectric.lib1.R.id.lib_button = 0x7f010001;
    org.robolectric.lib2.R.id.lib_button = 0x7f010001;
    org.robolectric.lib3.R.id.lib_button = 0x7f010001;

    org.robolectric.lib1.R.id.lib1_button = 0x7f010002;
    org.robolectric.lib2.R.id.lib2_button = 0x7f010002;
    org.robolectric.lib3.R.id.lib3_button = 0x7f010002;

    org.robolectric.lib1.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};
    org.robolectric.lib2.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};
    org.robolectric.lib3.R.styleable.Image = new int[] {0x7f010070, 0x7f010071};

    org.robolectric.lib1.R.attr.offsetX = 0x7f010070;
    org.robolectric.lib2.R.attr.offsetX = 0x7f010070;
    org.robolectric.lib3.R.attr.offsetX = 0x7f010070;

    org.robolectric.lib1.R.attr.offsetY = 0x7f010071;
    org.robolectric.lib2.R.attr.offsetY = 0x7f010071;
    org.robolectric.lib3.R.attr.offsetY = 0x7f010071;
  }

  private List<Integer> asIntList(int[] ints) {
    ArrayList<Integer> list = new ArrayList<Integer>();
    for (int anInt : ints) {
      list.add(anInt);
    }
    return list;
  }
}
