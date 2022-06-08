package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.TempDirectory;

/** Tests for {@link ShadowMediaMuxer}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowMediaMuxerTest {
  private static final int INPUT_SIZE = 512;
  private TempDirectory tempDirectory;

  @Before
  public void setUp() {
    tempDirectory = new TempDirectory();
  }

  @After
  public void tearDown() {
    tempDirectory.destroy();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void basicMuxingFlow_sameZeroOffset() throws IOException {
    basicMuxingFlow(0, 0, INPUT_SIZE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void basicMuxingFlow_sameNonZeroOffset() throws IOException {
    basicMuxingFlow(10, 10, INPUT_SIZE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void basicMuxingFlow_nonSameButSmallerOffset() throws IOException {
    basicMuxingFlow(0, 10, INPUT_SIZE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void basicMuxingFlow_nonSameButLargerOffset() throws IOException {
    basicMuxingFlow(10, 0, INPUT_SIZE);
  }

  private void basicMuxingFlow(int bufInfoOffset, int bufOffset, int inputSize) throws IOException {
    String tempFilePath =
        tempDirectory.create("dir").resolve(UUID.randomUUID().toString()).toString();
    MediaMuxer muxer = new MediaMuxer(tempFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

    MediaFormat format = new MediaFormat();
    format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_AUDIO_AAC);

    int trackIndex = muxer.addTrack(format);
    muxer.start();

    byte[] inputBytes = new byte[inputSize];
    new Random().nextBytes(inputBytes);
    ByteBuffer inputBuffer = ByteBuffer.wrap(inputBytes);
    inputBuffer.position(bufOffset);
    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    int outputSize = inputSize - bufInfoOffset;
    bufferInfo.set(bufInfoOffset, outputSize, 0, 0);

    muxer.writeSampleData(trackIndex, inputBuffer, bufferInfo);
    muxer.stop();

    // Read in what was muxed.
    byte[] outputBytes = new byte[outputSize];
    FileInputStream tempFile = new FileInputStream(tempFilePath);

    int offset = 0;
    int bytesRead = 0;
    while (outputSize - offset > 0
        && (bytesRead = tempFile.read(outputBytes, offset, outputSize - offset)) != -1) {
      offset += bytesRead;
    }

    assertThat(outputBytes)
        .isEqualTo(Arrays.copyOfRange(inputBytes, bufInfoOffset, inputBytes.length));
    new File(tempFilePath).deleteOnExit();
    muxer.release();
  }
}
