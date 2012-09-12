package com.xtremelabs.robolectric.shadows;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;


@Implements(SensorManager.class)
public class ShadowSensorManager {

	private ArrayList<SensorEventListener> listeners = new ArrayList<SensorEventListener>();
	
	public boolean forceListenersToFail = false;
	
	@Implementation
	public boolean registerListener(SensorEventListener listener, Sensor sensor, int rate) {
		
		if(forceListenersToFail)
			return false;
		
		if(!listeners.contains(listener))
			listeners.add(listener);
		
		return true;
	}
	
	@Implementation
	public void unregisterListener(SensorEventListener listener, Sensor sensor) {
		listeners.remove(listener);
	}
	
	public boolean hasListener(SensorEventListener listener) {
		return listeners.contains(listener);
	}
	
	public SensorEvent createSensorEvent() {
		return Robolectric.newInstanceOf(SensorEvent.class);
	}
}
