package org.robolectric.shadows;

import org.junit.Test;
import org.robolectric.res.ResType;
import org.robolectric.res.TypedResource;

import static org.assertj.core.api.Assertions.assertThat;

public class ConverterTest {

  @Test
  public void fromCharSequence_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" 100 ", ResType.CHAR_SEQUENCE);
    assertThat(Converter.getConverter(ResType.CHAR_SEQUENCE).asInt(resource)).isEqualTo(100);
  }

  @Test
  public void fromCharSequence_asCharSequence_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" Robolectric ", ResType.CHAR_SEQUENCE);
    assertThat(Converter.getConverter(ResType.CHAR_SEQUENCE).asCharSequence(resource)).isEqualTo("Robolectric");
  }

  @Test
  public void fromColor_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" #aaaaaa ", ResType.COLOR);
    assertThat(Converter.getConverter(ResType.COLOR).asInt(resource)).isEqualTo(-5592406);
  }

  @Test
  public void fromDrawableValue_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" #aaaaaa ", ResType.DRAWABLE);
    assertThat(Converter.getConverter(ResType.DRAWABLE).asInt(resource)).isEqualTo(-5592406);
  }

  @Test
  public void fromInt_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" 100 ", ResType.INTEGER);
    assertThat(Converter.getConverter(ResType.INTEGER).asInt(resource)).isEqualTo(100);
  }
}