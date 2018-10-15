package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.Qualifiers;
import org.robolectric.res.ResType;
import org.robolectric.res.TypedResource;
import org.robolectric.res.XmlContext;

@RunWith(AndroidJUnit4.class)
public class ConverterTest {

  private XmlContext xmlContext;

  @Before
  public void setUp() throws Exception {
    FsFile xmlFile = Fs.newFile(new File("res/values/foo.xml"));
    Qualifiers qualifiers = Qualifiers.fromParentDir(xmlFile.getParent());

    xmlContext = new XmlContext("", xmlFile, qualifiers);
  }

  @Test
  public void fromCharSequence_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" 100 ", ResType.CHAR_SEQUENCE, xmlContext);
    assertThat(Converter.getConverter(ResType.CHAR_SEQUENCE).asInt(resource)).isEqualTo(100);
  }

  @Test
  public void fromCharSequence_asCharSequence_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" Robolectric ", ResType.CHAR_SEQUENCE, xmlContext);
    assertThat(Converter.getConverter(ResType.CHAR_SEQUENCE).asCharSequence(resource)).isEqualTo("Robolectric");
  }

  @Test
  public void fromColor_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" #aaaaaa ", ResType.COLOR, xmlContext);
    assertThat(Converter.getConverter(ResType.COLOR).asInt(resource)).isEqualTo(-5592406);
  }

  @Test
  public void fromDrawableValue_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" #aaaaaa ", ResType.DRAWABLE, xmlContext);
    assertThat(Converter.getConverter(ResType.DRAWABLE).asInt(resource)).isEqualTo(-5592406);
  }

  @Test
  public void fromInt_asInt_shouldHandleSpacesInString() {
    final TypedResource<String> resource = new TypedResource<>(" 100 ", ResType.INTEGER, xmlContext);
    assertThat(Converter.getConverter(ResType.INTEGER).asInt(resource)).isEqualTo(100);
  }
}