package org.robolectric.android.controller;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class FragmentControllerTest {

  private static final int VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY = 123;

  @Test
  public void initialNotAttached() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment);

    assertThat(fragment.getView()).isNull();
    assertThat(fragment.getActivity()).isNull();
    assertThat(fragment.isAdded()).isFalse();
  }

  @Test
  public void initialNotAttached_customActivity() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment, LoginActivity.class);

    assertThat(fragment.getView()).isNull();
    assertThat(fragment.getActivity()).isNull();
    assertThat(fragment.isAdded()).isFalse();
  }

  @Test
  public void attachedAfterCreate() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment).create();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void attachedAfterCreate_customizedViewId() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment, CustomizedViewIdLoginActivity.class).create(VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY, null);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void attachedAfterCreate_customActivity() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment, LoginActivity.class).create();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginActivity.class);
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isFalse();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void isResumed() {
    final LoginFragment fragment = new LoginFragment();
    FragmentController.of(fragment, LoginActivity.class).create().start().resume();

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat(fragment.isAdded()).isTrue();
    assertThat(fragment.isResumed()).isTrue();
  }

  @Test
  public void isPaused() {
    final LoginFragment fragment = spy(new LoginFragment());
    FragmentController.of(fragment, LoginActivity.class).create().start().resume().pause();

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
    FragmentController.of(fragment, LoginActivity.class).create().start().resume().pause().stop();

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
    FragmentController<LoginFragment> controller = FragmentController.of(fragment, LoginActivity.class, intent).create();

    Intent intentInFragment = controller.get().getActivity().getIntent();
    assertThat(intentInFragment.getAction()).isEqualTo("test_action");
    assertThat(intentInFragment.getExtras().getString("test_key")).isEqualTo("test_value");
  }

  @Test
  public void withArguments() {
    final LoginFragment fragment = new LoginFragment();

    Bundle arguments = new Bundle();
    arguments.putString("test_argument", "test_value");
    FragmentController<LoginFragment> controller = FragmentController.of(fragment, LoginActivity.class, arguments).create();

    Bundle argumentsInFragment = controller.get().getArguments();
    assertThat(argumentsInFragment.getString("test_argument")).isEqualTo("test_value");
  }

  @Test
  public void visible() {
    final LoginFragment fragment = new LoginFragment();
    final FragmentController<LoginFragment> controller = FragmentController.of(fragment, LoginActivity.class);

    controller.create();
    assertThat(controller.get().getView()).isNotNull();
    controller.start().resume();
    assertThat(fragment.isVisible()).isFalse();

    controller.visible();
    assertThat(fragment.isVisible()).isTrue();
  }

  public static class LoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }

  private static class LoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }

  private static class CustomizedViewIdLoginActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(VIEW_ID_CUSTOMIZED_LOGIN_ACTIVITY);

      setContentView(view);
    }
  }
}
