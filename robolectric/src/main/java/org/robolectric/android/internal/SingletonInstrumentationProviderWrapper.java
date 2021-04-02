package org.robolectric.android.internal;

import android.app.Instrumentation;
import androidx.test.platform.app.InstrumentationProvider;

/**
 * An {@link InstrumentationProvider} wrapper for cases where the underlying provider is not
 * idempotent but could be called multiple times
 */
public class SingletonInstrumentationProviderWrapper implements InstrumentationProvider {

  private Instrumentation instrumentation;
  private InstrumentationProvider innerInstrumentationProvider;

  public SingletonInstrumentationProviderWrapper(
      InstrumentationProvider innerInstrumentationProvider) {
    this.innerInstrumentationProvider = innerInstrumentationProvider;
  }

  @Override
  public synchronized Instrumentation provide() {
    if (instrumentation != null) {
      return instrumentation;
    } else {
      return instrumentation = innerInstrumentationProvider.provide();
    }
  }
}
