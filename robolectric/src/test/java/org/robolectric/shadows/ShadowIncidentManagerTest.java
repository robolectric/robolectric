package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.IncidentManager.IncidentReport;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.R)
public final class ShadowIncidentManagerTest {

  @Test
  public void expectedResult() {
    ShadowIncidentManager shadowIncidentManager = new ShadowIncidentManager();
    shadowIncidentManager.addIncidentReport(
        Uri.parse("content://foo.com/1"), new IncidentReport(Parcel.obtain()));
    assertThat(shadowIncidentManager.getIncidentReportList("test_caller")).hasSize(1);
    assertThat(shadowIncidentManager.getIncidentReport(Uri.parse("content://foo.com/1")))
        .isNotNull();

    shadowIncidentManager.deleteIncidentReports(Uri.parse("content://foo.com/1"));
    assertThat(shadowIncidentManager.getIncidentReportList("test_caller")).isEmpty();
    assertThat(shadowIncidentManager.getIncidentReport(Uri.parse("content://foo.com/1"))).isNull();
  }
}
