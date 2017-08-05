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

    // screenLayout bits for layout direction.
    public static final int MASK_LAYOUTDIR = 0xC0;
    public static final int SHIFT_LAYOUTDIR = 6;
    public static final int LAYOUTDIR_ANY = ACONFIGURATION_LAYOUTDIR_ANY << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_LTR = ACONFIGURATION_LAYOUTDIR_LTR << SHIFT_LAYOUTDIR;
    public static final int LAYOUTDIR_RTL = ACONFIGURATION_LAYOUTDIR_RTL << SHIFT_LAYOUTDIR;

    public static final int SCREENWIDTH_ANY = 0;

    public int mcc;
    public int mnc;
    public int screenLayout;
    public int smallestScreenWidthDp;
}
