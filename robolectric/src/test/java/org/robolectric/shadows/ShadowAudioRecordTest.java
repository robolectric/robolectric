package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.Math.min;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAudioRecord.AudioRecordSource;
import org.robolectric.shadows.ShadowAudioRecord.AudioRecordSourceProvider;

/** Tests for {@link ShadowAudioRecord}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = LOLLIPOP)
public class ShadowAudioRecordTest {

  @Test
  public void startReturnsSuccess() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.getRecordingState()).isEqualTo(AudioRecord.RECORDSTATE_RECORDING);
  }

  @Test
  public void setSourceProvider() {
    byte[] firstAudioRecordInput = new byte[] {1, 2, 3};
    AudioRecordSource firstAudioRecordSource = createAudioRecordSource(firstAudioRecordInput);
    AudioRecord firstAudioRecord = createAudioRecord();
    byte[] secondAudioRecordInput = new byte[] {4, 5, 6, 7, 8};
    AudioRecordSource subsequentAudioRecordSource = createAudioRecordSource(secondAudioRecordInput);
    AudioRecord secondAudioRecord = createAudioRecord();
    ShadowAudioRecord.setSourceProvider(
        new AudioRecordSourceProvider() {
          @Override
          public AudioRecordSource get(AudioRecord audioRecord) {
            if (audioRecord == firstAudioRecord) {
              return firstAudioRecordSource;
            }
            return subsequentAudioRecordSource;
          }
        });

    firstAudioRecord.startRecording();
    byte[] firstAudioRecordData = new byte[100];
    int firstAudioRecordBytesRead = firstAudioRecord.read(firstAudioRecordData, 0, 100);
    firstAudioRecord.stop();
    firstAudioRecord.release();
    // Read from second AudioRecord.
    secondAudioRecord.startRecording();
    byte[] secondAudioRecordData = new byte[100];
    int secondAudioRecordBytesRead = secondAudioRecord.read(secondAudioRecordData, 0, 100);
    secondAudioRecord.stop();
    secondAudioRecord.release();

    assertThat(firstAudioRecordBytesRead).isEqualTo(firstAudioRecordInput.length);
    assertThat(Arrays.copyOf(firstAudioRecordData, firstAudioRecordInput.length))
        .isEqualTo(firstAudioRecordInput);
    assertThat(secondAudioRecordBytesRead).isEqualTo(secondAudioRecordInput.length);
    assertThat(Arrays.copyOf(secondAudioRecordData, secondAudioRecordInput.length))
        .isEqualTo(secondAudioRecordInput);
  }

  @Test
  public void setSourceProvider_readBytesSlowly() {
    byte[] audioRecordInput = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    AudioRecordSource audioRecordSource = createAudioRecordSource(audioRecordInput);
    ShadowAudioRecord.setSourceProvider(audioRecord -> audioRecordSource);

    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();
    byte[] audioRecordData = new byte[100];
    int audioRecordBytesFirstRead = audioRecord.read(audioRecordData, 0, 3);
    int audioRecordBytesSecondRead = audioRecord.read(audioRecordData, 3, 3);
    int audioRecordBytesThirdRead = audioRecord.read(audioRecordData, 6, 94);
    audioRecord.stop();
    audioRecord.release();

    assertThat(audioRecordBytesFirstRead).isEqualTo(3);
    assertThat(audioRecordBytesSecondRead).isEqualTo(3);
    assertThat(audioRecordBytesThirdRead).isEqualTo(2);
    assertThat(Arrays.copyOf(audioRecordData, audioRecordInput.length)).isEqualTo(audioRecordInput);
  }

  @Test
  public void setSource_instanceCreatedBeforeSetSourceIsCalled() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();
    byte[] audioRecordInput = new byte[] {1, 2, 3, 4, 5, 6, 7, 8};
    ShadowAudioRecord.setSource(createAudioRecordSource(audioRecordInput));

    byte[] audioRecordData = new byte[100];
    int audioRecordBytesRead = audioRecord.read(audioRecordData, 0, 100);
    audioRecord.stop();
    audioRecord.release();

    assertThat(audioRecordBytesRead).isEqualTo(audioRecordInput.length);
    assertThat(Arrays.copyOf(audioRecordData, audioRecordInput.length)).isEqualTo(audioRecordInput);
  }

  @Test
  public void nativeReadByteFillsAudioDataByDefault() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(new byte[100], 0, 100)).isEqualTo(100);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteFillsAudioDataByDefaultMOnwards() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(new byte[100], 0, 100, AudioRecord.READ_BLOCKING)).isEqualTo(100);
  }

  @Test
  public void nativeReadByteCallsAudioRecordSourceWhenSet() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new byte[100], 0, 100);

    verify(source).readInByteArray(any(byte[].class), eq(0), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteCallsAudioRecordSourceWhenSetBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new byte[100], 0, 100, AudioRecord.READ_BLOCKING);

    verify(source).readInByteArray(any(byte[].class), eq(0), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteCallsAudioRecordSourceWhenSetNonBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new byte[100], 0, 100, AudioRecord.READ_NON_BLOCKING);

    verify(source).readInByteArray(any(byte[].class), eq(0), eq(100), /* isBlocking=*/ eq(false));
    verifyNoMoreInteractions(source);
  }

  @Test
  public void nativeReadShortFillsAudioDataByDefault() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(new short[100], 0, 100)).isEqualTo(100);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadShortFillsAudioDataByDefaultMOnwards() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(new short[100], 0, 100, AudioRecord.READ_BLOCKING)).isEqualTo(100);
  }

  @Test
  public void nativeReadShortCallsAudioRecordSourceWhenSet() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new short[100], 0, 100);

    verify(source).readInShortArray(any(short[].class), eq(0), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadShortCallsAudioRecordSourceWhenSetBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new short[100], 0, 100, AudioRecord.READ_BLOCKING);

    verify(source).readInShortArray(any(short[].class), eq(0), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadShortCallsAudioRecordSourceWhenSetNonBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(new short[100], 0, 100, AudioRecord.READ_NON_BLOCKING);

    verify(source).readInShortArray(any(short[].class), eq(0), eq(100), /* isBlocking=*/ eq(false));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadFloatFillsAudioDataByDefaultMOnwards() {
    AudioRecord audioRecord =
        new AudioRecord(
            AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            1024);
    audioRecord.startRecording();

    assertThat(audioRecord.read(new float[100], 0, 100, AudioRecord.READ_BLOCKING)).isEqualTo(100);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadFloatCallsAudioRecordSourceWhenSetBlocking() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord =
        new AudioRecord(
            AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            1024);
    audioRecord.startRecording();

    audioRecord.read(new float[100], 0, 100, AudioRecord.READ_BLOCKING);

    verify(source).readInFloatArray(any(float[].class), eq(0), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadFloatCallsAudioRecordSourceWhenSetNonBlocking() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord =
        new AudioRecord(
            AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            1024);
    audioRecord.startRecording();

    audioRecord.read(new float[100], 0, 100, AudioRecord.READ_NON_BLOCKING);

    verify(source).readInFloatArray(any(float[].class), eq(0), eq(100), /* isBlocking=*/ eq(false));
    verifyNoMoreInteractions(source);
  }

  @Test
  public void nativeReadByteBufferFillsAudioDataByDefault() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(ByteBuffer.allocate(100), 100)).isEqualTo(100);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteBufferFillsAudioDataByDefaultMOnwards() {
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    assertThat(audioRecord.read(ByteBuffer.allocate(100), 100, AudioRecord.READ_BLOCKING))
        .isEqualTo(100);
  }

  @Test
  public void nativeReadByteBufferCallsAudioRecordSourceWhenSet() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(ByteBuffer.allocate(100), 100);

    verify(source).readInDirectBuffer(any(ByteBuffer.class), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteBufferCallsAudioRecordSourceWhenSetBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(ByteBuffer.allocate(100), 100, AudioRecord.READ_BLOCKING);

    verify(source).readInDirectBuffer(any(ByteBuffer.class), eq(100), /* isBlocking=*/ eq(true));
    verifyNoMoreInteractions(source);
  }

  @Test
  @Config(minSdk = M)
  public void nativeReadByteBufferCallsAudioRecordSourceWhenSetNonBlockingMOnwards() {
    AudioRecordSource source = Mockito.mock(AudioRecordSource.class);
    ShadowAudioRecord.setSource(source);
    AudioRecord audioRecord = createAudioRecord();
    audioRecord.startRecording();

    audioRecord.read(ByteBuffer.allocate(100), 100, AudioRecord.READ_NON_BLOCKING);

    verify(source).readInDirectBuffer(any(ByteBuffer.class), eq(100), /* isBlocking=*/ eq(false));
    verifyNoMoreInteractions(source);
  }

  private static AudioRecord createAudioRecord() {
    return new AudioRecord(
        AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 1024);
  }

  private static AudioRecordSource createAudioRecordSource(byte[] bytes) {
    return new AudioRecordSource() {
      int bytesRead = 0;

      @Override
      public int readInByteArray(
          byte[] audioData, int offsetInBytes, int sizeInBytes, boolean isBlocking) {
        int availableBytesToBeRead = min(bytes.length - bytesRead, sizeInBytes);
        if (availableBytesToBeRead <= 0) {
          return -1;
        }
        System.arraycopy(bytes, bytesRead, audioData, offsetInBytes, availableBytesToBeRead);
        bytesRead += availableBytesToBeRead;
        return availableBytesToBeRead;
      }
    };
  }
}
