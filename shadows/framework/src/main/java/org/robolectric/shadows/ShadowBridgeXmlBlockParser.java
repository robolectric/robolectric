package org.robolectric.shadows;
#if ($api >= 21)
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link com.android.layoutlib.bridge.android.BridgeXmlBlockParser}
 */
@Implements(className = ShadowBridgeXmlBlockParser.CLASS_NAME)
public class ShadowBridgeXmlBlockParser {
  public static final String CLASS_NAME = "com.android.layoutlib.bridge.android.BridgeXmlBlockParser";
  public static final int NOT_DEFINED_LAYOUT_RES_ID = -1;

  private int layoutResId = NOT_DEFINED_LAYOUT_RES_ID;

  public void setLayoutResId(int id) {
    layoutResId = id;
  }

  public int getLayoutResId() {
    return layoutResId;
  }
}
#end

