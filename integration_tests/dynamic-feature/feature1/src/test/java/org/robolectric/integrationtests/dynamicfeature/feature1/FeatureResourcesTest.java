package org.robolectric.integrationtests.dynamicfeature.feature1;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Robolectric tests that run inside the feature1 dynamic feature module. */
@RunWith(RobolectricTestRunner.class)
public class FeatureResourcesTest {
  private static final String BASE_PACKAGE = "org.robolectric.integrationtests.dynamicfeature";

  @Test
  public void getString_featureResource() {
    assertThat(RuntimeEnvironment.getApplication().getString(R.string.feature_greeting))
        .isEqualTo("Hello from feature 1");
  }

  @Test
  public void getString_baseResource() {
    Application app = RuntimeEnvironment.getApplication();
    int id = app.getResources().getIdentifier("app_name", "string", BASE_PACKAGE);
    assertThat(id).isNotEqualTo(0);
    assertThat(app.getString(id)).isEqualTo("Dynamic feature");
  }

  @Test
  public void packageInfo_isBaseAppWithFeatureSplit() throws Exception {
    Application app = RuntimeEnvironment.getApplication();
    assertThat(app.getPackageName()).isEqualTo(BASE_PACKAGE);
    ApplicationInfo appInfo = app.getApplicationInfo();
    assertThat(appInfo.splitNames).asList().containsExactly("feature1");
    assertThat(appInfo.loadLabel(app.getPackageManager()).toString()).isEqualTo("Dynamic feature");
    assertThat(app.getPackageManager().getPackageInfo(BASE_PACKAGE, 0).splitNames)
        .asList()
        .containsExactly("feature1");
  }

  @Test
  public void createContextForSplit_readsFeatureResources() throws Exception {
    Context split = RuntimeEnvironment.getApplication().createContextForSplit("feature1");
    assertThat(split.getString(R.string.feature_greeting)).isEqualTo("Hello from feature 1");
  }

  @Test
  public void featureActivity_inflatesFeatureAndBaseStrings() {
    FeatureActivity activity = Robolectric.buildActivity(FeatureActivity.class).setup().get();
    assertThat(((TextView) activity.findViewById(R.id.feature_text)).getText().toString())
        .isEqualTo("Hello from feature 1");
    assertThat(((TextView) activity.findViewById(R.id.base_text)).getText().toString())
        .isEqualTo("Dynamic feature");
  }
}
