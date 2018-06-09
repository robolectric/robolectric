package org.robolectric.res;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.robolectric.util.TestUtil.sdkResources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class StyleResourceLoaderTest {
  private PackageResourceTable resourceTable;

  @Before
  public void setUp() throws Exception {
    assumeTrue(RuntimeEnvironment.useLegacyResources());
    ResourcePath resourcePath = sdkResources(JELLY_BEAN);
    resourceTable = new ResourceTableFactory().newResourceTable("android", resourcePath);
  }

  @Test
  public void testStyleDataIsLoadedCorrectly() throws Exception {
    TypedResource typedResource = resourceTable.getValue(new ResName("android", "style", "Theme_Holo"), new ResTable_config());
    StyleData styleData = (StyleData) typedResource.getData();
    assertThat(styleData.getName()).isEqualTo("Theme_Holo");
    assertThat(styleData.getParent()).isEqualTo("Theme");
    assertThat(styleData.getPackageName()).isEqualTo("android");
    assertThat(styleData.getAttrValue(new ResName("android", "attr", "colorForeground")).value)
        .isEqualTo("@android:color/bright_foreground_holo_dark");
  }
}
