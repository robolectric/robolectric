package org.robolectric.internal.dependency;

import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import org.robolectric.util.Logger;

/**
 * Used to get KeyStore that can be used to establish TLS connections. Adapted from BouncyCastle.
 */
class KeyStoreUtil {
  static KeyStore getKeyStore() throws GeneralSecurityException, IOException {
    String defaultType = KeyStore.getDefaultType();

    String tsPath = null;
    char[] tsPassword = null;

    String trustStoreProp = getSystemProperty("javax.net.ssl.trustStore");
    if ("NONE".equals(trustStoreProp)) {
      // Do not try to load any file
    } else if (!Strings.isNullOrEmpty(trustStoreProp)) {
      if (new File(trustStoreProp).exists()) {
        tsPath = trustStoreProp;
      }
    } else {
      String javaHome = getSystemProperty("java.home");
      if (!Strings.isNullOrEmpty(javaHome)) {
        String jsseCacertsPath = javaHome + "/lib/security/jssecacerts";
        if (new File(jsseCacertsPath).exists()) {
          defaultType = "jks";
          tsPath = jsseCacertsPath;
        } else {
          String cacertsPath = javaHome + "/lib/security/cacerts";
          if (new File(cacertsPath).exists()) {
            defaultType = "jks";
            tsPath = cacertsPath;
          }
        }
      }
    }

    KeyStore ks = createTrustStore(defaultType);

    String tsPasswordProp = getSystemProperty("javax.net.ssl.trustStorePassword");
    if (!Strings.isNullOrEmpty(tsPasswordProp)) {
      tsPassword = tsPasswordProp.toCharArray();
    }

    InputStream tsInput = null;
    try {
      if (Strings.isNullOrEmpty(tsPath)) {
        Logger.info("Initializing empty trust store");
      } else {
        Logger.info("Initializing with trust store at path: " + tsPath);
        tsInput = new BufferedInputStream(new FileInputStream(tsPath));
      }
      ks.load(tsInput, tsPassword);
    } finally {
      Closeables.closeQuietly(tsInput);
    }

    return ks;
  }

  private static KeyStore createTrustStore(String defaultType)
      throws NoSuchProviderException, KeyStoreException {
    String tsType = getSystemProperty("javax.net.ssl.trustStoreType");
    tsType = Strings.isNullOrEmpty(tsType) ? defaultType : tsType;

    String tsProv = getSystemProperty("javax.net.ssl.trustStoreProvider");
    return Strings.isNullOrEmpty(tsProv)
        ? KeyStore.getInstance(tsType)
        : KeyStore.getInstance(tsType, tsProv);
  }

  private static String getSystemProperty(final String propertyName) {
    return AccessController.doPrivileged(
        (PrivilegedAction<String>) () -> System.getProperty(propertyName));
  }
}
