package org.robolectric.shadows;

import android.widget.Checkable;
import android.widget.CompoundButton;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.directlyOn;

/**
 * Shadows the {@code android.widget.CompoundButton} class.
 * <p/>
 * Keeps track of whether or not its "checked" state is set and deals with listeners in an appropriate way.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = CompoundButton.class)
public class ShadowCompoundButton extends ShadowTextView implements Checkable {
  @RealObject CompoundButton realCompoundButton;
  private boolean checked;
  private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

  @Implementation
  @Override public void toggle() {
    setChecked(!checked);
  }

  @Implementation
  public boolean performClick() {
    toggle();
    return (Boolean) directlyOn(realCompoundButton, CompoundButton.class, "performClick").invoke();
  }

  @Implementation
  @Override public boolean isChecked() {
    return checked;
  }

  @Implementation
  @Override public void setChecked(boolean checked) {
    if (this.checked != checked) {
      this.checked = checked;
      if (onCheckedChangeListener != null) {
        onCheckedChangeListener.onCheckedChanged((CompoundButton) realView, this.checked);
      }
    }
  }

  @Implementation
  public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
    onCheckedChangeListener = listener;
  }

  public CompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
    return onCheckedChangeListener;
  }
}
