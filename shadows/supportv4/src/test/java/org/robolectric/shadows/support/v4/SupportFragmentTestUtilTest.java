package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startVisibleFragment;

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
public class SupportFragmentTestUtilTest {

  @Test
  public void startFragment_shouldStartSupportFragment() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void startVisibleFragment_shouldStartSupportFragment() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startVisibleFragment(fragment);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
  }

  @Test
  public void startVisibleFragment_shouldAttachSupportFragmentToActivity() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startVisibleFragment(fragment);

    assertThat(fragment.getView().getWindowToken()).isNotNull();
  }

  @Test
  public void startFragment_shouldStartSupportFragmentWithSpecifiedActivityClass() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startFragment(fragment, LoginFragmentActivity.class);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginFragmentActivity.class);
  }

  @Test
  public void startVisibleFragment_shouldStartSupportFragmentWithSpecifiedActivityClass() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startVisibleFragment(fragment, LoginFragmentActivity.class, 1);

    assertThat(fragment.getView()).isNotNull();
    assertThat(fragment.getActivity()).isNotNull();
    assertThat((TextView) fragment.getView().findViewById(R.id.tacos)).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginFragmentActivity.class);
  }

  @Test
  public void startVisibleFragment_shouldAttachSupportFragmentToActivityWithSpecifiedActivityClass() {
    final LoginSupportFragment fragment = new LoginSupportFragment();
    startVisibleFragment(fragment, LoginFragmentActivity.class, 1);

    assertThat(fragment.getView().getWindowToken()).isNotNull();
    assertThat(fragment.getActivity()).isInstanceOf(LoginFragmentActivity.class);
  }

  public static class LoginSupportFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_contents, container, false);
    }
  }

  public static class LoginFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      LinearLayout view = new LinearLayout(this);
      view.setId(1);

      setContentView(view);
    }
  }
}
