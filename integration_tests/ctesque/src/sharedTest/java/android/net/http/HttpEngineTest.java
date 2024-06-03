package android.net.http;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@DoNotInstrument
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class HttpEngineTest {
  @Test
  public void supportHttpClientOpenConnection() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();

    HttpEngine engine = new HttpEngine.Builder(context).build();

    URL url = new URL("https://google.com/robots.txt");
    HttpURLConnection con = (HttpURLConnection) engine.openConnection(url);

    try {
      assertThat(con.getURL()).isEqualTo(url);

      // Check how to test network in robolectric builds
      //    String response = new String(con.getInputStream().readAllBytes());
      //    assertThat(response).contains("Disallow");
    } finally {
      con.disconnect();
    }
  }
}
