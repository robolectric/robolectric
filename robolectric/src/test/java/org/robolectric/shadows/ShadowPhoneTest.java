package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;

import android.telecom.Call;
import android.telecom.Phone;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Robolectric test for {@link ShadowPhone}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowPhoneTest {

  @Test
  public void getZeroCall_noCall() {
    Call[] calls = new Call[] {};
    testAddCallGetCall(calls);
  }

  @Test
  public void getOneCall_hasOneCall() {
    Call call = Shadow.newInstanceOf(Call.class);
    Call[] calls = new Call[] {call};
    testAddCallGetCall(calls);
  }

  @Test
  public void getTwoCall_hasTwoCall() {
    Call call1 = Shadow.newInstanceOf(Call.class);
    Call call2 = Shadow.newInstanceOf(Call.class);
    Call[] calls = new Call[] {call1, call2};
    testAddCallGetCall(calls);
  }

  @Test
  public void addAndRemoveCalls() {
    Call call1 = Shadow.newInstanceOf(Call.class);
    Call call2 = Shadow.newInstanceOf(Call.class);
    Phone phone = Shadow.newInstanceOf(Phone.class);
    ShadowPhone shadowPhone = Shadow.extract(phone);

    shadowPhone.addCall(call1);
    shadowPhone.addCall(call2);
    shadowPhone.removeCall(call1);

    assertThat(phone.getCalls()).containsExactly(call2);

    shadowPhone.removeCall(call2);

    assertThat(phone.getCalls()).isEmpty();
  }

  public static void testAddCallGetCall(Call[] calls) {
    Phone phone = Shadow.newInstanceOf(Phone.class);
    ShadowPhone shadowPhone = Shadow.extract(phone);

    for (Call call : calls) {
      shadowPhone.addCall(call);
    }

    List<Call> callList = phone.getCalls();

    for (int i = 0; i < calls.length; i++) {
      assertThat(callList.get(i)).isEqualTo(calls[i]);
    }
  }
}
