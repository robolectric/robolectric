package org.robolectric.shadows;

import android.util.AttributeSet;
import android.view.BridgeInflater;
import android.view.View;
import com.android.layoutlib.bridge.android.BridgeXmlBlockParser;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.fakes.RenderService;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Shadow of {@link android.view.BridgeInflater}
 */
@Implements(className = ShadowBridgeInflater.CLASS_NAME)
public class ShadowBridgeInflater {

  @RealObject
  private BridgeInflater realInflater;
  public static final String CLASS_NAME = "android.view.BridgeInflater";

  @Implementation
  public View onCreateView(String name, AttributeSet attrs) {
    View view = directlyOn(realInflater, CLASS_NAME, "onCreateView",
        ClassParameter.from(String.class, name),
        ClassParameter.from(AttributeSet.class, attrs));
    if (attrs instanceof BridgeXmlBlockParser) {
      ShadowBridgeXmlBlockParser shadow = Shadow.extract(attrs);
      shadow.setLayoutResId(RenderService.getCurrentLayoutId());
    }
    ShadowView shadowView = Shadow.extract(view);
    shadowView.updateViewId(attrs, 0);
    return view;
  }
}