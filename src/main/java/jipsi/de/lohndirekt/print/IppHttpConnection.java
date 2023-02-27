/**
 * Copyright (C) 2003 <a href="http://www.lohndirekt.de/">lohndirekt.de</a>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package jipsi.de.lohndirekt.print;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import jipsi.de.lohndirekt.print.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * simple facade / abstraction layer to "commons httpclient"
 *
 * @author sefftinge
 *
 */
class IppHttpConnection implements IppConnection
{
  private static final Logger LOG = LoggerFactory.getLogger(IppHttpConnection.class);

  private final URI uri;
  private final String user;
  private final String password;
  private final HttpClient httpClient;
  
  /**
   * @param uri The uri of the IPP endpoint
   * @param user The user for authentication (optional)
   * @param passwd The password for authentication (optional)
   */
  IppHttpConnection(URI uri, String user, String passwd) throws IOException
  {
    this.user = user;
    this.password = passwd;
    this.uri = toHttpURI(uri);

    HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    clientBuilder.version(HttpClient.Version.HTTP_1_1);
    clientBuilder.followRedirects(HttpClient.Redirect.NEVER);
    
    httpClient = clientBuilder.build();
  }
  
  private URI toHttpURI(URI uri)
  {
    if (uri.getScheme().equals("ipp")) {
      try {
        return new URI(uri.toString().replaceAll("^ipp", "http"));
      }
      catch (URISyntaxException e) {
        throw new RuntimeException("Exception while createing http uri for " + uri);
      }
    }
    return uri;
  }

  @Override
  public IppConnectionResponse send(InputStream requestBody) throws IOException
  {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    requestBuilder.uri(uri);
    requestBuilder.header("User-Agent", "jipsi");
    requestBuilder.header("Content-type", "application/ipp");
    requestBuilder.expectContinue(false);
    //method.addHeader("Accept", "application/ipp, */*; q=.2");
    addAuthenticationHeader(requestBuilder);
    requestBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(toByteArray(requestBody)));
    
    try {
      HttpResponse<InputStream> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      
      if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new AuthenticationException();
      }
      
      return new IppConnectionResponse(response.statusCode(), response.body());
    }
    catch (InterruptedException ex) {
      throw new InterruptedIOException(ex.getMessage());
    }
  }
  
  private byte[] toByteArray(InputStream input) throws IOException
  {
    try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream(input.available())) {
      input.transferTo(byteOut);
      return byteOut.toByteArray();
    }
  }
  
  private void addAuthenticationHeader(java.net.http.HttpRequest.Builder requestBuilder)
  {
    // authentication
    if (user != null && user.trim().length() > 0 && password != null) {
      LOG.debug("Using username: {}, passwd.length: {}", user, password.length());
      String valueToEncode = user + ":" + password; // NOI18N
      String encodedAuthenticationString = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes()); // NOI18N
      
      requestBuilder.header("Authorization", encodedAuthenticationString);
    }    
  }
}
