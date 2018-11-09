package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.TestUtil.testResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class ResourceParserTest {

  private ResourceTable resourceTable;
  private ResTable_config config;

  @Before
  public void setUp() {
    ResourceTableFactory resourceTableFactory = new ResourceTableFactory();
    resourceTable = resourceTableFactory.newResourceTable("org.robolectric", testResources());
    config = new ResTable_config();
  }


  @Test
  public void shouldLoadDrawableXmlResources() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "rainbow"), config);
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isFile()).isTrue();
    assertThat((String) value.getData()).contains("rainbow.xml");
  }

  @Test
  public void shouldLoadDrawableBitmapResources() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "an_image"), config);
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isFile()).isTrue();
    assertThat((String) value.getData()).contains("an_image.png");
  }

  @Test
  public void shouldLoadDrawableBitmapResourcesDefinedByItemTag() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "example_item_drawable"), config);
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isReference()).isTrue();
    assertThat((String) value.getData()).isEqualTo("@drawable/an_image");
  }

  @Test
  public void shouldLoadIdResourcesDefinedByItemTag() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "id", "id_declared_in_item_tag"), config);
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.CHAR_SEQUENCE);
    assertThat(value.isReference()).isFalse();
    assertThat(value.asString()).isEqualTo("");
    assertThat((String) value.getData()).isEqualTo("");
  }
}
