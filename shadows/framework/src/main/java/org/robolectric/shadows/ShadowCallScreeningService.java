package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.CallScreeningService.CallResponse;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link android.telecom.CallScreeningService}. */
@Implements(CallScreeningService.class)
public final class ShadowCallScreeningService {
  /** Contains the parameters used to call {@link CallScreeningService#respondToCall}. */
  public static final class RespondToCallInput {
    private Call.Details callDetails;
    private CallResponse callResponse;

    public RespondToCallInput(Call.Details callDetails, CallResponse callResponse) {
      this.callDetails = callDetails;
      this.callResponse = callResponse;
    }

    public Call.Details getCallDetails() {
      return callDetails;
    }

    public CallResponse getCallResponse() {
      return callResponse;
    }
  }

  private Optional<RespondToCallInput> lastRespondToCallInput = Optional.empty();

  /** Shadows {@link CallScreeningService#respondToCall}. */
  @Implementation(minSdk = N)
  protected final void respondToCall(Call.Details callDetails, CallResponse response) {
    lastRespondToCallInput = Optional.of(new RespondToCallInput(callDetails, response));
  }

  /**
   * If {@link CallScreeningService} has called {@link #respondToCall}, returns the values of its
   * parameters. Returns an empty optional otherwise.
   */
  public Optional<RespondToCallInput> getLastRespondToCallInput() {
    return lastRespondToCallInput;
  }
}
