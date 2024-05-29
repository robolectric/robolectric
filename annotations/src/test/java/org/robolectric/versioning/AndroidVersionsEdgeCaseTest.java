package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;
import org.robolectric.versioning.AndroidVersions.SdkInformation;

/** Test more esoteric versions mismatches in sdkInt numbers, and codenames. */
@RunWith(JUnit4.class)
public final class AndroidVersionsEdgeCaseTest {

  /**
   * sdkInt higher than any known release, claims it's released. Expects an error message to update
   * to update the AndroidVersions.class
   */
  @Test
  public void sdkIntHigherThanKnownReleasesClaimsIsReleased_throwsException() {
    AndroidRelease earliestUnrelease = null;
    try {
      SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
      earliestUnrelease = information.earliestUnreleased;
      information.computeCurrentSdk(
          earliestUnrelease.getSdkInt(), earliestUnrelease.getVersion(), "REL", Arrays.asList());
      assertThat(this).isNull();
    } catch (RuntimeException e) {
      assertThat(e)
          .hasMessageThat()
          .contains(
              "The current sdk "
                  + earliestUnrelease.getShortCode()
                  + " has been released. Please update the contents of "
                  + AndroidVersions.class.getName()
                  + " to mark sdk "
                  + earliestUnrelease.getShortCode()
                  + " as released.");
      assertThat(e).isInstanceOf(RuntimeException.class);
    }
  }

  /**
   * sdkInt lower than known release, claims it's released. Expects an error message to update the
   * jar.
   */
  @Test
  public void sdkIntReleasedButStillReportsCodeName_throwsException() {
    AndroidRelease latestRelease = null;
    try {
      SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
      latestRelease = information.latestRelease;
      information.computeCurrentSdk(
          latestRelease.getSdkInt(),
          null,
          information.latestRelease.getShortCode(),
          Arrays.asList(latestRelease.getShortCode()));
      assertThat(this).isNull();
    } catch (RuntimeException e) {
      assertThat(e)
          .hasMessageThat()
          .contains(
              "The current sdk "
                  + latestRelease.getShortCode()
                  + " has been been marked as released. Please update the contents of current sdk"
                  + " jar to the released version.");
      assertThat(e).isInstanceOf(RuntimeException.class);
    }
  }
}
