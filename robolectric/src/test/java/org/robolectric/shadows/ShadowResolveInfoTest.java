package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.ResolveInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowResolveInfoTest {
  @Test
  public void testNewResolveInfoWithActivity() {
    ResolveInfo mResolveInfo =
        ShadowResolveInfo.newResolveInfo("name", "package", "fragmentActivity");
    assertThat(mResolveInfo.loadLabel(null).toString()).isEqualTo("name");
    assertThat(mResolveInfo.activityInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.applicationInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.name).isEqualTo("fragmentActivity");
    assertThat(mResolveInfo.toString()).isNotEmpty();
  }

  @Test
  public void testNewResolveInfoWithoutActivity() {
    ResolveInfo mResolveInfo = ShadowResolveInfo.newResolveInfo("name", "package");
    assertThat(mResolveInfo.loadLabel(null).toString()).isEqualTo("name");
    assertThat(mResolveInfo.activityInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.applicationInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.name).isEqualTo("package.TestActivity");
    assertThat(mResolveInfo.toString()).isNotEmpty();
  }
}
