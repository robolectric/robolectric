package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.pm.pkg.component.AconfigFlags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VANILLA_ICE_CREAM, maxSdk = VANILLA_ICE_CREAM)
public final class ShadowAconfigFlagsTest {

  @Test
  public void constructor_doesNotLogErrors() {
    ReflectionHelpers.callConstructor(AconfigFlags.class);

    for (LogItem item : ShadowLog.getLogsForTag("AconfigFlags")) {
      assertThat(item.type).isLessThan(Log.ERROR);
    }
  }

  @Test
  public void getFlagValue_unknownFlag_returnsNull() {
    AconfigFlags flags = ReflectionHelpers.callConstructor(AconfigFlags.class);

    assertThat(flags.getFlagValue("com.example.flags.some_flag")).isNull();
  }
}
