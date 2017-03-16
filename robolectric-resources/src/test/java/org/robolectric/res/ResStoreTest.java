package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class ResStoreTest {

  private Path resCachePath;
  private InMemoryPackageResourceTable origResourceTable;
  private ResStore resStore;

  @Before
  public void setUp() throws Exception {
    origResourceTable = new InMemoryPackageResourceTable("package");
//    resCachePath = TempDirectory.create().resolve("tmp.res");
    resCachePath = FileSystems.getDefault().getPath("/tmp").resolve("robolectric.res");
    Files.createDirectories(resCachePath.getParent());
    resStore = new ResStore();
  }

  @Test
  public void serialization() throws Exception {
    origResourceTable.addResource("strings", "a_string", new TypedResource<>("value", ResType.CHAR_SEQUENCE,
        new XmlContext("package", Fs.fileFromPath("/path/to/wherever-en.xml"))));
    resStore.save(origResourceTable, resCachePath);
    assertThat(resCachePath).exists();

    ResourceTable loadedResourceTable = resStore.load(resCachePath);
    assertThat(loadedResourceTable.getValue(new ResName("package:strings/a_string"), "").getData())
        .isEqualTo("value");
  }
}