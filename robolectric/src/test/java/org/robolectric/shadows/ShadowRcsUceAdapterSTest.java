package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telephony.ims.ImsManager;
import android.telephony.ims.RcsContactUceCapability;
import android.telephony.ims.RcsUceAdapter;
import android.telephony.ims.RcsUceAdapter.CapabilitiesCallback;
import androidx.test.core.app.ApplicationProvider;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowRcsUceAdapter.CapabilityFailureInfo;

/**
 * Unit tests for {@link ShadowRcsUceAdapter} on S. Split out from ShadowRcsUceAdapterTest since the
 * callbacks as written are S-only
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class ShadowRcsUceAdapterSTest {

  private static final int SUBSCRIPTION_ID = 0;
  private static final Uri URI = Uri.parse("tel:+1-900-555-0191");
  private static final Uri OTHER_URI = Uri.parse("tel:+1-900-555-0192");
  private static final int ERROR_CODE = 1234;
  private static final long RETRY_MILLIS = 5678L;
  private static final String FEATURE_TAG = "I'm a feature tag! (:";

  private RcsUceAdapter rcsUceAdapter;
  private ExecutorService executorService;

  @Before
  public void setUp() {
    rcsUceAdapter =
        ((ImsManager)
                ApplicationProvider.getApplicationContext()
                    .getSystemService(Context.TELEPHONY_IMS_SERVICE))
            .getImsRcsManager(SUBSCRIPTION_ID)
            .getUceAdapter();
    executorService = Executors.newSingleThreadExecutor();
    ShadowRcsUceAdapter.reset();
  }

  @Test
  public void setCapabilitiesForUri_requestCapabilities_overridesCapabilitiesForUri()
      throws Exception {
    RcsContactUceCapability capability =
        new RcsContactUceCapability.OptionsBuilder(URI).addFeatureTag(FEATURE_TAG).build();
    ShadowRcsUceAdapter.setCapabilitiesForUri(URI, capability);
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(capability));

    rcsUceAdapter.requestCapabilities(ImmutableList.of(URI), executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  @Test
  public void setCapabilitiesForUri_requestCapabilities_doesNotOverrideCapabilitiesForOtherUri()
      throws Exception {
    RcsContactUceCapability capability =
        new RcsContactUceCapability.OptionsBuilder(URI).addFeatureTag(FEATURE_TAG).build();
    RcsContactUceCapability otherEmptyCapability =
        new RcsContactUceCapability.OptionsBuilder(OTHER_URI).build();
    ShadowRcsUceAdapter.setCapabilitiesForUri(URI, capability);
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(otherEmptyCapability));

    rcsUceAdapter.requestCapabilities(
        ImmutableList.of(OTHER_URI), executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  @Test
  public void setCapabilitiesFailureForUri_requestCapabilities_failsForUri() throws Exception {
    CapabilityFailureInfo failureInfo = CapabilityFailureInfo.create(ERROR_CODE, RETRY_MILLIS);
    ShadowRcsUceAdapter.setCapabilitiesFailureForUri(URI, failureInfo);
    ErrorVerifierCallback verifierCallback = new ErrorVerifierCallback(failureInfo);

    rcsUceAdapter.requestCapabilities(ImmutableList.of(URI), executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertOnErrorCalled();
  }

  @Test
  public void setCapabilitiesFailureForUri_requestCapabilities_doesNotFailForOtherUri()
      throws Exception {
    CapabilityFailureInfo failureInfo = CapabilityFailureInfo.create(ERROR_CODE, RETRY_MILLIS);
    ShadowRcsUceAdapter.setCapabilitiesFailureForUri(URI, failureInfo);
    RcsContactUceCapability otherEmptyCapability =
        new RcsContactUceCapability.OptionsBuilder(OTHER_URI).build();
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(otherEmptyCapability));

    rcsUceAdapter.requestCapabilities(
        ImmutableList.of(OTHER_URI), executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  @Test
  public void setCapabilitiesForUri_requestAvailability_overridesCapabilitiesForUri()
      throws Exception {
    RcsContactUceCapability capability =
        new RcsContactUceCapability.OptionsBuilder(URI).addFeatureTag(FEATURE_TAG).build();
    ShadowRcsUceAdapter.setCapabilitiesForUri(URI, capability);
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(capability));

    rcsUceAdapter.requestAvailability(URI, executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  @Test
  public void setCapabilitiesForUri_requestAvailability_doesNotOverrideCapabilitiesForOtherUri()
      throws Exception {
    RcsContactUceCapability capability =
        new RcsContactUceCapability.OptionsBuilder(URI).addFeatureTag(FEATURE_TAG).build();
    RcsContactUceCapability otherEmptyCapability =
        new RcsContactUceCapability.OptionsBuilder(OTHER_URI).build();
    ShadowRcsUceAdapter.setCapabilitiesForUri(URI, capability);
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(otherEmptyCapability));

    rcsUceAdapter.requestAvailability(OTHER_URI, executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  @Test
  public void setCapabilitiesFailureForUri_requestAvailability_failsForUri() throws Exception {
    CapabilityFailureInfo failureInfo = CapabilityFailureInfo.create(ERROR_CODE, RETRY_MILLIS);
    ShadowRcsUceAdapter.setCapabilitiesFailureForUri(URI, failureInfo);
    ErrorVerifierCallback verifierCallback = new ErrorVerifierCallback(failureInfo);

    rcsUceAdapter.requestAvailability(URI, executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertOnErrorCalled();
  }

  @Test
  public void setCapabilitiesFailureForUri_requestAvailability_doesNotFailForOtherUri()
      throws Exception {
    CapabilityFailureInfo failureInfo = CapabilityFailureInfo.create(ERROR_CODE, RETRY_MILLIS);
    ShadowRcsUceAdapter.setCapabilitiesFailureForUri(URI, failureInfo);
    RcsContactUceCapability otherEmptyCapability =
        new RcsContactUceCapability.OptionsBuilder(OTHER_URI).build();
    SuccessfulCapabilityVerifierCallback verifierCallback =
        new SuccessfulCapabilityVerifierCallback(ImmutableList.of(otherEmptyCapability));

    rcsUceAdapter.requestAvailability(OTHER_URI, executorService, verifierCallback);
    executorService.shutdown();
    executorService.awaitTermination(10, SECONDS);

    verifierCallback.assertExchangeSuccessfullyCompleted();
  }

  private static class SuccessfulCapabilityVerifierCallback implements CapabilitiesCallback {
    private final List<RcsContactUceCapability> expectedCapabilities;
    private int currentIndex = 0;
    private boolean onCompleteCalled = false;

    private SuccessfulCapabilityVerifierCallback(
        List<RcsContactUceCapability> expectedCapabilities) {
      this.expectedCapabilities = expectedCapabilities;
    }

    @Override
    public void onCapabilitiesReceived(List<RcsContactUceCapability> contactCapabilities) {
      if (onCompleteCalled) {
        Assert.fail();
      }
      for (RcsContactUceCapability capability : contactCapabilities) {
        assertThat(capability.getFeatureTags())
            .isEqualTo(expectedCapabilities.get(currentIndex).getFeatureTags());
        currentIndex++;
      }
    }

    @Override
    public void onComplete() {
      onCompleteCalled = true;
    }

    @Override
    public void onError(int i, long l) {
      Assert.fail();
    }

    private void assertExchangeSuccessfullyCompleted() {
      assertThat(currentIndex).isEqualTo(expectedCapabilities.size());
      assertThat(onCompleteCalled).isTrue();
    }
  }

  private static class ErrorVerifierCallback implements CapabilitiesCallback {
    private final CapabilityFailureInfo failureInfo;
    private boolean onErrorCalled = false;

    private ErrorVerifierCallback(CapabilityFailureInfo failureInfo) {
      this.failureInfo = failureInfo;
    }

    @Override
    public void onCapabilitiesReceived(List<RcsContactUceCapability> contactCapabilities) {
      Assert.fail();
    }

    @Override
    public void onComplete() {
      Assert.fail();
    }

    @Override
    public void onError(int errorCode, long retryMillis) {
      assertThat(errorCode).isEqualTo(failureInfo.errorCode());
      assertThat(retryMillis).isEqualTo(failureInfo.retryMillis());
      onErrorCalled = true;
    }

    private void assertOnErrorCalled() {
      assertThat(onErrorCalled).isTrue();
    }
  }
}
