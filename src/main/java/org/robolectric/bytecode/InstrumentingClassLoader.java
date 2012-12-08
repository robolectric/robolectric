package org.robolectric.bytecode;

public interface InstrumentingClassLoader {
    String CLASS_HANDLER_DATA_FIELD_NAME = "__robo_data__"; // todo: rename
    String STATIC_INITIALIZER_METHOD_NAME = "__staticInitializer__";
    String CONSTRUCTOR_METHOD_NAME = "__constructor__";
}
