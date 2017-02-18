package org.robolectric.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.ResourcePath;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BuckManifestFactoryTest {

  private Config.Builder configBuilder;
  private BuckManifestFactory buckManifestFactory;

  @Before
  public void setUp() throws Exception {
    configBuilder = Config.Builder.defaults().setPackageName("com.robolectric.buck");
    System.setProperty("buck.robolectric_manifest", "buck/AndroidManifest.xml");
    System.setProperty("buck.robolectric_res_directories", "buck/res1:buck/res2");
    System.setProperty("buck.robolectric_assets_directories", "buck/assets");
    buckManifestFactory = new BuckManifestFactory();
  }

  @After
  public void tearDown() {
    System.clearProperty("buck.robolectric_manifest");
    System.clearProperty("buck.robolectric_res_directories");
    System.clearProperty("buck.robolectric_assets_directories");
  }

  @Test public void identify() throws Exception {
    ManifestIdentifier manifestIdentifier = buckManifestFactory.identify(configBuilder.build());
    assertThat(manifestIdentifier.getManifestFile())
        .isEqualTo(FileFsFile.from("buck/AndroidManifest.xml"));
    assertThat(manifestIdentifier.getPackageName())
        .isEqualTo("com.robolectric.buck");
  }

  @Test public void multiple_res_dirs() throws Exception {
    ManifestIdentifier manifestIdentifier = buckManifestFactory.identify(configBuilder.build());
    AndroidManifest manifest = buckManifestFactory.create(manifestIdentifier);
    assertThat(manifest.getResDirectory())
            .isEqualTo(FileFsFile.from("buck/res2"));
    assertThat(manifest.getAssetsDirectory())
            .isEqualTo(FileFsFile.from("buck/assets"));

    List<ResourcePath> resourcePathList = manifest.getIncludedResourcePaths();
    assertThat(resourcePathList.size()).isEqualTo(2);
    assertThat(resourcePathList).containsExactly(
            new ResourcePath(manifest.getRClass(), FileFsFile.from("buck/res2"), FileFsFile.from("buck/assets")),
            new ResourcePath(manifest.getRClass(), FileFsFile.from("buck/res1"), FileFsFile.from("buck/assets"))
    );
  }
}
