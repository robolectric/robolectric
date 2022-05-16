package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.CallScreeningService.CallResponse;
import com.android.internal.telecom.ICallScreeningAdapter;
import java.util.Optional;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

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

  @RealObject private CallScreeningService realObject;

  private Optional<RespondToCallInput> lastRespondToCallInput = Optional.empty();

  /** Shadows {@link CallScreeningService#respondToCall}. */
  @Implementation(minSdk = N)
  protected final void respondToCall(Call.Details callDetails, CallResponse response) {
    lastRespondToCallInput = Optional.of(new RespondToCallInput(callDetails, response));

    if (shouldForwardResponseToRealObject()) {
      reflector(CallScreeningServiceReflector.class, realObject)
          .respondToCall(callDetails, response);
    }
  }

  /**
   * The real {@link CallScreeningService} class forwards the response to an {@code
   * ICallScreeningAdapter}, which sends it across via IPC to the Telecom system service. In
   * Robolectric, when interacting with a {@link CallScreeningService} via {@link
   * org.robolectric.android.controller.ServiceController} as in
   *
   * <pre>{@code
   * ServiceController<? extends CallScreeningService> serviceController =
   *     Robolectric.buildService(MyCallScreeningServiceImpl.class, intent).create().bind();
   * serviceController.onScreenCall(callDetails);
   * }</pre>
   *
   * then no {@code ICallScreeningAdapter} is present and the response must not be forwarded to the
   * real object to avoid a NullPointerException.
   *
   * <p>Test code interacting with {@link CallScreeningService} may set up an {@code
   * ICallScreeningAdapter} by doing the following:
   *
   * <pre>{@code
   * ServiceController<? extends CallScreeningService> serviceController =
   *     Robolectric.buildService(MyCallScreeningServiceImpl.class, intent).create();
   * ICallScreeningService.Stub binder = serviceController.get().onBind(intent);
   * binder.screenCall(callScreeningAdapter, parcelableCall);
   * }</pre>
   *
   * When this second approach is used, ShadowCallScreeningService will find that the {@code
   * ICallScreeningAdapter} instance is present and forward the response to it.
   */
  private boolean shouldForwardResponseToRealObject() {
    return reflector(CallScreeningServiceReflector.class, realObject).getCallScreeningAdapter()
        != null;
  }

  /**
   * If {@link CallScreeningService} has called {@link #respondToCall}, returns the values of its
   * parameters. Returns an empty optional otherwise.
   */
  public Optional<RespondToCallInput> getLastRespondToCallInput() {
    return lastRespondToCallInput;
  }

  /** Reflector interface for {@link CallScreeningService}'s internals. */
  @ForType(CallScreeningService.class)
  interface CallScreeningServiceReflector {

    @Accessor("mCallScreeningAdapter")
    ICallScreeningAdapter getCallScreeningAdapter();

    @Direct
    void respondToCall(Call.Details callDetails, CallResponse response);
  }
}
