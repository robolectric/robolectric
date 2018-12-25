package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Trace;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
}
