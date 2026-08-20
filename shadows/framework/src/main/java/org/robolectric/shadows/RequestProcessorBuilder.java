package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.camera2.extension.IRequestProcessorImpl;
import android.hardware.camera2.extension.RequestProcessor;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link RequestProcessor}. */
public class RequestProcessorBuilder {

  private RequestProcessorBuilder() {}

  public static RequestProcessorBuilder newBuilder() {
    return new RequestProcessorBuilder();
  }

  public RequestProcessor build() {
    IRequestProcessorImpl nullProxy =
        ReflectionHelpers.createNullProxy(IRequestProcessorImpl.class);
    return reflector(RequestProcessorReflector.class).newRequestProcessor(nullProxy, 0L);
  }

  @ForType(RequestProcessor.class)
  interface RequestProcessorReflector {
    @Constructor
    RequestProcessor newRequestProcessor(IRequestProcessorImpl requestProcessorImpl, long vendorId);
  }
}
