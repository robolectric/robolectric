package org.robolectric.shadows;

import android.media.AudioManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import java.util.HashMap;
import java.util.Map;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(AudioManager.class)
public class ShadowAudioManager {

  /**the default max volume of the music and dtmf stream*/
  private static final int MAX_VOLUME_MUSIC_DTMF = 15;

  /**the default max volume of the rest of the stream*/
  private static final int DEFAULT_MAX_VOLUME = 7;

  /**the default current volume of a stream*/
  private static final int DEFAULT_VOLUME     = 7;

  /**return value for an invalid stream*/
  private static final int INVALID_VOLUME     = 0;

  /**flag to indicate to do nothing when setting stream volume*/
  private static final int FLAG_NO_ACTION     = 0;

  private AudioFocusRequest lastAudioFocusRequest;
  private int               nextResponseValue = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  private AudioManager.OnAudioFocusChangeListener lastAbandonedAudioFocusListener;
  private HashMap<Integer, AudioStream> streamStatus = new HashMap<Integer, AudioStream>();

  public ShadowAudioManager() {
    /*
     * initialise the state of each media stream. List of valid audio stream
     * was taken from AndroidManager API 17.
     */
    int[] allStreams = {
          AudioManager.STREAM_MUSIC,
          AudioManager.STREAM_ALARM,
          AudioManager.STREAM_NOTIFICATION,
          AudioManager.STREAM_RING,
          AudioManager.STREAM_SYSTEM,
          AudioManager.STREAM_VOICE_CALL,
          AudioManager.STREAM_DTMF
    };

    for (int stream : allStreams) {
      streamStatus.put(
              stream,
              new AudioStream(DEFAULT_VOLUME, DEFAULT_MAX_VOLUME, FLAG_NO_ACTION)
      );
    }
    /*the music and dtmf streams have a higher max volume*/
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

  /*Note, the flag has no effect*/
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

  /**
   * set the maximum volume for all streams
   *
   * @param streamMaxVolume the new maximum volume for all streams
   */
  public void setStreamMaxVolume(int streamMaxVolume) {

    for (Map.Entry<Integer, AudioStream> entry : streamStatus.entrySet()) {
      entry.getValue().setMaxVolume(streamMaxVolume);
    }
  }

  /**
   * set the current volume for all streams
   *
   * @param streamVolume the new current volume for all streams
   */
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

  /**
   * contains the status of an audio stream
   */
  private static class AudioStream {
    /**the current volume*/
    private int currentVolume;

    /**the maximum volume*/
    private int maxVolume;

    /**flag for audios stream*/
    private int flag;

    /**
     * initalise the audio stream
     *
     * @param currVol   the default current volume
     * @param maxVol    the default max volume
     * @param flag      default flag value
     */
    public AudioStream(int currVol, int maxVol, int flag) {

      setCurrentVolume(currVol);
      setMaxVolume(maxVol);
      setFlag(flag);
    }

    /**
     * get the current volume
     *
     * @return current volume
     */
    public int getCurrentVolume() {
      return currentVolume;
    }

    /**
     * get the maximum volume
     *
     * @return maximum volume
     */
    public int getMaxVolume() {
      return maxVolume;
    }

    /**
     * get the flag
     *
     * @return flag
     */
    public int getFlag() {
      return flag;
    }

    /**
     * set the current volume. Volume will not be less than 0 or greater than
     * maxVolume.
     *
     * @param vol   the new current volume
     */
    public void setCurrentVolume(int vol) {

      if (vol > maxVolume) {
        vol = maxVolume;
      }
      else if (vol < 0) {
        vol = 0;
      }

      currentVolume = vol;

    }

    /**
     * set the maximum volume
     *
     * @param vol  the new maximum volume value
     */
    public void setMaxVolume(int vol) {
      maxVolume = vol;
    }

    /**
     * set the flag
     *
     * @param flag  the new flag value
     */
    public void setFlag(int flag) {
      this.flag = flag;
    }



  }
}
