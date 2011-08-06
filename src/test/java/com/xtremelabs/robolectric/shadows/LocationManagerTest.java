package com.xtremelabs.robolectric.shadows;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.location.LocationManager;
import android.location.GpsStatus.Listener;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@RunWith(WithTestDefaultsRunner.class)
public class LocationManagerTest {
	private LocationManager locationManager;
	
	@Before
	public void setUp() {
		locationManager = (LocationManager) Robolectric.application.getSystemService(Context.LOCATION_SERVICE);
	}
	
	@Test
	public void shouldReturnProviderEnabledAsDefault() {
		Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Assert.assertTrue(enabled);
	}
	
	@Test
	public void shouldDisableProvider() {
		ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
		shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false);
		
		Boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Assert.assertFalse(enabled);
	}
	
	@Test
	public void shouldHaveListenerOnceAdded() {
		Listener listener = addListenerToLocationManager();
		
		ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
		
		Assert.assertTrue(shadowLocationManager.hasListener(listener));
	}
	
	@Test
	public void shouldNotHaveListenerOnceRemoved() {
		Listener listener = addListenerToLocationManager();
		
		ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
		
		locationManager.removeGpsStatusListener(listener);
		
		Assert.assertFalse(shadowLocationManager.hasListener(listener));
	}
	
	private Listener addListenerToLocationManager() {
		Listener listener = new TestListener();
		locationManager.addGpsStatusListener(listener);
		return listener;
	}
	
	private class TestListener implements Listener {

		@Override
		public void onGpsStatusChanged(int event) {
			
		}
		
	}
}
