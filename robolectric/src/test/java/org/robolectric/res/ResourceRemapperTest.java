package org.robolectric.res;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResourceRemapperTest {

  @Test(expected = IllegalArgumentException.class)
  public void forbidFinalRClasses() {
    ResourceRemapper remapper = new ResourceRemapper(null);
    remapper.remapRClass(FinalRClass.class);
  }

  @Test
  public void testRemap() {
    ResourceRemapper remapper = new ResourceRemapper(org.robolectric.R.class);
    remapper.remapRClass(org.robolectric.lib1.R.class);
    remapper.remapRClass(org.robolectric.lib2.R.class);
    remapper.remapRClass(org.robolectric.lib3.R.class);

    // Resource identifiers that are common across libraries should be remapped to the same value.
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib1.R.string.in_all_libs);
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib2.R.string.in_all_libs);
    assertThat(org.robolectric.R.string.in_all_libs).isEqualTo(org.robolectric.lib3.R.string.in_all_libs);

    // Resource identifiers that clash across two libraries should be remapped to different values.
    assertThat(org.robolectric.lib1.R.id.lib1_button)
        .isNotEqualTo(org.robolectric.lib2.R.id.lib2_button);

    // Styleable arrays of values should be updated to match the remapped values.
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib1.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib2.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.lib3.R.styleable.SomeStyleable);
    assertThat(org.robolectric.R.styleable.SomeStyleable).containsExactly(org.robolectric.R.attr.offsetX, org.robolectric.R.attr.offsetY);
  }

  @Test
  public void resourcesOfDifferentTypes_shouldHaveDifferentTypeSpaces() {
    ResourceRemapper remapper = new ResourceRemapper(ApplicationRClass.class);
    remapper.remapRClass(SecondClass.class);
    remapper.remapRClass(ThirdClass.class);

    Set<Integer> allIds = new HashSet<>();
    assertThat(allIds.add(ApplicationRClass.string.string_one)).isTrue();
    assertThat(allIds.add(ApplicationRClass.string.string_two)).isTrue();
    assertThat(allIds.add(SecondClass.integer.integer_one)).isTrue();
    assertThat(allIds.add(SecondClass.integer.integer_two)).isTrue();
    assertThat(allIds.add(SecondClass.string.string_one)).isFalse();
    assertThat(allIds.add(SecondClass.string.string_three)).isTrue();
    assertThat(allIds.add(ThirdClass.raw.raw_one)).isTrue();
    assertThat(allIds.add(ThirdClass.raw.raw_two)).isTrue();

    assertThat(ResourceIds.getTypeIdentifier(ApplicationRClass.string.string_one)).isEqualTo(ResourceIds.getTypeIdentifier(ApplicationRClass.string.string_two));
    assertThat(ResourceIds.getTypeIdentifier(ApplicationRClass.string.string_one)).isEqualTo(ResourceIds.getTypeIdentifier(SecondClass.string.string_three));

    assertThat(ResourceIds.getTypeIdentifier(ApplicationRClass.string.string_two)).isNotEqualTo(ResourceIds.getTypeIdentifier(SecondClass.integer.integer_two));
    assertThat(ResourceIds.getTypeIdentifier(ThirdClass.raw.raw_two)).isNotEqualTo(ResourceIds.getTypeIdentifier(SecondClass.integer.integer_two));
  }

  public static final class FinalRClass {
    public static final class string {
      public static final int a_final_value = 0x7f020001;
      public static final int another_final_value = 0x7f020002;
    }
  }

  public static final class ApplicationRClass {
    public static final class string {
      public static final int string_one = 0x7f010001;
      public static final int string_two = 0x7f010002;
    }
  }

  public static final class SecondClass {
    public static final class integer {
      public static int integer_one = 0x7f010001;
      public static int integer_two = 0x7f010002;
    }

    public static final class string {
      public static int string_one = 0x7f020001;
      public static int string_three = 0x7f020002;
    }

  }

  public static final class ThirdClass {
    public static final class raw {
      public static int raw_one = 0x7f010001;
      public static int raw_two = 0x7f010002;
    }
  }

}
