package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class DialogFragmentTest {

    @Test
    public void testDialogIsShowing() {
        DialogFragment dialogFragment = new DialogFragment();

        ContainerActivity containerActivity = new ContainerActivity();
        final ShadowDialogFragment shadowDialogFragment = shadowOf(dialogFragment);
        shadowDialogFragment.setActivity(new ContainerActivity());

        shadowDialogFragment.show(containerActivity.getSupportFragmentManager(), "TAG");
        assertTrue(shadowDialogFragment.getDialog().isShowing());
    }

    @Test
    public void testDialogDismiss() {
        DialogFragment dialogFragment = new DialogFragment();

        ContainerActivity containerActivity = new ContainerActivity();
        final ShadowDialogFragment shadowDialogFragment = shadowOf(dialogFragment);
        shadowDialogFragment.setActivity(new ContainerActivity());
        shadowDialogFragment.show(containerActivity.getSupportFragmentManager(), "TAG");
        assertTrue(shadowDialogFragment.getDialog().isShowing());

        shadowDialogFragment.dismiss();

        assertTrue(!shadowDialogFragment.getDialog().isShowing());
    }

    private static class ContainerActivity extends FragmentActivity {

    }
}
