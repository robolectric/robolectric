package com.xtremelabs.robolectric.shadows;

import android.app.Dialog;
import android.content.DialogInterface;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class DialogTest {
	
    @Test
    public void shouldCallOnDismissListener() throws Exception {
        final Transcript transcript = new Transcript();

        final Dialog dialog = new Dialog(null);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInListener) {
                assertThat((Dialog) dialogInListener, sameInstance(dialog));
                transcript.add("onDismiss called!");
            }
        });

        dialog.dismiss();

        transcript.assertEventsSoFar("onDismiss called!");
    }
    
    @Test
    public void shouldSetCancelable() {
    	Dialog dialog = new Dialog(null);
    	ShadowDialog shadow = Robolectric.shadowOf(dialog);
    	
    	assertThat(shadow.isCancelable(), equalTo(false));
    	
    	dialog.setCancelable(true);
    	assertThat(shadow.isCancelable(), equalTo(true));
    	
    	dialog.setCancelable(false);
    	assertThat(shadow.isCancelable(), equalTo(false));
   }
}
