package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.StandardCharsets;
import libcore.io.IoUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowIoUtilsTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void ioUtils() throws Exception {

    File file = temporaryFolder.newFile("test_file.txt");
    Files.asCharSink(file, StandardCharsets.UTF_8).write("some contents");

    String contents = IoUtils.readFileAsString(file.getAbsolutePath());
    assertThat(contents).isEqualTo("some contents");
  }
}
