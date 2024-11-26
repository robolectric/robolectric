package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.textclassifier.TextClassifier;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public final class ShadowTextClassifierServiceTest {
  TextClassifier textClassifier = new TextClassifier() {};
  Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void setDefaultTextClassifierService_returnsDefaultTextClassifier() {
    ShadowTextClassifierService.setDefaultTextClassifierImplementation(textClassifier);

    assertThat(ShadowTextClassifierService.getDefaultTextClassifierImplementation(context))
        .isEqualTo(textClassifier);
  }

  @Test
  public void resetTextClassifierService_returnsNoOpTextClassifier() {
    ShadowTextClassifierService.setDefaultTextClassifierImplementation(textClassifier);
    ShadowTextClassifierService.reset();

    assertThat(ShadowTextClassifierService.getDefaultTextClassifierImplementation(context))
        .isEqualTo(TextClassifier.NO_OP);
  }

  @Test
  public void getDefaultTextClassifierImplementation_returnsNoOpTextClassifier() {
    assertThat(ShadowTextClassifierService.getDefaultTextClassifierImplementation(context))
        .isEqualTo(TextClassifier.NO_OP);
  }
}
