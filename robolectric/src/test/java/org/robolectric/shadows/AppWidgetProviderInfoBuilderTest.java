package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.L;
import static com.google.common.truth.Truth.assertThat;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests for {@link AppWidgetProviderInfoBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN)
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
    AppWidgetProviderInfoBuilder builder = AppWidgetProviderInfoBuilder.newBuilder();
    if (RuntimeEnvironment.getApiLevel() >= L) {
      applicationInfo = new ApplicationInfo();
      applicationInfo.uid = UserHandle.myUserId();
      providerInfo.applicationInfo = applicationInfo;
      builder.setProviderInfo(providerInfo);
    }
    appWidgetProviderInfo = builder.build();
  }

  @Test
  public void appWidgetProviderInfo_canBeBuilt() {
    appWidgetProviderInfo.icon = 100;
    Parcel parcel = Parcel.obtain();
    parcel.writeParcelable(appWidgetProviderInfo, 0);
    parcel.setDataPosition(0);
    AppWidgetProviderInfo info2 =
        parcel.readParcelable(AppWidgetProviderInfo.class.getClassLoader());
    assertThat(info2).isNotNull();
    assertThat(info2.icon).isEqualTo(100);
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
