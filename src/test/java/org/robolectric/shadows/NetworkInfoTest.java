package org.robolectric.shadows;

import android.net.NetworkInfo;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class NetworkInfoTest {

    @Test
    public void getDetailedState_shouldReturnTheAssignedState() throws Exception {
        NetworkInfo networkInfo = Robolectric.newInstanceOf(NetworkInfo.class);
        shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
        assertThat(networkInfo.getDetailedState(), equalTo(NetworkInfo.DetailedState.SCANNING));
    }
}
