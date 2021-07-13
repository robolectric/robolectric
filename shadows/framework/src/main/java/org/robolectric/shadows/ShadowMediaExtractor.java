package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static java.lang.Math.min;
import static org.robolectric.shadows.util.DataSource.toDataSource;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.util.DataSource;

/**
 * A shadow for the MediaExtractor class.
 *
 * <p>Returns data previously injected by {@link #addTrack(DataSource, MediaFormat, byte[])}.
 *
 * <p>Note several limitations, due to not using actual media codecs for decoding:
 *   * Only one track may be selected at a time; multi-track selection is not supported.
 *   * {@link #advance()} will advance by the size of the last read (i.e. the return value of the
 *     last call to {@link #readSampleData(ByteBuffer, int)}).
 *   * {@link MediaExtractor#getSampleTime()} and {@link MediaExtractor#getSampleSize()} are
 *     unimplemented.
 *   * {@link MediaExtractor#seekTo()} is unimplemented.
 */
@Implements(MediaExtractor.class)
public class ShadowMediaExtractor {

  private static class TrackInfo {
    MediaFormat format;
    byte[] sampleData;
  }

  private static final Map<DataSource, List<TrackInfo>> tracksMap = new HashMap<>();

  private List<TrackInfo> tracks;
  private int[] trackSampleReadPositions;
  private int[] trackLastReadSize;
  private int selectedTrackIndex = -1;

  /**
   * Adds a track of data to an associated {@link org.robolectric.shadows.util.DataSource}.
   *
   * @param format the format which will be returned by {@link MediaExtractor#getTrackFormat(int)}
   * @param sampleData the data which will be iterated upon and returned by {@link
   *     MediaExtractor#readSampleData(ByteBuffer, int)}.
   */
  public static void addTrack(DataSource dataSource, MediaFormat format, byte[] sampleData) {
    TrackInfo trackInfo = new TrackInfo();
    trackInfo.format = format;
    trackInfo.sampleData = sampleData;
    tracksMap.putIfAbsent(dataSource, new ArrayList<TrackInfo>());
    List<TrackInfo> tracks = tracksMap.get(dataSource);
    tracks.add(trackInfo);
  }

  private void setDataSource(DataSource dataSource) {
    if (tracksMap.containsKey(dataSource)) {
      this.tracks = tracksMap.get(dataSource);
    } else {
      this.tracks = new ArrayList<>();
    }

    this.trackSampleReadPositions = new int[tracks.size()];
    Arrays.fill(trackSampleReadPositions, 0);
    this.trackLastReadSize = new int[tracks.size()];
    Arrays.fill(trackLastReadSize, 0);
  }

  @Implementation(minSdk = N)
  protected void setDataSource(AssetFileDescriptor assetFileDescriptor) {
    setDataSource(toDataSource(assetFileDescriptor));
  }

  @Implementation
  protected void setDataSource(Context context, Uri uri, Map<String, String> headers) {
    setDataSource(toDataSource(context, uri, headers));
  }

  @Implementation
  protected void setDataSource(FileDescriptor fileDescriptor) {
    setDataSource(toDataSource(fileDescriptor));
  }

  @Implementation(minSdk = M)
  protected void setDataSource(MediaDataSource mediaDataSource) {
    setDataSource(toDataSource(mediaDataSource));
  }

  @Implementation
  protected void setDataSource(FileDescriptor fileDescriptor, long offset, long length) {
    setDataSource(toDataSource(fileDescriptor, offset, length));
  }

  @Implementation
  protected void setDataSource(String path) {
    setDataSource(toDataSource(path));
  }

  @Implementation
  protected void setDataSource(String path, Map<String, String> headers) {
    setDataSource(toDataSource(path));
  }

  @Implementation
  protected boolean advance() {
    if (selectedTrackIndex == -1) {
      throw new IllegalStateException("Called advance() with no selected track");
    }

    int readPosition = trackSampleReadPositions[selectedTrackIndex];
    int trackDataLength = tracks.get(selectedTrackIndex).sampleData.length;
    if (readPosition >= trackDataLength) {
      return false;
    }

    trackSampleReadPositions[selectedTrackIndex] += trackLastReadSize[selectedTrackIndex];
    return true;
  }

  @Implementation
  protected int getSampleTrackIndex() {
    return selectedTrackIndex;
  }

  @Implementation
  protected final int getTrackCount() {
    return tracks.size();
  }

  @Implementation
  protected MediaFormat getTrackFormat(int index) {
    if (index >= tracks.size()) {
      throw new ArrayIndexOutOfBoundsException(
          "Called getTrackFormat() with index:"
              + index
              + ", beyond number of tracks:"
              + tracks.size());
    }

    return tracks.get(index).format;
  }

  @Implementation
  protected int readSampleData(ByteBuffer byteBuf, int offset) {
    if (selectedTrackIndex == -1) {
      return 0;
    }
    int currentReadPosition = trackSampleReadPositions[selectedTrackIndex];
    TrackInfo trackInfo = tracks.get(selectedTrackIndex);
    int trackDataLength = trackInfo.sampleData.length;
    if (currentReadPosition >= trackDataLength) {
      return -1;
    }

    int length = min(byteBuf.capacity(), trackDataLength - currentReadPosition);
    byteBuf.put(trackInfo.sampleData, currentReadPosition, length);
    trackLastReadSize[selectedTrackIndex] = length;
    return length;
  }

  @Implementation
  protected void selectTrack(int index) {
    if (selectedTrackIndex != -1) {
      throw new IllegalStateException(
          "Called selectTrack() when there is already a track selected; call unselectTrack() first."
              + " ShadowMediaExtractor does not support multiple track selection.");
    }
    if (index >= tracks.size()) {
      throw new ArrayIndexOutOfBoundsException(
          "Called selectTrack() with index:"
              + index
              + ", beyond number of tracks:"
              + tracks.size());
    }

    selectedTrackIndex = index;
  }

  @Implementation
  protected void unselectTrack(int index) {
    if (selectedTrackIndex != index) {
      throw new IllegalStateException(
          "Called unselectTrack() on a track other than the single selected track."
              + " ShadowMediaExtractor does not support multiple track selection.");
    }
    selectedTrackIndex = -1;
  }

  @Resetter
  public static void reset() {
    tracksMap.clear();
  }
}
