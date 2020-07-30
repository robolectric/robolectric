package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAudioRecord.AudioRecordSource;

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
}
