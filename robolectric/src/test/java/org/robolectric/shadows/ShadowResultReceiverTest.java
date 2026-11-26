package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowResultReceiverTest {
  @Test
  public void callingSend_shouldCallOverriddenOnReceiveResultWithTheSameArguments() {
    TestResultReceiver testResultReceiver = new TestResultReceiver(null);
    Bundle bundle = new Bundle();

    testResultReceiver.send(5, bundle);
    assertEquals(5, testResultReceiver.resultCode);
    assertEquals(bundle, testResultReceiver.resultData);
  }

  @Test
  public void callingSend_onParcelledCopy_shouldCallOriginalReceiver() {
    TestResultReceiver testResultReceiver = new TestResultReceiver(null);
    Bundle bundle = new Bundle();
    bundle.putString("key", "value");

    Parcel parcel = Parcel.obtain();
    try {
      testResultReceiver.writeToParcel(parcel, 0);
      parcel.setDataPosition(0);
      ResultReceiver copy = ResultReceiver.CREATOR.createFromParcel(parcel);
      assertNotSame(testResultReceiver, copy);

      copy.send(7, bundle);
    } finally {
      parcel.recycle();
    }

    assertEquals(7, testResultReceiver.resultCode);
    assertEquals("value", testResultReceiver.resultData.getString("key"));
  }

  static class TestResultReceiver extends ResultReceiver {
    int resultCode;
    Bundle resultData;

    public TestResultReceiver(Handler handler) {
      super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
      this.resultCode = resultCode;
      this.resultData = resultData;
    }
  }
}
