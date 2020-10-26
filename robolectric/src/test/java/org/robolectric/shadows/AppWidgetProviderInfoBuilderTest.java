package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.CUPCAKE;
import static android.os.Build.VERSION_CODES.L;
import static com.google.common.truth.Truth.assertThat;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link AppWidgetProviderInfoBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = CUPCAKE)
public class AppWidgetProviderInfoBuilderTest {
  private Context context;
  private PackageManager packageManager;
  private AppWidgetProviderInfo appWidgetProviderInfo;
  private ActivityInfo providerInfo;
  private ApplicationInfo applicationInfo;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    packageManager = context.getPackageManager();
    providerInfo = new ActivityInfo();
    providerInfo.nonLocalizedLabel = "nonLocalizedLabel";
    providerInfo.icon = -1;
    applicationInfo = new ApplicationInfo();
    applicationInfo.uid = UserHandle.myUserId();
    providerInfo.applicationInfo = applicationInfo;
    appWidgetProviderInfo =
        AppWidgetProviderInfoBuilder.newBuilder().setProviderInfo(providerInfo).build();
  }

  @Test
  @Config(minSdk = L)
  public void getProfile_shouldReturnUserHandleWithAssignedUID() {
    assertThat(appWidgetProviderInfo.getProfile().getIdentifier()).isEqualTo(applicationInfo.uid);
  }

  @Test
  @Config(minSdk = L)
  public void loadIcon_shouldReturnNonNullIcon() {
    assertThat(appWidgetProviderInfo.loadIcon(context, 240)).isNotNull();
  }

  @Test
  @Config(minSdk = L)
  public void loadLabel_shouldReturnAssignedLabel() {
    assertThat(appWidgetProviderInfo.loadLabel(packageManager))
        .isEqualTo(providerInfo.nonLocalizedLabel.toString());
  }
}
