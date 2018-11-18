package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

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

  @SuppressWarnings("TruthConstantAsserts")
  @Test
  public void testRemap() {
    ResourceRemapper remapper = new ResourceRemapper(ApplicationRClass.class);
    remapper.remapRClass(SecondClass.class);
    remapper.remapRClass(ThirdClass.class);

    // Resource identifiers that are common across libraries should be remapped to the same value.
    assertThat(ApplicationRClass.string.string_one).isEqualTo(SecondClass.string.string_one);
    assertThat(ApplicationRClass.string.string_one).isEqualTo(ThirdClass.string.string_one);

    // Resource identifiers that clash across two libraries should be remapped to different values.
    assertThat(SecondClass.id.id_clash)
        .isNotEqualTo(ThirdClass.id.another_id_clash);

    // Styleable arrays of values should be updated to match the remapped values.
    assertThat(ThirdClass.styleable.SomeStyleable).isEqualTo(ApplicationRClass.styleable.SomeStyleable);
    assertThat(SecondClass.styleable.SomeStyleable).isEqualTo(ApplicationRClass.styleable.SomeStyleable);
    assertThat(ApplicationRClass.styleable.SomeStyleable).asList().containsExactly(ApplicationRClass.attr.attr_one, ApplicationRClass.attr.attr_two);
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

    public static final class attr {
      public static int attr_one = 0x7f010008;
      public static int attr_two = 0x7f010009;
    }

    public static final class styleable {
      public static final int[] SomeStyleable = new int[]{ApplicationRClass.attr.attr_one, ApplicationRClass.attr.attr_two};
      public static final int SomeStyleable_offsetX = 0;
      public static final int SomeStyleable_offsetY = 1;
    }
  }

  public static final class SecondClass {
    public static final class id {
      public static int id_clash = 0x7f010001;
    }

    public static final class integer {
      public static int integer_one = 0x7f010001;
      public static int integer_two = 0x7f010002;
    }

    public static final class string {
      public static int string_one = 0x7f020001;
      public static int string_three = 0x7f020002;
    }

    public static final class attr {
      public static int attr_one = 0x7f010001;
      public static int attr_two = 0x7f010002;
    }

    public static final class styleable {
      public static final int[] SomeStyleable = new int[]{SecondClass.attr.attr_one, SecondClass.attr.attr_two};
      public static final int SomeStyleable_offsetX = 0;
      public static final int SomeStyleable_offsetY = 1;
    }
  }

  public static final class ThirdClass {
    public static final class id {
      public static int another_id_clash = 0x7f010001;
    }

    public static final class raw {
      public static int raw_one = 0x7f010001;
      public static int raw_two = 0x7f010002;
    }

    public static final class string {
      public static int string_one = 0x7f020009;
    }

    public static final class attr {
      public static int attr_one = 0x7f010003;
      public static int attr_two = 0x7f010004;
    }

    public static final class styleable {
      public static final int[] SomeStyleable = new int[]{ThirdClass.attr.attr_one, ThirdClass.attr.attr_two};
      public static final int SomeStyleable_offsetX = 0;
      public static final int SomeStyleable_offsetY = 1;
    }
  }

}
