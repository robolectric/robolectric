package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResourceIdGeneratorTest {

  @Test
  public void shouldGenerateUniqueId() {
    ResourceIdGenerator generator = new ResourceIdGenerator(0x7F);
    generator.record(0x7F010001, "string", "some_name");
    generator.record(0x7F010002, "string", "another_name");

    assertThat(generator.generate("string", "next_name")).isEqualTo(0x7F010003);
  }

  @Test
  public void shouldIdForUnseenType() {
    ResourceIdGenerator generator = new ResourceIdGenerator(0x7F);
    generator.record(0x7F010001, "string", "some_name");
    generator.record(0x7F010002, "string", "another_name");

    assertThat(generator.generate("int", "int_name")).isEqualTo(0x7F020001);
  }
}
