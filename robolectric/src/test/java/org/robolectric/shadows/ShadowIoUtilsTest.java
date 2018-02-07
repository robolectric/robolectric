package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import libcore.io.IoUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowIoUtilsTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void ioUtils() throws Exception {

    File file = temporaryFolder.newFile("test_file.txt");
    Files.write("some contents", file, StandardCharsets.UTF_8);

    String contents = IoUtils.readFileAsString(file.getAbsolutePath());
    assertThat(contents).isEqualTo("some contents");
  }
}
