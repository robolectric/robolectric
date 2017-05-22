package org.robolectric.annotation.processing.generator;

/**
 * Base class for code generators.
 */
public abstract class Generator {
  protected static final String GEN_CLASS = "Shadows";

  public abstract void generate();
}
