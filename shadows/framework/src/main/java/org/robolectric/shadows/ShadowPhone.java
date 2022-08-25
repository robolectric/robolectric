package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import android.telecom.Phone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.telecom.Phone}. */
@Implements(value = Phone.class, isInAndroidSdk = false)
public class ShadowPhone {
  @RealObject private Phone phone;

  private final List<Call> calls = new ArrayList<>();

  @Implementation(minSdk = M)
  protected final List<Call> getCalls() {
    List<Call> unmodifiableCalls = reflector(ReflectorPhone.class, phone).getUnmodifiableCalls();
    if (unmodifiableCalls != null) {
      return unmodifiableCalls;
    }
    return Collections.unmodifiableList(calls);
  }

  @Implementation(minSdk = M)
  protected final CallAudioState getCallAudioState() {
    CallAudioState callAudioState = reflector(ReflectorPhone.class, phone).getCallAudioState();
    if (callAudioState != null) {
      return callAudioState;
    }
    InCallAdapter inCallAdapter = ReflectionHelpers.getField(phone, "mInCallAdapter");
    int audioRoute = ((ShadowInCallAdapter) Shadow.extract(inCallAdapter)).getAudioRoute();

    return new CallAudioState(
        /* muted= */ false,
        audioRoute,
        CallAudioState.ROUTE_SPEAKER | CallAudioState.ROUTE_EARPIECE);
  }

  /** Add Call to a collection that returns when getCalls is called. */
  public void addCall(Call call) {
    calls.add(call);
    List<Call> realCalls = reflector(ReflectorPhone.class, phone).getCalls();
    if (realCalls != null) {
      realCalls.add(call);
    }
  }

  /** Remove call that has previously been added via addCall(). */
  public void removeCall(Call call) {
    calls.remove(call);
    List<Call> realCalls = reflector(ReflectorPhone.class, phone).getCalls();
    if (realCalls != null) {
      realCalls.remove(call);
    }
  }

  @ForType(Phone.class)
  interface ReflectorPhone {
    @Accessor("mUnmodifiableCalls")
    List<Call> getUnmodifiableCalls();

    @Accessor("mCalls")
    List<Call> getCalls();

    @Accessor("mCallAudioState")
    CallAudioState getCallAudioState();
  }
}
