// copied verbatim from httpclient-4.0.3 sources

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.robolectric.shadows.httpclient;

import java.net.URI;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Redirect request (can be either GET or HEAD).
 *
 * @since 4.0
 */
class HttpRedirect extends HttpRequestBase {

  private String method;

  public HttpRedirect(final String method, final URI uri) {
    super();
    if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
      this.method = HttpHead.METHOD_NAME;
    } else {
      this.method = HttpGet.METHOD_NAME;
    }
    setURI(uri);
  }

  @Override
  public String getMethod() {
    return this.method;
  }
}
