package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.util.TestRunnerWithManifest;

@RunWith(TestRunnerWithManifest.class)
public class SupportFragmentControllerTest {

  private static final int VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY = 123;

  @Test
  public void initialNotAttached() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment);

    assertThat(fragment.getView()).isNull();
    assertThat(fragment.getActivity()).isNull();
    assertThat(fragment.isAdded()).isFalse();
  }

  @Test
  public void initialNotAttached_customActivity() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment, LoginActivity.class);

    assertThat(fragment.getView()).isNull();
    assertThat(fragment.getActivity()).isNull();
    assertThat(fragment.isAdded()).isFalse();
  }

  @Test
  public void attachedAfterCreate() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment).create();

    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
  }

  @Test
  public void attachedAfterCreate_customActivity() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment, LoginActivity.class).create();

    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginActivity.class);
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
  }

  @Test
  public void attachedAfterCreate_customizedViewId() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment, CustomizedViewIdLoginActivity.class).create(VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY, null).start();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void hasViewAfterStart() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment).create().start();

    assertThat(fragment.getView()).isNotNull();
  }

  @Test
  public void isResumed() {
    final LoginFragment fragment = new LoginFragment();
    SupportFragmentController.of(fragment, LoginActivity.class).create().start().resume();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isTrue();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void isPaused() {
    final LoginFragment fragment = spy(new LoginFragment());
    SupportFragmentController.of(fragment, LoginActivity.class).create().start().resume().pause();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();

    verify(fragment).onResume();
    verify(fragment).onPause();
  }

  @Test
  public void isStopped() {
    final LoginFragment fragment = spy(new LoginFragment());
    SupportFragmentController.of(fragment, LoginActivity.class).create().start().resume().pause().stop();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();

    verify(fragment).onStart();
    verify(fragment).onResume();
    verify(fragment).onPause();
    verify(fragment).onStop();
  }

  @Test
  public void withIntent() {
    final LoginFragment fragment = new LoginFragment();
    Intent intent = new Intent("test_action");
    intent.putExtra("test_key", "test_value");
    SupportFragmentController<LoginFragment> controller =
        SupportFragmentController.of(fragment, LoginActivity.class, intent).create();

    Intent intentInFragment = controller.get().getActivity().getIntent();
    assertThat(intentInFragment.getAction()).isEqualTo("test_action");
    assertThat(intentInFragment.getExtras().getString("test_key")).isEqualTo("test_value");
  }

  @Test
  public void visible() {
    final LoginFragment fragment = new LoginFragment();
    final SupportFragmentController<LoginFragment> controller = SupportFragmentController.of(fragment, LoginActivity.class);

    controller.create().start().resume();
    assertThat(fragment.isVisible()).isFalse();

    controller.visible();
    assertThat(fragment.isVisible()).isTrue();
  }

  @Test
  public void savesInstanceState() {
    final LoginFragment fragment = new LoginFragment();
    final SupportFragmentController<LoginFragment> controller =
        SupportFragmentController.of(fragment, LoginActivity.class);
    controller.create().start().resume().visible();
    LoginActivity activity = (LoginActivity) controller.get().getActivity();
    Bundle expectedState = new Bundle();
    expectedState.putBoolean("isRestored", true);
    activity.setState(expectedState);
    final Bundle savedInstanceState = new Bundle();

    controller.saveInstanceState(savedInstanceState);

    assertThat(savedInstanceState.getBoolean("isRestored")).isTrue();
  }

  public static class LoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }

  public static class LoginActivity extends FragmentActivity {
    private Bundle state = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putAll(state);
    }

    public void setState(Bundle state) {
      this.state = state;
    }
  }

  public static class CustomizedViewIdLoginActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY);

      setContentView(view);
    }
  }
}
