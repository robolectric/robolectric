package org.robolectric.res.android;

// transliterated from https://android.googlesource.com/platform/system/core/+/android-9.0.0_r12/include/utils/Errors.h

public class Errors {

  public static final int NO_ERROR = 0;

  // in the Cpp code, 'int' return values can either indicate an error, or a valid value
  // success can be interpreted as return value >= 0. So make all error codes negative values
  // following the convention of  assigning UNKNOWN_ERROR to INT32_MIN value as a base and
  // incrementing from there

  public static final int UNKNOWN_ERROR = Integer.MIN_VALUE;
  public static final int BAD_INDEX = UNKNOWN_ERROR + 1;
  public static final int BAD_TYPE = UNKNOWN_ERROR + 2;
  public static final int BAD_VALUE = UNKNOWN_ERROR + 3;
  public static final int NO_MEMORY = UNKNOWN_ERROR + 4;
  public static final int NAME_NOT_FOUND = UNKNOWN_ERROR + 5;
  public static final int NO_INIT = UNKNOWN_ERROR + 6;
}
