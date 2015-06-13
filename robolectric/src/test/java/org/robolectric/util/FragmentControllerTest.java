package org.robolectric.util;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(TestRunners.WithDefaults.class)
public class FragmentControllerTest {
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
    assertThat(fragment.getView().findViewById(R.id.tacos)).isNotNull();
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
    assertThat(fragment.getView().findViewById(R.id.tacos)).isNotNull();
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

  private static class LoginFragment extends Fragment {
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
}
