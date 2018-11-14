package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.FragmentTestUtil.startFragment;
import static org.robolectric.util.FragmentTestUtil.startVisibleFragment;

import android.app.Activity;
import android.app.Fragment;
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
public class FragmentTestUtilTest {
  @Test
  public void startFragment_shouldStartFragment() {
    final LoginFragment fragment = new LoginFragment();
    startFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void startVisibleFragment_shouldStartFragment() {
    final LoginFragment fragment = new LoginFragment();
    startVisibleFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void startVisibleFragment_shouldAttachFragmentToActivity() {
    final LoginFragment fragment = new LoginFragment();
    startVisibleFragment(fragment);

    assertThat(fragment.getView().getWindowToken()).isNotNull();
  }

  @Test
  public void startFragment_shouldStartFragmentWithSpecifiedActivityClass() {
    final LoginFragment fragment = new LoginFragment();
    startFragment(fragment, LoginActivity.class);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginActivity.class);
  }

  @Test
  public void startVisibleFragment_shouldStartFragmentWithSpecifiedActivityClass() {
    final LoginFragment fragment = new LoginFragment();
    startVisibleFragment(fragment, LoginActivity.class, 1);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginActivity.class);
  }

  @Test
  public void startVisibleFragment_shouldAttachFragmentToActivityWithSpecifiedActivityClass() {
    final LoginFragment fragment = new LoginFragment();
    startVisibleFragment(fragment, LoginActivity.class, 1);

    assertThat(fragment.getView().getWindowToken()).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginActivity.class);
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
}

