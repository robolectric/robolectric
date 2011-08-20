package com.xtremelabs.robolectric.shadows;

import android.content.Intent;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowIntentTest {
    @Test
    public void setUri_setsUri()throws Exception {
        Intent intent = new Intent();
        shadowOf(intent).setURI("http://foo");
        assertThat(intent.toURI(), is("http://foo"));
    }

    @Test
    public void putStringArrayListExtra_addsListToExtras() {
        Intent intent = new Intent();
        final List<String> strings = Arrays.asList("hi", "there");
        final ShadowIntent shadowIntent = shadowOf(intent);

        shadowIntent.putStringArrayListExtra("KEY", new ArrayList<String>(strings));

        final ArrayList<String> stringArrayList = Robolectric.shadowOf(shadowIntent.getExtras()).getStringArrayList("KEY");
        assertEquals(2, stringArrayList.size());
        for(String shadowIntentString : stringArrayList) {
            assertTrue(strings.contains(shadowIntentString));
        }
    }
}
