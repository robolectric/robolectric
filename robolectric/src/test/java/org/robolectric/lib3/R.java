package org.robolectric.lib3;

public class R {
  public static final class id {
    public static int lib_button = 0x7f010001;
    public static int lib3_button = 0x7f010002;
  }

  public static final class string {
    public static int only_in_lib3 = 0x7f020001;
    public static int in_all_libs = 0x7f020002;
    public static int also_in_all_libs = 0x7f020003;
  }

  public static final class styleable {
    public static final int[] SomeStyleable = new int[]{attr.offsetX, attr.offsetY};
    public static final int SomeStyleable_offsetX = 0;
    public static final int SomeStyleable_offsetY = 1;
  }

  public static final class attr {
    public static int offsetX = 0x7f030001;
    public static int offsetY = 0x7f030002;
  }
}
