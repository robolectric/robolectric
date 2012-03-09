package com.xtremelabs.robolectric.shadows;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class DialogFragmentTest {

    @Test
    public void testDialogIsShowing() {
        DialogFragment dialogFragment = new DialogFragment();

        ContainerActivity containerActivity = new ContainerActivity();

        FragmentManager fragmentManager = containerActivity.getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "TAG");
        assertTrue(dialogFragment.getDialog().isShowing());
        assertSame(dialogFragment, fragmentManager.findFragmentByTag("TAG"));
        assertSame(containerActivity, dialogFragment.getActivity());
    }

    @Test
    public void testDialogDismiss() {
        DialogFragment dialogFragment = new DialogFragment();

        ContainerActivity containerActivity = new ContainerActivity();

        FragmentManager fragmentManager = containerActivity.getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "TAG");
        dialogFragment.dismiss();

        assertTrue(!dialogFragment.getDialog().isShowing());
        assertNull(fragmentManager.findFragmentByTag("TAG"));
    }

    private static class ContainerActivity extends FragmentActivity {

    }
}
