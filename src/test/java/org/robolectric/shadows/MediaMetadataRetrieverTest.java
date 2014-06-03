package org.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import android.media.MediaMetadataRetriever;
import static org.fest.assertions.api.Assertions.*;
import static android.media.MediaMetadataRetriever.*;
import static org.robolectric.shadows.ShadowMediaMetadataRetriever.*;

@RunWith(TestRunners.WithDefaults.class)
public class MediaMetadataRetrieverTest {
  private final String path = "/media/foo.mp3";
  private final MediaMetadataRetriever retriever = new MediaMetadataRetriever();

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
  public void reset_clearsStaticValues() {
    addMetadata(path, METADATA_KEY_ARTIST, "The Rolling Stones");
    retriever.setDataSource(path);
    assertThat(retriever.extractMetadata(METADATA_KEY_ARTIST)).isEqualTo("The Rolling Stones");
    ShadowMediaMetadataRetriever.reset();
    assertThat(retriever.extractMetadata(METADATA_KEY_ARTIST)).isNull();
  }
}
