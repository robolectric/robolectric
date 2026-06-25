package org.robolectric.shadows;

import android.app.ProgressDialog;
import android.widget.TextView;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ProgressDialog.class)
public class ShadowProgressDialog extends ShadowAlertDialog {
  @RealObject ProgressDialog realProgressDialog;

  private int mProgressStyle;

  /**
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

  @Filter
  protected void setProgressStyle(int style) {
    mProgressStyle = style;
  }

  /**
   * @return the style of the progress dialog
   */
  public int getProgressStyle() {
    return mProgressStyle;
  }
}
