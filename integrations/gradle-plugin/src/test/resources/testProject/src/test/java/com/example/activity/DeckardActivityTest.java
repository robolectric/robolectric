package com.example.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.widget.TextView;
import com.example.BuildConfig;
import com.example.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class DeckardActivityTest {

    @Test
    public void testAppComesUpAndResourcesWork() throws Exception {
        Activity activity = Robolectric.setupActivity(DeckardActivity.class);
        assertTrue(activity != null);

        assertEquals("Hiya!", ((TextView) activity.findViewById(R.id.text)).getText());
        throw new EverythingWorkedException();
    }

    class EverythingWorkedException extends Exception {
        public EverythingWorkedException() {
        }
    }
}
