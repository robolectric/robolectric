package org.robolectric.versioning;

import static com.google.common.truth.Truth.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.versioning.AndroidVersions.AndroidRelease;
import org.robolectric.versioning.AndroidVersions.SdkInformation;

/** Test more esoteric versions mismatches in sdkInt numbers, and codenames. */
@RunWith(JUnit4.class)
public final class AndroidVersionsEdgeCaseTest {

  private void forceWarningMode(boolean warnMode) {
    try {
      Field field = AndroidVersions.class.getDeclaredField("warnOnly");
      field.setAccessible(true);
      field.set(null, warnMode);
    } catch (NoSuchFieldException | IllegalAccessException ex) {
      throw new RuntimeException("Could not update warnOnly field", ex);
    }
  }

  /**
   * sdkInt higher than any known release, claims it's released. Expects an error message to update
   * to update the AndroidVersions.class
   */
  @Test
  public void sdkIntHigherThanKnownReleasesClaimsIsReleased_throwsException() {
    AndroidRelease earliestUnrelease = null;
    try {
      forceWarningMode(false);
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
      forceWarningMode(false);
      SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
      latestRelease =
          information.sdkIntToAllReleases.get(information.latestRelease.getSdkInt() - 1);
      information.computeCurrentSdk(
          latestRelease.getSdkInt(),
          null,
          latestRelease.getShortCode(),
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

  @Test
  public void sdkIntReleasedButStillReportsCodeName_warningMode() {
    AndroidRelease latestRelease = null;
    try {
      forceWarningMode(true);
      SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
      latestRelease =
          information.sdkIntToAllReleases.get(information.latestRelease.getSdkInt() - 1);
      information.computeCurrentSdk(
          latestRelease.getSdkInt(),
          null,
          information.latestRelease.getShortCode(),
          Arrays.asList(latestRelease.getShortCode()));
    } catch (Throwable t) {
      assertThat(t).isNull();
    }
  }

  /**
   * sdkInt lower than known release, claims it's released. Expects an error message to update the
   * jar if release is older than the latest release, otherwise warn only.
   */
  @Test
  public void lastReleasedIntReleasedButStillReportsCodeName_noException() {
    forceWarningMode(false);
    SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
    AndroidRelease latestRelease =
        information.sdkIntToAllReleases.get(information.latestRelease.getSdkInt());
    information.computeCurrentSdk(
        latestRelease.getSdkInt(),
        null,
        information.latestRelease.getShortCode(),
        Arrays.asList(latestRelease.getShortCode()));
    assertThat(this).isNotNull();
  }

  @Test
  public void unknownSdkInt_warningMode() {
    try {
      forceWarningMode(true);
      SdkInformation information = AndroidVersions.gatherStaticSdkInformationFromThisClass();
      AndroidRelease found =
          information.computeCurrentSdk(
              35, "zzzz", "Z", Arrays.asList("wwww", "xxxx", "yyyy", "zzzz"));
      assertThat(found.getSdkInt()).isEqualTo(10000);
    } catch (Throwable t) {
      assertThat(t).isNull();
    }
  }
}
