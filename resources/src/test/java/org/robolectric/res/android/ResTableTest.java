package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.resources.R;

@RunWith(JUnit4.class)
public class ResTableTest {

  private ByteBuffer order;
  private ResTable resTable;

  @Before
  public void setup() throws IOException {
    URL resource = getClass().getResource("/binaryresources/resources.ap_");
    ZipFile zipFile = new ZipFile(resource.getFile());

    ZipEntry arscEntry = zipFile.getEntry("resources.arsc");
    InputStream inputStream = zipFile.getInputStream(arscEntry);
    resTable = new ResTable();
    resTable.add(inputStream, mResources.getTableCount());
  }

  @Test
  public void testGetEntry_intType() {
    Ref<ResValue> outValue = new Ref<>(null);
    assertThat(resTable.getResource(R.integer.flock_size, outValue, true, 0, new Ref<Integer>(null), null)).isGreaterThan(-1);

    assertThat(outValue.get().dataType).isEqualTo(DataType.INT_DEC.code());
    assertThat(outValue.get().data).isEqualTo(1234);
  }

  @Test
  public void testGetEntry_intType_large() {
    Ref<ResValue> outValue = new Ref<>(null);
    resTable.mParams = newConfig("large");
    assertThat(resTable.getResource(R.integer.flock_size, outValue, true, 0, new Ref<Integer>(null), null)).isGreaterThan(-1);

    assertThat(outValue.get().dataType).isEqualTo(DataType.INT_DEC.code());
    assertThat(outValue.get().data).isEqualTo(1000000);
  }

  @Test
  public void testGetEntry_stringType() throws Exception {
    Ref<ResValue> outValue = new Ref<>(null);

    assertThat(resTable.getResource(R.string.first_string, outValue, true, 0, new Ref<Integer>(null), null)).isGreaterThan(-1);

    assertThat(outValue.get().dataType).isEqualTo(
        DataType.STRING.code());
    //assertThat(entry.entry.value.data).isEqualTo("sheep");
  }

  @Test
  public void testGetResource_boolType() throws Exception {
    Ref<ResValue> outValue = new Ref<>(null);
    assertThat(resTable.getResource(R.bool.is_verizon, outValue, true, 0, new Ref<Integer>(null), null)).isGreaterThan(-1);

    assertThat(outValue.get().dataType).isEqualTo(DataType.INT_BOOLEAN.code());
    assertThat(outValue.get().data).isEqualTo(0);
  }


  private static ResTableConfig newConfig(String qualifiers) {
    ResTableConfig config = new ResTableConfig();
    if (qualifiers != null) {
      new ConfigDescription().parse(qualifiers, config);
    }
    return config;
  }

//  @Test
//  public void getPackageName() {
//    assertThat(resTable.getPackageName(R.string.first_string)).isEqualTo("org.robolectric.resources");
//    assertThat(resTable.getPackageName(R.string.second_string)).isEqualTo("org.robolectric.resources");
//  }
//
//  @Test
//  public void shouldResolveResIdToType() throws Exception {
//    assertThat(resTable.getTypeName(R.string.first_string)).isEqualTo("string");
//
// assertThat(resTable.getTypeName(R.string.second_string)).isEqualTo("string");
//  }
}
