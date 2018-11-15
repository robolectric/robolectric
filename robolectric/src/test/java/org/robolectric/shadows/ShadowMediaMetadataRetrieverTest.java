package org.robolectric.shadows;

import static android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM;
import static android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST;
import static android.media.MediaMetadataRetriever.METADATA_KEY_TITLE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowMediaMetadataRetriever.addException;
import static org.robolectric.shadows.ShadowMediaMetadataRetriever.addFrame;
import static org.robolectric.shadows.ShadowMediaMetadataRetriever.addMetadata;
import static org.robolectric.shadows.util.DataSource.toDataSource;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowMediaMetadataRetrieverTest {
  private final String path = "/media/foo.mp3";
  private final String path2 = "/media/foo2.mp3";
  private final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
  private final MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
  private final Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
  private final Bitmap bitmap2 = Bitmap.createBitmap(11, 11, Bitmap.Config.ARGB_8888);
  private FileDescriptor fd = new FileDescriptor();

  @Test
  public void extractMetadata_shouldReturnValue() {
    addMetadata(path, METADATA_KEY_ARTIST, "The Rolling Stones");
    addMetadata(path, METADATA_KEY_ALBUM, "Sticky Fingers");
    addMetadata(path, METADATA_KEY_TITLE, "Brown Sugar");

    retriever.setDataSource(path);
    assertThat(retriever.extractMetadata(METADATA_KEY_ARTIST)).isEqualTo("The Rolling Stones");
    assertThat(retriever.extractMetadata(METADATA_KEY_ALBUM)).isEqualTo("Sticky Fingers");
    assertThat(retriever.extractMetadata(METADATA_KEY_TITLE)).isEqualTo("Brown Sugar");
  }

  @Test
  public void getFrameAtTime_shouldDependOnDataSource() {
    addFrame(path, 1, bitmap);
    addFrame(path2, 1, bitmap2);
    retriever.setDataSource(path);
    retriever2.setDataSource(path2);
    assertThat(retriever.getFrameAtTime(1)).isEqualTo(bitmap);
    assertThat(retriever.getFrameAtTime(1)).isNotEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(1)).isEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(1)).isNotEqualTo(bitmap);
  }

  @Test
  public void setDataSource_usersSameDataSourceForFileDescriptors() {
    addFrame(fd, 1, bitmap);
    addFrame(fd, 0, 0, 1, bitmap2);
    retriever.setDataSource(fd);
    assertThat(retriever.getFrameAtTime(1)).isEqualTo(bitmap2);
  }

  @Test
  public void setDataSource_fdsWithDifferentOffsetsAreDifferentDataSources() {
    addFrame(fd, 1, bitmap);
    addFrame(fd, 1, 0, 1, bitmap2);
    retriever.setDataSource(fd);
    retriever2.setDataSource(fd, 1, 0);
    assertThat(retriever.getFrameAtTime(1)).isEqualTo(bitmap);
    assertThat(retriever.getFrameAtTime(1)).isNotEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(1)).isEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(1)).isNotEqualTo(bitmap);
  }

  @Test
  public void getFrameAtTime_shouldDependOnTime() {
    Context context = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse(path);
    addFrame(context, uri, 12, bitmap);
    addFrame(context, uri, 13, bitmap2);
    retriever.setDataSource(context, uri);
    assertThat(retriever.getFrameAtTime(12)).isEqualTo(bitmap);
    assertThat(retriever.getFrameAtTime(13)).isNotEqualTo(bitmap);
    assertThat(retriever.getFrameAtTime(12)).isNotEqualTo(bitmap2);
    assertThat(retriever.getFrameAtTime(13)).isEqualTo(bitmap2);
  }

  @Test
  public void setDataSource_ignoresHeadersWhenShadowed() {
    Context context = ApplicationProvider.getApplicationContext();
    Uri uri = Uri.parse(path);
    Map<String, String> headers = new HashMap<>();
    headers.put("cookie", "nomnomnom");
    retriever.setDataSource(context, uri);
    retriever2.setDataSource(uri.toString(), headers);
    addFrame(context, uri, 10, bitmap);
    addFrame(uri.toString(), headers, 13, bitmap2);
    assertThat(retriever.getFrameAtTime(10)).isEqualTo(bitmap);
    assertThat(retriever.getFrameAtTime(13)).isEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(13)).isEqualTo(bitmap2);
    assertThat(retriever2.getFrameAtTime(10)).isEqualTo(bitmap);
  }

  @Test
  public void reset_clearsStaticValues() {
    addMetadata(path, METADATA_KEY_ARTIST, "The Rolling Stones");
    addFrame(path, 1, bitmap);
    addException(toDataSource(path2), new IllegalArgumentException());
    retriever.setDataSource(path);
    assertThat(retriever.extractMetadata(METADATA_KEY_ARTIST)).isEqualTo("The Rolling Stones");
    assertThat(retriever.getFrameAtTime(1)).isSameAs(bitmap);
    try {
      retriever2.setDataSource(path2);
      fail("Expected exception");
    } catch (IllegalArgumentException e) {}
    ShadowMediaMetadataRetriever.reset();
    assertThat(retriever.extractMetadata(METADATA_KEY_ARTIST)).isNull();
    assertThat(retriever.getFrameAtTime(1)).isNull();
    try {
      retriever2.setDataSource(path2);
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Shouldn't throw exception after reset", e);
    }
  }
  
  @Test
  public void setDataSourceException_withAllowedException() {
    RuntimeException e = new RuntimeException("some dummy message");
    addException(toDataSource(path), e);
    try {
      retriever.setDataSource(path);
      fail("Expected exception");
    } catch (Exception caught) {
      assertThat(caught).isSameAs(e);
      assertThat(e.getStackTrace()[0].getClassName())
         .named("Stack trace should originate in Shadow")
         .isEqualTo(ShadowMediaMetadataRetriever.class.getName());
    }
  }
}
