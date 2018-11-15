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
  public void endSection_calledBeforeBeginning_throwsIllegalStateException() throws Exception {
    try {
      Trace.endSection();
      fail("Must throw");
    } catch (IllegalStateException e) {
      // Must throw.
    }
  }

  @Test
  public void endSection_oneSectionButCalledTwice_throwsIllegalStateException() throws Exception {
    Trace.beginSection("section1");

    Trace.endSection();
    try {
      Trace.endSection();
      fail("Must throw");
    } catch (IllegalStateException e) {
      // Must throw.
    }
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
