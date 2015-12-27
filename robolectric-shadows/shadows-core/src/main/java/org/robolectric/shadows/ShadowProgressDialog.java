package org.robolectric.shadows;

import android.app.ProgressDialog;
import android.widget.TextView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.app.ProgressDialog}.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {
  @RealObject ProgressDialog realProgressDialog;

  private int mProgressStyle;

  /**
   * Non-Android accessor.
   *
   * @return the message displayed in the dialog
   */
  @Override
  public CharSequence getMessage() {
    if (mProgressStyle == ProgressDialog.STYLE_HORIZONTAL) {
      return super.getMessage();
    } else {
      TextView message = ReflectionHelpers.getField(realProgressDialog, "mMessageView");
      return message.getText();
    }
  }

  @Implementation
  public void setProgressStyle(int style) {
    mProgressStyle = style;
    directlyOn(realProgressDialog, ProgressDialog.class).setProgressStyle(style);
  }

  /**
   * Non-Android accessor.
   *
   * @return the style of the progress dialog
   */
  public int getProgressStyle() {
    return mProgressStyle;
  }
}
