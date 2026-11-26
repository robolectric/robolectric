package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.annotation.RequiresApi;
import android.annotation.SystemApi;
import android.app.JobSchedulerImpl;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import android.os.Build;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

@Implements(JobScheduler.class)
public abstract class ShadowJobScheduler {

  @Implementation
  protected abstract int schedule(JobInfo job);

  @Implementation
  @SystemApi
  @HiddenApi
  protected abstract int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag);

  @Implementation
  protected abstract void cancel(int jobId);

  @Implementation
  protected abstract void cancelAll();

  @Implementation
  protected abstract List<JobInfo> getAllPendingJobs();

  @Implementation
  @HiddenApi
  public abstract JobInfo getPendingJob(int jobId);

  @Implementation(minSdk = O)
  protected abstract int enqueue(JobInfo job, JobWorkItem work);

  public abstract void failOnJob(int jobId);

  /** Whether to fail a job if it is set as expedited. */
  public abstract void failExpeditedJob(boolean enabled);

  @Implements(value = JobSchedulerImpl.class, isInAndroidSdk = false)
  public static class ShadowJobSchedulerImpl extends ShadowJobScheduler {

    /**
     * The jobs scheduled through every {@link JobScheduler}, by namespace. Each context has its own
     * {@link JobScheduler} instance, but on a device they all talk to one job service, so a job
     * scheduled through an activity's instance is visible through the application's. Jobs in
     * different namespaces ({@link JobScheduler#forNamespace}) stay apart, as on a device.
     */
    private static final Map<String, Map<Integer, JobInfo>> scheduledJobsByNamespace =
        new HashMap<>();

    @RealObject private JobScheduler realJobScheduler;
    private final Set<Integer> jobsToFail = Collections.synchronizedSet(new HashSet<>());
    private boolean failExpeditedJobEnabled;

    @Resetter
    public static void reset() {
      synchronized (scheduledJobsByNamespace) {
        scheduledJobsByNamespace.clear();
      }
    }

    private Map<Integer, JobInfo> scheduledJobs() {
      String namespace =
          RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE
              ? realJobScheduler.getNamespace()
              : null;
      synchronized (scheduledJobsByNamespace) {
        return scheduledJobsByNamespace.computeIfAbsent(
            namespace, unused -> Collections.synchronizedMap(new LinkedHashMap<>()));
      }
    }

    @Override
    @Implementation
    public int schedule(JobInfo job) {
      if (jobsToFail.contains(job.getId())) {
        return JobScheduler.RESULT_FAILURE;
      }

      if (Build.VERSION.SDK_INT >= S && failExpeditedJobEnabled && job.isExpedited()) {
        return JobScheduler.RESULT_FAILURE;
      }

      scheduledJobs().put(job.getId(), job);
      return JobScheduler.RESULT_SUCCESS;
    }

    /**
     * Simple implementation redirecting all calls to {@link #schedule(JobInfo)}. Ignores all
     * arguments other than {@code job}.
     */
    @Override
    @Implementation
    @SystemApi
    @HiddenApi
    protected int scheduleAsPackage(JobInfo job, String packageName, int userId, String tag) {
      return schedule(job);
    }

    @Override
    @Implementation
    public void cancel(int jobId) {
      scheduledJobs().remove(jobId);
    }

    @Override
    @Implementation
    public void cancelAll() {
      scheduledJobs().clear();
    }

    @Override
    @Implementation
    public List<JobInfo> getAllPendingJobs() {
      return new ArrayList<>(scheduledJobs().values());
    }

    @Override
    @Implementation
    public JobInfo getPendingJob(int jobId) {
      return scheduledJobs().get(jobId);
    }

    @Override
    @Implementation(minSdk = O)
    public int enqueue(JobInfo job, JobWorkItem work) {
      // Shadow-wise, enqueue and schedule are identical.
      return schedule(job);
    }

    @Override
    public void failOnJob(int jobId) {
      jobsToFail.add(jobId);
    }

    @Override
    @RequiresApi(S)
    public void failExpeditedJob(boolean enabled) {
      failExpeditedJobEnabled = enabled;
    }
  }
}
