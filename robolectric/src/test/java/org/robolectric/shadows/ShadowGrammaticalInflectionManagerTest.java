package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.GrammaticalInflectionManager;
import android.content.Context;
import android.content.res.Configuration;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestActivity;

/** Test for {@link ShadowGrammaticalInflectionManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = UPSIDE_DOWN_CAKE)
public class ShadowGrammaticalInflectionManagerTest {

  private Context context;
  private GrammaticalInflectionManager grammaticalInflectionManager;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    grammaticalInflectionManager =
        (GrammaticalInflectionManager)
            context.getSystemService(Context.GRAMMATICAL_INFLECTION_SERVICE);
  }

  @Test
  public void getRequestedApplicationGrammaticalGender_returnsNotSpecified() {
    assertThat(grammaticalInflectionManager.getApplicationGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED);
  }

  @Test
  public void setRequestedApplicationGrammaticalGender_updatesApplicationGrammaticalGender() {
    grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(
        Configuration.GRAMMATICAL_GENDER_FEMININE);

    assertThat(grammaticalInflectionManager.getApplicationGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_FEMININE);
  }

  @Test
  public void setRequestedApplicationGrammaticalGender_updatesConfiguration() {
    grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(
        Configuration.GRAMMATICAL_GENDER_FEMININE);

    assertThat(context.getResources().getConfiguration().getGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_FEMININE);
  }

  @Test
  public void setRequestedApplicationGrammaticalGender_withNotSpecified_updatesConfiguration() {
    grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(
        Configuration.GRAMMATICAL_GENDER_FEMININE);
    grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(
        Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED);

    assertThat(context.getResources().getConfiguration().getGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED);
  }

  @Test
  public void setRequestedApplicationGrammaticalGender_invalidValue_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(100));
  }

  @Test
  public void setRequestedApplicationGrammaticalGender_updatesActivityConfiguration() {
    // Setup an activity
    ActivityController<TestActivity> activityController =
        Robolectric.buildActivity(TestActivity.class).setup().create().start().resume();
    TestActivity activity = activityController.get();

    // Verify the initial grammatical gender
    GrammaticalInflectionManager activityGim =
        (GrammaticalInflectionManager)
            activity.getSystemService(Context.GRAMMATICAL_INFLECTION_SERVICE);
    assertThat(activityGim.getApplicationGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_NOT_SPECIFIED);

    // Make a grammatical gender config change
    grammaticalInflectionManager.setRequestedApplicationGrammaticalGender(
        Configuration.GRAMMATICAL_GENDER_MASCULINE);
    activityController.configurationChange();
    TestActivity activity2 = activityController.get();

    // Verify the grammatical gender is updated
    GrammaticalInflectionManager activityGim2 =
        (GrammaticalInflectionManager)
            activity2.getSystemService(Context.GRAMMATICAL_INFLECTION_SERVICE);
    assertThat(activityGim2.getApplicationGrammaticalGender())
        .isEqualTo(Configuration.GRAMMATICAL_GENDER_MASCULINE);
  }
}
