package org.robolectric;

import android.app.IntentService;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.IBinder;

public class CustomConstructorServices {

  public static class CustomConstructorService extends Service {
    private final int intValue;

    public CustomConstructorService(int intValue) {
      this.intValue = intValue;
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    public int getIntValue() {
      return intValue;
    }
  }

  @SuppressWarnings("deprecation")
  public static class CustomConstructorIntentService extends IntentService {
    private final int intValue;

    public CustomConstructorIntentService(int intValue) {
      super("test");
      this.intValue = intValue;
    }

    @Override
    protected void onHandleIntent(Intent intent) {}

    public int getIntValue() {
      return intValue;
    }
  }

  public static class CustomConstructorJobService extends JobService {
    private final int intValue;

    public CustomConstructorJobService(int intValue) {
      this.intValue = intValue;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
      return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
      return true;
    }

    public int getIntValue() {
      return intValue;
    }
  }
}
