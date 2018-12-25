package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class DrawableResourceLoaderNoRunnerTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private PackageResourceTable resourceTable;

  @Before
  public void setUp() {
    resourceTable = new ResourceTableFactory().newResourceTable("org.robolectric");
  }

  @Test
  public void shouldFindDrawableResources() throws Exception {
    Path testBaseDir = temporaryFolder.newFolder("res").toPath();
    temporaryFolder.newFolder("res", "drawable");
    temporaryFolder.newFile("res/drawable/foo.png");
    ResourcePath resourcePath = new ResourcePath(null, testBaseDir, null);

    DrawableResourceLoader testLoader = new DrawableResourceLoader(resourceTable);
    testLoader.findDrawableResources(resourcePath);

    assertThat(resourceTable.getValue(new ResName("org.robolectric", "drawable", "foo"), new ResTable_config()).isFile()).isTrue();
  }
}
