package org.robolectric.shadows;

import android.media.AudioManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAudioManager {
  public static final int MAX_VOLUME_MUSIC_DTMF = 15;
  public static final int DEFAULT_MAX_VOLUME = 7;
  public static final int DEFAULT_VOLUME = 7;
  public static final int INVALID_VOLUME = 0;
  public static final int FLAG_NO_ACTION = 0;
  public static final int[] ALL_STREAMS = {
      AudioManager.STREAM_MUSIC,
      AudioManager.STREAM_ALARM,
      AudioManager.STREAM_NOTIFICATION,
      AudioManager.STREAM_RING,
      AudioManager.STREAM_SYSTEM,
      AudioManager.STREAM_VOICE_CALL,
      AudioManager.STREAM_DTMF
  };

  private AudioFocusRequest lastAudioFocusRequest;
  private int nextResponseValue = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  private AudioManager.OnAudioFocusChangeListener lastAbandonedAudioFocusListener;
  private HashMap<Integer, AudioStream> streamStatus = new HashMap<Integer, AudioStream>();

  public ShadowAudioManager() {
    for (int stream : ALL_STREAMS) {
      streamStatus.put(stream, new AudioStream(DEFAULT_VOLUME, DEFAULT_MAX_VOLUME, FLAG_NO_ACTION));
    }
    streamStatus.get(AudioManager.STREAM_MUSIC).setMaxVolume(MAX_VOLUME_MUSIC_DTMF);
    streamStatus.get(AudioManager.STREAM_DTMF).setMaxVolume(MAX_VOLUME_MUSIC_DTMF);
  }

  @Implementation
  public int getStreamMaxVolume(int streamType) {
    AudioStream stream = streamStatus.get(streamType);
    return (stream != null) ? stream.getMaxVolume() : INVALID_VOLUME;
  }

  @Implementation
  public int getStreamVolume(int streamType) {
    AudioStream stream = streamStatus.get(streamType);
    return (stream != null) ? stream.getCurrentVolume() : INVALID_VOLUME;
  }

  @Implementation
  public void setStreamVolume(int streamType, int index, int flags) {
    AudioStream stream = streamStatus.get(streamType);
    if (stream != null) {
      stream.setCurrentVolume(index);
      stream.setFlag(flags);
    }
  }

  @Implementation
  public int requestAudioFocus(android.media.AudioManager.OnAudioFocusChangeListener l, int streamType, int durationHint) {
    lastAudioFocusRequest = new AudioFocusRequest(l, streamType, durationHint);
    return nextResponseValue;
  }

  @Implementation
  public int abandonAudioFocus(AudioManager.OnAudioFocusChangeListener l) {
    lastAbandonedAudioFocusListener = l;
    return nextResponseValue;
  }

  public void setStreamMaxVolume(int streamMaxVolume) {
    for (Map.Entry<Integer, AudioStream> entry : streamStatus.entrySet()) {
      entry.getValue().setMaxVolume(streamMaxVolume);
    }
  }

  public void setStreamVolume(int streamVolume) {
    for (Map.Entry<Integer, AudioStream> entry : streamStatus.entrySet()) {
      entry.getValue().setCurrentVolume(streamVolume);
    }
  }

  public AudioFocusRequest getLastAudioFocusRequest() {
    return lastAudioFocusRequest;
  }

  public void setNextFocusRequestResponse(int nextResponseValue) {
    this.nextResponseValue = nextResponseValue;
  }

  public AudioManager.OnAudioFocusChangeListener getLastAbandonedAudioFocusListener() {
    return lastAbandonedAudioFocusListener;
  }

  public static class AudioFocusRequest {
    public final AudioManager.OnAudioFocusChangeListener listener;
    public final int streamType;
    public final int durationHint;

    private AudioFocusRequest(AudioManager.OnAudioFocusChangeListener listener, int streamType, int durationHint) {
      this.listener = listener;
      this.streamType = streamType;
      this.durationHint = durationHint;
    }
  }

  private static class AudioStream {
    private int currentVolume;
    private int maxVolume;
    private int flag;

    public AudioStream(int currVol, int maxVol, int flag) {
      setCurrentVolume(currVol);
      setMaxVolume(maxVol);
      setFlag(flag);
    }

    public int getCurrentVolume() {
      return currentVolume;
    }

    public int getMaxVolume() {
      return maxVolume;
    }

    public int getFlag() {
      return flag;
    }

    public void setCurrentVolume(int vol) {
      if (vol > maxVolume) {
        vol = maxVolume;
      } else if (vol < 0) {
        vol = 0;
      }
      currentVolume = vol;
    }

    public void setMaxVolume(int vol) {
      maxVolume = vol;
    }

    public void setFlag(int flag) {
      this.flag = flag;
    }
  }
}
