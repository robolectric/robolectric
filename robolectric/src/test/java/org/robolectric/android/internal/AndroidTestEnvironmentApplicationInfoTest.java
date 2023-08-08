package org.robolectric.android.internal;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@Config(application = AndroidTestEnvironmentApplicationInfoTest.ThisApplication.class)
@RunWith(AndroidJUnit4.class)
public final class AndroidTestEnvironmentApplicationInfoTest {

  @Test
  public void testApplicationInfoIncludesConfiguredAppClass() {
    Application app = ApplicationProvider.getApplicationContext();
    assertThat(app).isInstanceOf(ThisApplication.class);
    assertThat(app.getApplicationInfo().className).isEqualTo(ThisApplication.class.getName());
  }

  public static final class ThisApplication extends Application {}
}
