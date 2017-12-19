package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.ProgressDialog;
import android.widget.TextView;
import org.robolectric.annotation.Implementation;
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

  @Implementation
  protected void setProgressStyle(int style) {
    mProgressStyle = style;
    directlyOn(realProgressDialog, ProgressDialog.class).setProgressStyle(style);
  }

  /**
   * @return the style of the progress dialog
   */
  public int getProgressStyle() {
    return mProgressStyle;
  }
}
