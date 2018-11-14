package android.content.pm;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SdkSuppress;
import androidx.test.runner.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Compatibility test for {@link PackageManager}
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public final class PackageManagerTest {

  private Context context;

  @Before
  public void setup() throws Exception {
    context = InstrumentationRegistry.getTargetContext();
  }

  @Test
  @Config(minSdk = O)
  @SdkSuppress(minSdkVersion = O)
  public void isInstantApp_shouldNotBlowup() {
    assertThat(context.getPackageManager().isInstantApp()).isFalse();
  }

  @Test
  public void whenComponentDisabled_itIsReportedAsSuch() throws Exception {
    PackageManager pm = context.getPackageManager();
    ComponentName disabledActivityName =
        new ComponentName(context, "org.robolectric.DisabledTestActivity");

    ActivityInfo info =
        pm.getActivityInfo(disabledActivityName, PackageManager.GET_DISABLED_COMPONENTS);
    assertThat(info).isNotNull();
    assertThat(info.enabled).isFalse();
  }
}
