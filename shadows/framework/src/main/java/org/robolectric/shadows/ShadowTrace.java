package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.base.Verify.verifyNotNull;

import android.os.Trace;
import android.util.Log;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow implementation for {@link Trace}, which stores the traces locally in arrays (unlike the
 * real implementation) and allows reading them.
 */
@Implements(Trace.class)
public class ShadowTrace {
  private static final String TAG = "ShadowTrace";

  private static final ThreadLocal<Deque<String>> currentSections =
      ThreadLocal.withInitial(() -> new ArrayDeque<>());

  private static final ThreadLocal<Queue<String>> previousSections =
      ThreadLocal.withInitial((Supplier<Deque<String>>) () -> new ArrayDeque<>());

  private static final Set<AsyncTraceSection> currentAsyncSections = new HashSet<>();

  private static final Set<AsyncTraceSection> previousAsyncSections = new HashSet<>();

  private static final List<Counter> counters = new ArrayList<>();

  private static final boolean CRASH_ON_INCORRECT_USAGE_DEFAULT = true;
  private static boolean crashOnIncorrectUsage = CRASH_ON_INCORRECT_USAGE_DEFAULT;
  private static boolean isEnabled = true;

  private static final long TRACE_TAG_APP = 1L << 12;
  private static final int MAX_SECTION_NAME_LEN = 127;

  private static long tags = TRACE_TAG_APP;

  /** Starts a new trace section with given name. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void beginSection(String sectionName) {
    if (tags == 0) {
      return;
    }
    if (!checkValidSectionName(sectionName)) {
      return;
    }
    currentSections.get().addFirst(sectionName);
  }

  /** Ends the most recent active trace section. */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void endSection() {
    if (tags == 0) {
      return;
    }
    if (currentSections.get().isEmpty()) {
      Log.e(TAG, "Trying to end a trace section that was never started");
      return;
    }
    previousSections.get().offer(currentSections.get().removeFirst());
  }

  /** Starts a new async trace section with given name. */
  @Implementation(minSdk = Q)
  protected static synchronized void beginAsyncSection(String sectionName, int cookie) {
    if (tags == 0) {
      return;
    }
    if (!checkValidSectionName(sectionName)) {
      return;
    }
    AsyncTraceSection newSection =
        AsyncTraceSection.newBuilder().setSectionName(sectionName).setCookie(cookie).build();
    if (currentAsyncSections.contains(newSection)) {
      if (crashOnIncorrectUsage) {
        throw new IllegalStateException("Section is already running");
      }
      Log.w(TAG, "Section is already running");
      return;
    }
    currentAsyncSections.add(newSection);
  }

  /** Ends async trace trace section. */
  @Implementation(minSdk = Q)
  protected static synchronized void endAsyncSection(String sectionName, int cookie) {
    if (tags == 0) {
      return;
    }
    AsyncTraceSection section =
        AsyncTraceSection.newBuilder().setSectionName(sectionName).setCookie(cookie).build();
    if (!currentAsyncSections.contains(section)) {
      Log.e(TAG, "Trying to end a trace section that was never started");
      return;
    }
    currentAsyncSections.remove(section);
    previousAsyncSections.add(section);
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static long nativeGetEnabledTags() {
    return tags;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected static void setAppTracingAllowed(boolean appTracingAllowed) {
    tags = appTracingAllowed ? TRACE_TAG_APP : 0;
  }

  /** Returns whether systrace is enabled. */
  @Implementation(minSdk = Q)
  protected static boolean isEnabled() {
    return isEnabled;
  }

  @Implementation(minSdk = Q)
  protected static void setCounter(String counterName, long counterValue) {
    verifyNotNull(counterName);
    counters.add(Counter.newBuilder().setName(counterName).setValue(counterValue).build());
  }

  /** Sets the systrace to enabled or disabled. */
  public static void setEnabled(boolean enabled) {
    ShadowTrace.isEnabled = enabled;
  }

  /** Returns a stack of the currently active trace sections for the current thread. */
  public static Deque<String> getCurrentSections() {
    return new ArrayDeque<>(currentSections.get());
  }

  /** Returns a queue of all the previously active trace sections for the current thread. */
  public static Queue<String> getPreviousSections() {
    return new ArrayDeque<>(previousSections.get());
  }

  /** Returns a set of all the current active async trace sections. */
  public static ImmutableSet<AsyncTraceSection> getCurrentAsyncSections() {
    return ImmutableSet.copyOf(currentAsyncSections);
  }

  /** Returns a set of all the previously active async trace sections. */
  public static ImmutableSet<AsyncTraceSection> getPreviousAsyncSections() {
    return ImmutableSet.copyOf(previousAsyncSections);
  }

  /** Returns an ordered list of previous counters. */
  public static ImmutableList<Counter> getCounters() {
    return ImmutableList.copyOf(counters);
  }

  /**
   * Do not use this method unless absolutely necessary. Prefer fixing the tests instead.
   *
   * <p>Sets whether to crash on incorrect usage (e.g., calling {@link #endSection()} before {@link
   * beginSection(String)}. Default value - {@code true}.
   */
  public static void doNotUseSetCrashOnIncorrectUsage(boolean crashOnIncorrectUsage) {
    ShadowTrace.crashOnIncorrectUsage = crashOnIncorrectUsage;
  }

  private static boolean checkValidSectionName(String sectionName) {
    if (sectionName == null) {
      if (crashOnIncorrectUsage) {
        throw new NullPointerException("sectionName cannot be null");
      }
      Log.w(TAG, "Section name cannot be null");
      return false;
    } else if (sectionName.length() > MAX_SECTION_NAME_LEN) {
      if (crashOnIncorrectUsage) {
        throw new IllegalArgumentException("sectionName is too long");
      }
      Log.w(TAG, "Section name is too long");
      return false;
    }
    return true;
  }

  /** Resets internal lists of active trace sections. */
  @Resetter
  public static void reset() {
    // TODO: clear sections from other threads
    currentSections.get().clear();
    previousSections.get().clear();
    currentAsyncSections.clear();
    previousAsyncSections.clear();
    counters.clear();
    ShadowTrace.isEnabled = true;
    crashOnIncorrectUsage = CRASH_ON_INCORRECT_USAGE_DEFAULT;
  }

  /** AutoValue representation of a trace triggered by one of the async apis */
  @AutoValue
  public abstract static class AsyncTraceSection {

    public abstract String getSectionName();

    public abstract Integer getCookie();

    public static Builder newBuilder() {
      return new AutoValue_ShadowTrace_AsyncTraceSection.Builder();
    }

    /** Builder for traces triggered by one of the async apis */
    @AutoValue.Builder()
    public abstract static class Builder {
      public abstract Builder setSectionName(String sectionName);

      public abstract Builder setCookie(Integer cookie);

      public abstract AsyncTraceSection build();
    }
  }

  /** Counters emitted with the setCounter API */
  @AutoValue
  public abstract static class Counter {

    public abstract String getName();

    public abstract long getValue();

    public static Builder newBuilder() {
      return new AutoValue_ShadowTrace_Counter.Builder();
    }

    /** Builder for counters emitted with the setCounter API */
    @AutoValue.Builder()
    public abstract static class Builder {

      public abstract Builder setName(String value);

      public abstract Builder setValue(long value);

      public abstract Counter build();
    }
  }
}
