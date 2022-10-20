package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.net.Uri;
import android.os.IncidentManager;
import android.os.IncidentManager.IncidentReport;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow class for {@link IncidentManager}. */
@Implements(value = IncidentManager.class, minSdk = R, isInAndroidSdk = false)
public class ShadowIncidentManager {

  private final Map<Uri, IncidentReport> reports = new LinkedHashMap<>();

  @NonNull
  @Implementation
  protected List<Uri> getIncidentReportList(String receiverClass) {
    return new ArrayList<>(reports.keySet());
  }

  @Implementation
  protected IncidentReport getIncidentReport(Uri uri) {
    return reports.get(uri);
  }

  @Implementation
  protected void deleteIncidentReports(Uri uri) {
    reports.remove(uri);
  }

  /** Add {@link IncidentReport} to the list of reported incidents. */
  public void addIncidentReport(Uri uri, IncidentReport report) {
    reports.put(uri, report);
  }
}
