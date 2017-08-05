package org.robolectric.res;

public class ResTableConfig {

    /** Layout direction: not specified. */
    public static final int ACONFIGURATION_LAYOUTDIR_ANY  = 0x00;
    /**
     * Layout direction: value that corresponds to
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#LayoutDirectionQualifier">ldltr</a> resource qualifier specified.
     */
    public static final int ACONFIGURATION_LAYOUTDIR_LTR  = 0x01;
    /**
     * Layout direction: value that corresponds to
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#LayoutDirectionQualifier">ldrtl</a> resource qualifier specified.
     */
    public static final int ACONFIGURATION_LAYOUTDIR_RTL  = 0x02;

    /** Screen size: not specified. */
    public static final int ACONFIGURATION_SCREENSIZE_ANY  = 0x00;
    /**
     * Screen size: value indicating the screen is at least
     * approximately 320x426 dp units, corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">small</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_SCREENSIZE_SMALL = 0x01;
    /**
     * Screen size: value indicating the screen is at least
     * approximately 320x470 dp units, corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">normal</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_SCREENSIZE_NORMAL = 0x02;
    /**
     * Screen size: value indicating the screen is at least
     * approximately 480x640 dp units, corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">large</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_SCREENSIZE_LARGE = 0x03;
    /**
     * Screen size: value indicating the screen is at least
     * approximately 720x960 dp units, corresponding to the
     * <a href="@dacRoot/guide/topics/resources/providing-resources.html#ScreenSizeQualifier">xlarge</a>
     * resource qualifier.
     */
    public static final int ACONFIGURATION_SCREENSIZE_XLARGE = 0x04;

    // screenLayout bits for layout direction.
    public static final int MASK_LAYOUTDIR = 0xC0;
    public static final int SHIFT_LAYOUTDIR = 6;
    public static final int LAYOUTDIR_ANY = ACONFIGURATION_LAYOUTDIR_ANY << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_LTR = ACONFIGURATION_LAYOUTDIR_LTR << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_RTL = ACONFIGURATION_LAYOUTDIR_RTL << SHIFT_LAYOUTDIR;

    public static final int SCREENWIDTH_ANY = 0;
    public static final int MASK_SCREENSIZE = 0x0f;
    public static final int SCREENSIZE_ANY = ACONFIGURATION_SCREENSIZE_ANY;
    public static final int SCREENSIZE_SMALL = ACONFIGURATION_SCREENSIZE_SMALL;
    public static final int SCREENSIZE_NORMAL = ACONFIGURATION_SCREENSIZE_NORMAL;
    public static final int SCREENSIZE_LARGE = ACONFIGURATION_SCREENSIZE_LARGE;
    public static final int SCREENSIZE_XLARGE = ACONFIGURATION_SCREENSIZE_XLARGE;

    public int mcc;
    public int mnc;
    public int screenLayout;
    public int smallestScreenWidthDp;
    public int screenWidthDp;
    public int screenHeightDp;
}
