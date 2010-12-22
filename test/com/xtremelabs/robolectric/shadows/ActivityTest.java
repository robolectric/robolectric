package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.appwidget.AppWidgetProvider;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.xtremelabs.robolectric.ApplicationResolver;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class ActivityTest {

    @Test(expected = IllegalStateException.class)
    public void shouldComplainIfActivityIsDestroyedWithRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();
        activity.registerReceiver(new AppWidgetProvider(), new IntentFilter());
        activity.onDestroy();
    }

    @Test
    public void shouldNotComplainIfActivityIsDestroyedWhileAnotherActivityHasRegisteredBroadcastReceivers() throws Exception {
        MyActivity activity = new MyActivity();

        MyActivity activity2 = new MyActivity();
        activity2.registerReceiver(new AppWidgetProvider(), new IntentFilter());

        activity.onDestroy(); // should not throw exception
    }

    @Test
    public void shouldRetrievePackageNameFromTheManifest() throws Exception {
        Robolectric.application = new ApplicationResolver("test" + File.separator + "TestAndroidManifestWithPackageName.xml").resolveApplication();
        assertEquals("com.wacka.wa", new Activity().getPackageName());
    }

    @Test
    public void onContentChangedTest() throws RuntimeException{
    	CustomActivity customActivity = new CustomActivity();
    	customActivity.onCreate(null); // should not throw exception
    }

    private static class MyActivity extends Activity {
        @Override protected void onDestroy() {
            super.onDestroy();
        }
    }
    
    private static class CustomActivity extends Activity{
    	private static final int CUSTOM_ID = 1032421210;
    	
    	@Override
    	public void onCreate(Bundle icicle){
    		super.onCreate(icicle);
    		View customView = new View(this);
    		customView.setId(CUSTOM_ID);
    		setContentView(customView);
    	}
    	
    	@Override
    	public void onContentChanged(){
    		View theView = findViewById(CUSTOM_ID);
    		if( theView == null ){
    			throw new RuntimeException("Your content must have a HeaderView whose id attribute is 1032421210");
    		}
    	}
    }
}
