package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.net.ConnectivityManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ConnectivityManagerTest {
    private ConnectivityManager connectivityManager;
    private ShadowNetworkInfo networkInfo;

    @Before
    public void setUp() throws Exception {
        connectivityManager = (ConnectivityManager) Robolectric.application.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = Robolectric.shadowOf(connectivityManager.getActiveNetworkInfo());
    }

    @Test
    public void getConnectivityManagerShouldNotBeNull() {
        assertNotNull(connectivityManager);
        assertNotNull(connectivityManager.getActiveNetworkInfo());
    }

    @Test
    public void networkInfoShouldReturnTrueCorrectly() {
        networkInfo.setConnectionStatus(true);

        assertTrue(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
        assertTrue(connectivityManager.getActiveNetworkInfo().isConnected());
    }

    @Test
    public void networkInfoShouldReturnFalseCorrectly() {
        networkInfo.setConnectionStatus(false);

        assertFalse(connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting());
        assertFalse(connectivityManager.getActiveNetworkInfo().isConnected());
    }
    
    @Test
    public void networkInfoShouldReturnTypeCorrectly(){
    	networkInfo.setConnectionType(ConnectivityManager.TYPE_MOBILE);
    	assertEquals(ConnectivityManager.TYPE_MOBILE, networkInfo.getType());
    	
    	networkInfo.setConnectionType(ConnectivityManager.TYPE_WIFI);
    	assertEquals(ConnectivityManager.TYPE_WIFI, networkInfo.getType());
    }
}
