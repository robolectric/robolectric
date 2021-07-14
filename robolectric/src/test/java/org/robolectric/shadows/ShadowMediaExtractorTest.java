package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.util.DataSource;

/** Tests for ShadowMediaExtractor */
@RunWith(AndroidJUnit4.class)
public class ShadowMediaExtractorTest {
  private final String path = "/media/foo.mp4";
  private final DataSource dataSource = DataSource.toDataSource(path);
  private final MediaFormat audioMediaFormat =
      MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_FLAC, 44100, 2);
  private final MediaFormat videoMediaFormat =
      MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_VP8, 320, 240);

  private byte[] generateSampleData(int length) {
    byte[] data = new byte[length];
    new Random().nextBytes(data);
    return data;
  }

  @Test
  public void getTrackFormat_returnsTrackFormat() throws IOException {

    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, new byte[0]);

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);

    assertThat(mediaExtractor.getTrackCount()).isEqualTo(1);
    assertThat(mediaExtractor.getTrackFormat(0)).isEqualTo(audioMediaFormat);
  }

  @Test
  public void getSampleTrackIndex_returnsSelectedTrack() throws IOException {
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, new byte[0]);
    ShadowMediaExtractor.addTrack(dataSource, videoMediaFormat, new byte[0]);

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);
    mediaExtractor.selectTrack(0);

    assertThat(mediaExtractor.getSampleTrackIndex()).isEqualTo(0);
  }

  @Test
  public void getSampleTrackIndex_returnsSecondSelectedTrack() throws IOException {
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, new byte[0]);
    ShadowMediaExtractor.addTrack(dataSource, videoMediaFormat, new byte[0]);

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);
    mediaExtractor.selectTrack(0);
    mediaExtractor.unselectTrack(0);
    mediaExtractor.selectTrack(1);

    assertThat(mediaExtractor.getSampleTrackIndex()).isEqualTo(1);
  }

  @Test
  public void selectTrack_onlyOneAtATime() throws IOException {
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, new byte[0]);
    ShadowMediaExtractor.addTrack(dataSource, videoMediaFormat, new byte[0]);

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);
    mediaExtractor.selectTrack(0);

    assertThrows(IllegalStateException.class, () -> mediaExtractor.selectTrack(1));
  }

  @Test
  public void selectTrack_outOfBounds() throws IOException {
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, new byte[0]);

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> mediaExtractor.selectTrack(1));
  }

  @Test
  public void readSampleData_returnsSampleData() throws IOException {
    byte[] sampleData = generateSampleData(4096);
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, sampleData);

    int byteBufferSize = 1024;
    ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferSize);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);
    mediaExtractor.selectTrack(0);

    while (true) {
      int length = mediaExtractor.readSampleData(byteBuffer, 0);
      if (length == -1) {
        break;
      }
      outputStream.write(byteBuffer.array());
      byteBuffer.rewind();
      mediaExtractor.advance();
      assertThat(length).isEqualTo(byteBufferSize);
    }

    // Check that the read data matches the injected data.
    assertThat(outputStream.toByteArray()).isEqualTo(sampleData);
  }

  @Test
  public void readSampleData_returnsSampleDataForTwoTracks() throws IOException {
    byte[] audioSampleData = generateSampleData(4096);
    ShadowMediaExtractor.addTrack(dataSource, audioMediaFormat, audioSampleData);
    byte[] videoSampleData = generateSampleData(8192);
    ShadowMediaExtractor.addTrack(dataSource, videoMediaFormat, videoSampleData);

    int byteBufferSize = 1024;
    ByteBuffer byteBuffer = ByteBuffer.allocate(byteBufferSize);
    ByteArrayOutputStream audioDataOutputStream = new ByteArrayOutputStream();
    ByteArrayOutputStream videoDataOutputStream = new ByteArrayOutputStream();

    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);
    mediaExtractor.selectTrack(0);

    // Read data from the audio track into audioDataOutputStream.
    while (true) {
      int length = mediaExtractor.readSampleData(byteBuffer, 0);
      if (length == -1) {
        break;
      }
      audioDataOutputStream.write(byteBuffer.array());
      byteBuffer.rewind();
      mediaExtractor.advance();
      assertThat(length).isEqualTo(byteBufferSize);
    }

    // Read data from the video track into videoDataOutputStream.
    mediaExtractor.unselectTrack(0);
    mediaExtractor.selectTrack(1);

    while (true) {
      int length = mediaExtractor.readSampleData(byteBuffer, 0);
      if (length == -1) {
        break;
      }
      videoDataOutputStream.write(byteBuffer.array());
      byteBuffer.rewind();
      mediaExtractor.advance();
      assertThat(length).isEqualTo(byteBufferSize);
    }

    // Check that the read data matches the injected data.
    assertThat(audioDataOutputStream.toByteArray()).isEqualTo(audioSampleData);
    assertThat(videoDataOutputStream.toByteArray()).isEqualTo(videoSampleData);
  }

  @Test
  public void setDataSource_emptyTracksWhenNotAdded() throws IOException {
    // Note: no data source data has been set with ShadowMediaExtractor.addTrack().
    MediaExtractor mediaExtractor = new MediaExtractor();
    mediaExtractor.setDataSource(path);

    assertThat(mediaExtractor.getTrackCount()).isEqualTo(0);
  }
}
