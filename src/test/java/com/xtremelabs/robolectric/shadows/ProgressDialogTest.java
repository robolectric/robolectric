package com.xtremelabs.robolectric.shadows;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.ProgressDialog;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ProgressDialogTest {
	
	private ProgressDialog dialog;
	private ShadowProgressDialog shadow;
		
    @Before
    public void setUp() {
    	dialog = new ProgressDialog(null);
    	shadow = Robolectric.shadowOf(dialog);
    }
    
    @Test
    public void shouldSetMessage() {
    	CharSequence message = "This is only a test";
    	
    	assertThat(shadow.getMessage(), nullValue());
    	dialog.setMessage(message);
    	assertThat(shadow.getMessage(), equalTo(message));
   }
    
    @Test
    public void shouldSetIndeterminate() {
    	assertThat(dialog.isIndeterminate(), equalTo(false));
  	
    	dialog.setIndeterminate(true);
    	assertThat(dialog.isIndeterminate(), equalTo(true));
    	
    	dialog.setIndeterminate(false);
    	assertThat(dialog.isIndeterminate(), equalTo(false));
    }   
}
