package org.robolectric.internal.bytecode;

public class ShadowConstants {
  public static final String ROBO_PREFIX = "$$robo$$";
  public static final String CLASS_HANDLER_DATA_FIELD_NAME = "__robo_data__"; // todo: rename
  public static final String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";
  public static final String CONSTRUCTOR_METHOD_NAME = "__constructor__";
  public static final String GET_ROBO_DATA_METHOD_NAME = "$$robo$getData";
}
