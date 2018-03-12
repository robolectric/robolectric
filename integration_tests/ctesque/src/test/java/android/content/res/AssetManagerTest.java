package android.content.res;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Compatibility test for {@link AssetManager}
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class AssetManagerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AssetManager assetManager;

  @Before
  public void setup() throws Exception {
    Context context = InstrumentationRegistry.getTargetContext();
    assetManager = context.getResources().getAssets();
  }

  @Test
  public void assetsPathListing() throws IOException {
    assertThat(assetManager.list("")).asList()
        .containsAllOf("assetsHome.txt", "robolectric.png", "myFont.ttf");

    assertThat(assetManager.list("testing")).asList()
        .contains("hello.txt");

    assertThat(assetManager.list("bogus-dir")).isEmpty();
  }

  @Test
  public void open_shouldOpenFile() throws IOException {
    final String contents =
        CharStreams.toString(new InputStreamReader(assetManager.open("assetsHome.txt"), UTF_8));
    assertThat(contents).isEqualTo("assetsHome!");
  }

  @Test
  public void open_withAccessMode_shouldOpenFile() throws IOException {
    final String contents = CharStreams.toString(
        new InputStreamReader(assetManager.open("assetsHome.txt", AssetManager.ACCESS_BUFFER), UTF_8));
    assertThat(contents).isEqualTo("assetsHome!");
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForAsset() throws Exception {
    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("assetsHome.txt");
    assertThat(CharStreams.toString(new InputStreamReader(assetFileDescriptor.createInputStream(), UTF_8)))
        .isEqualTo("assetsHome!");
    assertThat(assetFileDescriptor.getLength()).isEqualTo(11);
  }
}
