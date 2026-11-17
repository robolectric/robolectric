package org.robolectric.integrationtests.commonslogging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.Init;

final class LoggingClient {
  static Log getLogger() {
    return LogFactory.getLog(LoggingClient.class);
  }

  static void initializeXmlSecurity() {
    Init.init();
  }
}
