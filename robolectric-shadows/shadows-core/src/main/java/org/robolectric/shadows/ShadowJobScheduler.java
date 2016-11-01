package org.robolectric.shadows;

import android.app.JobSchedulerImpl;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Shadow for {@link android.app.job.JobScheduler}.
 */
@Implements(value = JobScheduler.class, minSdk = LOLLIPOP)
public abstract class ShadowJobScheduler {

  @Implementation
  public abstract int schedule(JobInfo job);

  @Implementation
  public abstract void cancel(int jobId);

  @Implementation
  public abstract void cancelAll();

  @Implementation
  public abstract List<JobInfo> getAllPendingJobs();

  public abstract void failOnJob(int jobId);

  @Implements(value = JobSchedulerImpl.class, isInAndroidSdk = false)
  public static class ShadowJobSchedulerImpl extends ShadowJobScheduler {

    private Map<Integer, JobInfo> scheduledJobs = new HashMap<>();
    private Set<Integer> jobsToFail = new HashSet<>();

    @Implementation
    public int schedule(JobInfo job) {
      if (jobsToFail.contains(job.getId())) {
        return JobScheduler.RESULT_FAILURE;
      }

      scheduledJobs.put(job.getId(), job);
      return JobScheduler.RESULT_SUCCESS;
    }

    @Implementation
    public void cancel(int jobId) {
    }

    @Implementation
    public void cancelAll() {
      scheduledJobs.clear();
    }

    @Implementation
    public List<JobInfo> getAllPendingJobs() {
      return new ArrayList<>(scheduledJobs.values());
    }

    @Override
    public void failOnJob(int jobId) {
      jobsToFail.add(jobId);
    }
  }
}