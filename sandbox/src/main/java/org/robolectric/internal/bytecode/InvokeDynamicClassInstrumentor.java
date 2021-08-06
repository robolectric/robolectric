package org.robolectric.internal.bytecode;

/**
 * @deprecated The invoke-dynamic case has been moved to ClassInstrumentor. Classes previously
 *     extending this class should extend {@link ClassInstrumentor} directly.
 */
@Deprecated
public class InvokeDynamicClassInstrumentor extends ClassInstrumentor {
  public InvokeDynamicClassInstrumentor(Decorator decorator) {
    super(decorator);
  }
}
