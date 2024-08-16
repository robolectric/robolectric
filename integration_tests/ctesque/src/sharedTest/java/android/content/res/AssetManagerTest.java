package android.content.res;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.CharStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Compatibility test for {@link AssetManager} */
@RunWith(AndroidJUnit4.class)
public class AssetManagerTest {

  private AssetManager assetManager;

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  @Before
  public void setup() throws Exception {
    Context context = getTargetContext();
    assetManager = context.getResources().getAssets();
  }

  @Test
  public void assetsPathListing() throws IOException {
    assertThat(assetManager.list(""))
        .asList()
        .containsAtLeast("assetsHome.txt", "robolectric.png", "myFont.ttf");

    assertThat(assetManager.list("testing")).asList().contains("hello.txt");

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
    final String contents =
        CharStreams.toString(
            new InputStreamReader(
                assetManager.open("assetsHome.txt", AssetManager.ACCESS_BUFFER), UTF_8));
    assertThat(contents).isEqualTo("assetsHome!");
  }

  @Test
  public void openFd_shouldProvideFileDescriptorForAsset() throws Exception {
    AssetFileDescriptor assetFileDescriptor = assetManager.openFd("assetsHome.txt");
    FileInputStream fis = assetFileDescriptor.createInputStream();
    InputStreamReader isr = new InputStreamReader(fis, UTF_8);
    String actual = CharStreams.toString(isr);
    assertThat(actual).isEqualTo("assetsHome!");
    assertThat(assetFileDescriptor.getLength()).isEqualTo(11);
  }

  @Test
  public void open_shouldProvideFileDescriptor() throws Exception {
    File file =
        new File(
            getTargetContext().getFilesDir()
                + File.separator
                + "open_shouldProvideFileDescriptor.txt");
    FileOutputStream output = new FileOutputStream(file);
    output.write("hi".getBytes());
    output.close();

    ParcelFileDescriptor parcelFileDescriptor =
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    AssetFileDescriptor assetFileDescriptor =
        new AssetFileDescriptor(parcelFileDescriptor, 0, "hi".getBytes().length);

    assertThat(
            CharStreams.toString(
                new InputStreamReader(assetFileDescriptor.createInputStream(), UTF_8)))
        .isEqualTo("hi");
    assertThat(assetFileDescriptor.getLength()).isEqualTo(2);
    assetFileDescriptor.close();
  }
}
