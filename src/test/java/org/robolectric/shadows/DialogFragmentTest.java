package org.robolectric.shadows;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Transcript;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class DialogFragmentTest {

  private FragmentActivity activity;
  private TestDialogFragment dialogFragment;
  private FragmentManager fragmentManager;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.buildActivity(FragmentActivity.class).create().start().resume().get();
    fragmentManager = activity.getSupportFragmentManager();
    dialogFragment = new TestDialogFragment();
  }

  @Test
  public void show_shouldCallLifecycleMethods() throws Exception {
    dialogFragment.show(fragmentManager, "this is a tag");

    dialogFragment.transcript.assertEventsSoFar(
        "onAttach",
        "onCreate",
        "onCreateDialog",
        "onCreateView",
        "onViewCreated",
        "onActivityCreated",
        "onStart",
        "onResume"
    );

    assertNotNull(dialogFragment.getActivity());
    assertSame(activity, dialogFragment.onAttachActivity);
  }

  @Test
  public void show_whenPassedATransaction_shouldCallShowWithManager() throws Exception {
    dialogFragment.show(fragmentManager.beginTransaction(), "this is a tag");

    dialogFragment.transcript.assertEventsSoFar(
        "onAttach",
        "onCreate",
        "onCreateDialog",
        "onCreateView",
        "onViewCreated",
        "onActivityCreated",
        "onStart",
        "onResume"
    );

    assertNotNull(dialogFragment.getActivity());
    assertSame(activity, dialogFragment.onAttachActivity);
  }

  @Test
  public void show_shouldShowDialogThatWasReturnedFromOnCreateDialog_whenOnCreateDialogReturnsADialog() throws Exception {
    Dialog dialogFromOnCreateDialog = new Dialog(activity);
    dialogFragment.returnThisDialogFromOnCreateDialog(dialogFromOnCreateDialog);
    dialogFragment.show(fragmentManager, "this is a tag");

    Dialog dialog = ShadowDialog.getLatestDialog();
    assertSame(dialogFromOnCreateDialog, dialog);
    assertSame(dialogFromOnCreateDialog, dialogFragment.getDialog());
    assertSame(dialogFragment, fragmentManager.findFragmentByTag("this is a tag"));
  }

  @Test
  public void show_shouldShowDialogThatWasAutomaticallyCreated_whenOnCreateDialogReturnsNull() throws Exception {
    dialogFragment.show(fragmentManager, "this is a tag");

    Dialog dialog = ShadowDialog.getLatestDialog();
    assertNotNull(dialog);
    assertSame(dialog, dialogFragment.getDialog());
    assertNotNull(dialog.findViewById(R.id.title));
    assertSame(dialogFragment, fragmentManager.findFragmentByTag("this is a tag"));
  }

  @Ignore("needs some work") @Test
  public void dismiss_shouldDismissTheDialog() throws Exception {
    dialogFragment.show(fragmentManager, "tag");

    dialogFragment.dismiss();

    Dialog dialog = ShadowDialog.getLatestDialog();
    assertFalse(dialog.isShowing());
    assertTrue(shadowOf(dialog).hasBeenDismissed());

    assertNull(fragmentManager.findFragmentByTag("tag"));
  }

  @Test
  public void removeUsingTransaction_shouldDismissTheDialog() throws Exception {
    dialogFragment.show(fragmentManager, null);

    FragmentTransaction t = fragmentManager.beginTransaction();
    t.remove(dialogFragment);
    t.commit();

    Dialog dialog = ShadowDialog.getLatestDialog();
    assertFalse(dialog.isShowing());
    assertTrue(shadowOf(dialog).hasBeenDismissed());
  }

  private class TestDialogFragment extends DialogFragment {
    final Transcript transcript = new Transcript();
    Activity onAttachActivity;
    private Dialog returnThisDialogFromOnCreateDialog;

    @Override
    public void onAttach(Activity activity) {
      transcript.add("onAttach");
      onAttachActivity = activity;
      super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      transcript.add("onCreate");
      super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      transcript.add("onCreateDialog");
      return returnThisDialogFromOnCreateDialog == null
          ? super.onCreateDialog(savedInstanceState)
          : returnThisDialogFromOnCreateDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      transcript.add("onCreateView");
      return inflater.inflate(R.layout.main, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
      transcript.add("onViewCreated");
      super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      transcript.add("onActivityCreated");
      super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
      transcript.add("onStart");
      super.onStart();
    }

    @Override
    public void onResume() {
      transcript.add("onResume");
      super.onResume();
    }

    public void returnThisDialogFromOnCreateDialog(Dialog dialog) {
      returnThisDialogFromOnCreateDialog = dialog;
    }
  }
}
