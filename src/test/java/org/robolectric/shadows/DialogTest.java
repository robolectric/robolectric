package org.robolectric.shadows;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.robolectric.util.TestUtil.assertInstanceOf;
import static junit.framework.Assert.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
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
    public void setContentViewWithViewAllowsFindById() throws Exception {
        final int viewId = 1234;
        Activity context = new Activity();
        final Dialog dialog = new Dialog(context);
        final View view = new View(context);
        view.setId(viewId);
        dialog.setContentView(view);

        assertSame(view, dialog.findViewById(viewId));
    }

    @Test
    public void shouldGetLayoutInflater() {
        Dialog dialog = new Dialog(Robolectric.application);
        assertNotNull(dialog.getLayoutInflater());
    }

    @Test
    public void shouldCallOnStartFromShow() {
        TestDialog dialog = new TestDialog();
        dialog.show();

        assertTrue(dialog.onStartCalled);
    }

    @Test
    public void shouldSetCancelable() {
        Dialog dialog = new Dialog(null);
        ShadowDialog shadow = Robolectric.shadowOf(dialog);

        dialog.setCancelable(false);
        assertThat(shadow.isCancelable(), equalTo(false));
    }

    @Test
    public void shouldDismissTheRealDialogWhenCancelled() throws Exception {
        TestDialog dialog = new TestDialog();
        dialog.cancel();
        assertThat(dialog.wasDismissed, equalTo(true));
    }

    @Test
    public void shouldDefaultCancelableToTrueAsTheSDKDoes() throws Exception {
        Dialog dialog = new Dialog(null);
        ShadowDialog shadow = Robolectric.shadowOf(dialog);

        assertThat(shadow.isCancelable(), equalTo(true));
    }

    @Test
    public void shouldOnlyCallOnCreateOnce() {
        final Transcript transcript = new Transcript();

        Dialog dialog = new Dialog(Robolectric.application) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                transcript.add("onCreate called");
            }
        };

        dialog.show();
        transcript.assertEventsSoFar("onCreate called");

        dialog.dismiss();
        dialog.show();
        transcript.assertNoEventsSoFar();
    }

    @Test
    public void show_setsLatestDialog() {
        Dialog dialog = new Dialog(Robolectric.application);
        assertNull(ShadowDialog.getLatestDialog());
        
        dialog.show();

        assertSame(dialog, ShadowDialog.getLatestDialog());
        assertNull(ShadowAlertDialog.getLatestAlertDialog());
    }

    @Test
    public void getLatestDialog_shouldReturnARealDialog() throws Exception {
        assertThat(ShadowDialog.getLatestDialog(), nullValue());

        Dialog dialog = new Dialog(null);
        dialog.show();
        assertThat(ShadowDialog.getLatestDialog(), sameInstance(dialog));
    }

    @Test
    public void shouldKeepListOfOpenedDialogs() throws Exception {
        assertEquals(0, ShadowDialog.getShownDialogs().size());

        TestDialog dialog = new TestDialog();
        dialog.show();

        assertEquals(1, ShadowDialog.getShownDialogs().size());
        assertEquals(dialog, ShadowDialog.getShownDialogs().get(0));

        TestDialog dialog2 = new TestDialog();
        dialog2.show();

        assertEquals(2, ShadowDialog.getShownDialogs().size());
        assertEquals(dialog2, ShadowDialog.getShownDialogs().get(1));

        dialog.dismiss();

        assertEquals(2, ShadowDialog.getShownDialogs().size());

        ShadowDialog.reset();

        assertEquals(0, ShadowDialog.getShownDialogs().size());
    }

    @Test
    public void shouldPopulateListOfRecentDialogsInCorrectOrder() throws Exception {
        new NestingTestDialog().show();
        
        assertEquals(TestDialog.class, ShadowDialog.getLatestDialog().getClass());
    }

    @Test
    public void shouldFindViewsWithinAContentViewThatWasPreviouslySet() throws Exception {
        Dialog dialog = new Dialog(Robolectric.application);
        dialog.setContentView(dialog.getLayoutInflater().inflate(R.layout.main, null));
        assertInstanceOf(TextView.class, dialog.findViewById(R.id.title));
    }
    
    private static class TestDialog extends Dialog {
        boolean onStartCalled = false;
        boolean wasDismissed =  false;

        public TestDialog() {
            super(null);
        }

        @Override
        protected void onStart() {
            onStartCalled = true;
        }

        @Override public void dismiss() {
            super.dismiss();
            wasDismissed = true;
        }
    }

    private static class NestingTestDialog extends Dialog {
        public NestingTestDialog() {
            super(null);
        };
        
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            new TestDialog().show();
        }
    }
}
