package com.xtremelabs.robolectric.shadows;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ResultReceiverTest {
    @Test
    public void callingSend_shouldCallOverridenOnReceiveResultWithTheSameArguments() throws Exception {
        TestResultReceiver testResultReceiver = new TestResultReceiver(null);
        Bundle bundle = new Bundle();

        testResultReceiver.send(5, bundle);
        assertEquals(5, testResultReceiver.resultCode);
        assertEquals(bundle, testResultReceiver.resultData);
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
