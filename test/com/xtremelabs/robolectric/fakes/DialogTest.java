package com.xtremelabs.robolectric.fakes;

import android.app.Dialog;
import android.content.DialogInterface;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.util.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(DogfoodRobolectricTestRunner.class)
public class DialogTest {
    @Test
    public void shouldCallOnDismissListener() throws Exception {
        DogfoodRobolectricTestRunner.addProxy(Dialog.class, FakeDialog.class);

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
}
