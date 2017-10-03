package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;

import android.app.JobSchedulerImpl;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobWorkItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

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

  @Implementation(minSdk = N)
  public abstract JobInfo getPendingJob(int jobId);

  @Implementation(minSdk = O)
  public abstract int enqueue(JobInfo job, JobWorkItem work);

  public abstract void failOnJob(int jobId);

  @Implements(value = JobSchedulerImpl.class, isInAndroidSdk = false)
  public static class ShadowJobSchedulerImpl extends ShadowJobScheduler {

    private Map<Integer, JobInfo> scheduledJobs = new HashMap<>();
    private Set<Integer> jobsToFail = new HashSet<>();

    @Override @Implementation
    public int schedule(JobInfo job) {
      if (jobsToFail.contains(job.getId())) {
        return JobScheduler.RESULT_FAILURE;
      }

      scheduledJobs.put(job.getId(), job);
      return JobScheduler.RESULT_SUCCESS;
    }

    @Override @Implementation
    public void cancel(int jobId) {
      scheduledJobs.remove(jobId);
    }

    @Override @Implementation
    public void cancelAll() {
      scheduledJobs.clear();
    }

    @Override @Implementation
    public List<JobInfo> getAllPendingJobs() {
      return new ArrayList<>(scheduledJobs.values());
    }

    @Override @Implementation(minSdk = N)
    public JobInfo getPendingJob(int jobId) {
      return scheduledJobs.get(jobId);
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
  }
}
