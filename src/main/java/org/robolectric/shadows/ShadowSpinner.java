package org.robolectric.shadows;

import android.widget.Spinner;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Spinner.class)
public class ShadowSpinner extends ShadowAbsSpinner {

  private CharSequence prompt;

  @Implementation
  public void setPrompt(CharSequence prompt) {
    this.prompt = prompt;
  }

  @Implementation
  public CharSequence getPrompt() {
    return prompt;
  }
}
