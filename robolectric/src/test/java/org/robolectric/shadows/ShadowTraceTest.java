package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Trace;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ShadowTrace}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowTraceTest {
  private static final String VERY_LONG_TAG_NAME = String.format(String.format("%%%ds", 128), "A");

  @Test
  public void beginSection_calledOnce_addsSection() throws Exception {
    Trace.beginSection("section1");

    assertThat(ShadowTrace.getCurrentSections()).containsExactly("section1");
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();
  }

  @Test
  public void beginSection_calledTwice_addsBothSections() throws Exception {
    Trace.beginSection("section1");
    Trace.beginSection("section2");

    assertThat(ShadowTrace.getCurrentSections()).containsExactly("section1", "section2");
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();
  }

  @Test
  public void beginSection_tagIsNull_throwsNullPointerException() throws Exception {
    try {
      Trace.beginSection(null);
      fail("Must throw");
    } catch (NullPointerException e) {
      // Must throw.
    }
  }

  @Test
  public void beginSection_tagIsNullAndCrashDisabled_doesNotThrow() throws Exception {
    ShadowTrace.doNotUseSetCrashOnIncorrectUsage(false);
    Trace.beginSection(null);
    // Should not crash.
  }

  @Test
  public void beginSection_tagIsTooLong_throwsIllegalArgumentException() throws Exception {
    try {
      Trace.beginSection(VERY_LONG_TAG_NAME);
      fail("Must throw");
    } catch (IllegalArgumentException e) {
      // Must throw.
    }
  }

  @Test
  public void beginSection_tagIsTooLongAndCrashDisabled_doesNotThrow() throws Exception {
    ShadowTrace.doNotUseSetCrashOnIncorrectUsage(false);
    Trace.beginSection(VERY_LONG_TAG_NAME);
    // Should not crash.
  }

  @Test
  public void endSection_oneSection_closesSection() throws Exception {
    Trace.beginSection("section1");

    Trace.endSection();

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).containsExactly("section1");
  }

  @Test
  public void endSection_twoSections_closesLastSection() throws Exception {
    Trace.beginSection("section1");
    Trace.beginSection("section2");

    Trace.endSection();

    assertThat(ShadowTrace.getCurrentSections()).containsExactly("section1");
    assertThat(ShadowTrace.getPreviousSections()).containsExactly("section2");
  }

  @Test
  public void endSection_twoRecursiveSectionsAndCalledTwice_closesAllSections() throws Exception {
    Trace.beginSection("section1");
    Trace.beginSection("section2");

    Trace.endSection();
    Trace.endSection();

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).containsExactly("section2", "section1");
  }

  @Test
  public void endSection_twoSequentialSections_closesAllSections() throws Exception {
    Trace.beginSection("section1");
    Trace.endSection();
    Trace.beginSection("section2");
    Trace.endSection();

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).containsExactly("section1", "section2");
  }

  @Test
  public void endSection_calledBeforeBeginning_doesNotThrow() throws Exception {
    Trace.endSection();
    // Should not crash.
  }

  @Test
  public void endSection_oneSectionButCalledTwice_doesNotThrow() throws Exception {
    Trace.beginSection("section1");

    Trace.endSection();
    Trace.endSection();
    // Should not crash.
  }

  @Test
  public void reset_resetsInternalState() throws Exception {
    Trace.beginSection("section1");
    Trace.endSection();
    Trace.beginSection("section2");

    ShadowTrace.reset();

    assertThat(ShadowTrace.getCurrentSections()).isEmpty();
    assertThat(ShadowTrace.getPreviousSections()).isEmpty();
  }

  @Test
  public void toggleEnabledTest() throws Exception {
    Trace.beginSection("section1");
    assertThat(ShadowTrace.isEnabled()).isTrue();
    ShadowTrace.setEnabled(false);
    assertThat(ShadowTrace.isEnabled()).isFalse();
    ShadowTrace.setEnabled(true);
    assertThat(ShadowTrace.isEnabled()).isTrue();
    Trace.endSection();

  }

  @Test
  public void traceFromIndependentThreads() throws ExecutionException, InterruptedException {
    ShadowTrace.doNotUseSetCrashOnIncorrectUsage(true);
    ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    try {
      Trace.beginSection("main_looper_trace");
      Future<?> f = backgroundExecutor.submit(() -> Trace.beginSection("bg_trace"));
      f.get();
      Trace.endSection();

      assertThat(ShadowTrace.getPreviousSections()).containsExactly("main_looper_trace");
      assertThat(ShadowTrace.getCurrentSections()).isEmpty();

      f =
          backgroundExecutor.submit(
              new Runnable() {
                @Override
                public void run() {
                  assertThat(ShadowTrace.getCurrentSections()).containsExactly("bg_trace");
                  assertThat(ShadowTrace.getPreviousSections()).isEmpty();

                  Trace.endSection();

                  assertThat(ShadowTrace.getPreviousSections()).containsExactly("bg_trace");
                  assertThat(ShadowTrace.getCurrentSections()).isEmpty();
                }
              });
      f.get();
    } finally {
      backgroundExecutor.shutdown();
    }
  }
}
