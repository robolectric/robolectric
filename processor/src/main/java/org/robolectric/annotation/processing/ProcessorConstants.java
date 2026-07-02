package org.robolectric.annotation.processing;

/** Package-local constants for Robolectric's javac annotation processor. */
final class ProcessorConstants {
  static final String PACKAGE_OPT = "org.robolectric.annotation.processing.shadowPackage";
  static final String SHOULD_INSTRUMENT_PKG_OPT =
      "org.robolectric.annotation.processing.shouldInstrumentPackage";
  static final String PRIORITY_OPT = "org.robolectric.annotation.processing.priority";

  private ProcessorConstants() {}
}
