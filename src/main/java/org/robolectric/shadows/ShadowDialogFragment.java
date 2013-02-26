package org.robolectric.shadows;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.tester.android.util.TestFragmentManager;
import org.robolectric.tester.android.util.TestFragmentTransaction;

import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadow implementation of the {@link DialogFragment}.
 */
@Implements(DialogFragment.class)
public class ShadowDialogFragment extends ShadowFragment {
    private static DialogFragment latestDialogFragment;

    private Dialog dialog;
    private TestFragmentManager testFragmentManager;

    @RealObject
    private DialogFragment realDialogFragment;

    @Implementation
    public int show(FragmentTransaction transaction, String tag) {
        TestFragmentTransaction ft = (TestFragmentTransaction) transaction;
        show(ft.getManager(), tag);
        ft.commit();
        return 0;
    }

    @Implementation
    public void show(FragmentManager manager, String tag) {
        testFragmentManager = (TestFragmentManager) manager;
        FragmentActivity activityFromManager = testFragmentManager.getActivity();

        shadowOf(realDialogFragment).setActivity(activityFromManager);

        realDialogFragment.onAttach(fragmentActivity);
        realDialogFragment.onCreate(null);
        dialog = realDialogFragment.onCreateDialog(null);
        view = realDialogFragment.onCreateView(ShadowLayoutInflater.from(fragmentActivity), null, null);
        if (dialog == null) {
            dialog = new Dialog(activityFromManager);
            dialog.setContentView(view);
        }
        testFragmentManager.addDialogFragment(tag, realDialogFragment);
        realDialogFragment.onViewCreated(view, null);
        realDialogFragment.onActivityCreated(null);
        realDialogFragment.onStart();
        realDialogFragment.onResume();

        latestDialogFragment = realDialogFragment;
    }

    @Implementation
    public void onStart() {
        if (dialog != null) {
            dialog.show();
        }
    }

    @Implementation
    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }

        if (testFragmentManager == null) {
            testFragmentManager = (TestFragmentManager) getFragmentManager();
        }
        testFragmentManager.removeDialogFragment(realDialogFragment);
    }

    @Implementation
    public Dialog getDialog() {
        return dialog;
    }

    // The following API is not supported by Android Support Library V4 r6(r7). Need to add annotation back
    // when Maven supports newer revision of support library.
    // Also fix the case in RobolectricWiringTest#verifyMethod(Class, Method)
//    @Implementation
    public void dismissAllowingStateLoss() {
        dismiss();
    }

    public static DialogFragment getLatestDialogFragment() {
        return latestDialogFragment;
    }
}
