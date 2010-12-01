package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ConnectivityManagerTest {

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
        Robolectric.application = new Application();
    }

    @Test
    public void getConnectivityManagerShouldNotBeNull() {
    	ConnectivityManager cm = (ConnectivityManager) Robolectric.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    	assertNotNull(cm);
    	assertNotNull(cm.getActiveNetworkInfo());
    }

    @Test
    public void networkInfoShouldReturnTrueCorrectly() {
    	ConnectivityManager cm = (ConnectivityManager) Robolectric.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    	ShadowNetworkInfo ni = Robolectric.shadowOf(cm.getActiveNetworkInfo());
    	ni.setConnectionStatus(true);
    	
    	assertTrue(cm.getActiveNetworkInfo().isConnectedOrConnecting());
    }

    @Test
    public void networkInfoShouldReturnFalseCorrectly() {
    	ConnectivityManager cm = (ConnectivityManager) Robolectric.application.getSystemService(Context.CONNECTIVITY_SERVICE);
    	ShadowNetworkInfo ni = Robolectric.shadowOf(cm.getActiveNetworkInfo());
    	ni.setConnectionStatus(false);
    	
    	assertFalse(cm.getActiveNetworkInfo().isConnectedOrConnecting());
    }
}
