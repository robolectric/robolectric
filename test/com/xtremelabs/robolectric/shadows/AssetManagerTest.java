package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Application;
import android.content.res.AssetManager;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class AssetManagerTest {
	Application app;

	@Before
	public void setUp() throws Exception {
		Robolectric.bindDefaultShadowClasses();
		Robolectric.application = new Application();
		app = Robolectric.application;
	}
	
	@Test
	public void assertGetAssetsNotNull(){
		AssetManager assetManager;
		
		assetManager = new AssetManager();
		assertNotNull(assetManager);
		
		assetManager = new Activity().getAssets();
		assertNotNull(assetManager);
		
		assetManager = app.getAssets();
		assertNotNull(assetManager);

		assetManager = app.getResources().getAssets();
		assertNotNull(assetManager);
		
		System.out.println(assetManager.toString());
	}
	
	@Test
	public void ensureShadowIsWorking(){

	}
	
	
}
