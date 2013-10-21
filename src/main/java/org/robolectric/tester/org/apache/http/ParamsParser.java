package org.robolectric.tester.org.apache.http;

import android.net.Uri;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParamsParser {

  public static Map<String, String> parseParams(HttpRequest request) {
    if (request instanceof HttpGet) {
      return parseParamsForGet(request);
    }
    if (request instanceof HttpEntityEnclosingRequestBase) {
      return parseParamsForRequestWithEntity((HttpEntityEnclosingRequestBase) request);
    }
    return new LinkedHashMap<String, String>();
  }

  private static Map<String, String> parseParamsForRequestWithEntity(HttpEntityEnclosingRequestBase request) {
    try {
      LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
      HttpEntity entity = request.getEntity();
      if (entity != null) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(entity);

        for (NameValuePair pair : pairs) {
          map.put(pair.getName(), pair.getValue());
        }
      }
      return map;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<String, String> parseParamsForGet(HttpRequest request) {
    Uri uri = Uri.parse(request.getRequestLine().getUri());
    Set<String> paramNames = uri.getQueryParameterNames();
    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
    for (String paramName : paramNames) {
      map.put(paramName, uri.getQueryParameter(paramName));
    }
    return map;
  }
}
