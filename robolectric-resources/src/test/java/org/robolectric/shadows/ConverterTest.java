package org.robolectric.shadows;

import org.junit.Test;
import org.robolectric.res.ResType;
import org.robolectric.res.TypedResource;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {

  @Test
  public void parseInt_fromCharSequence_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" 100 ", ResType.CHAR_SEQUENCE);
    assertThat(Converter.getConverter(ResType.CHAR_SEQUENCE).asInt(resource)).isEqualTo(100);
  }
}