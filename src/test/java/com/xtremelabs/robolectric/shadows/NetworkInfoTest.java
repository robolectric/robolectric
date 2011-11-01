package com.xtremelabs.robolectric.shadows;

import android.net.NetworkInfo;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class NetworkInfoTest {

    @Test
    public void getDetailedState_shouldReturnTheAssignedState() throws Exception {
        NetworkInfo networkInfo = Robolectric.newInstanceOf(NetworkInfo.class);
        shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
        assertThat(networkInfo.getDetailedState(), equalTo(NetworkInfo.DetailedState.SCANNING));
    }
}
