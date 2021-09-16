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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import jipsi.de.lohndirekt.print.exception.AuthenticationException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
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

  private HttpClient httpConn;
  private PostMethod method;

  /**
   * @param uri The uri of the IPP endpoint
   * @param user The user for authentication (optional)
   * @param passwd The password for authentication (optional)
   */
  public IppHttpConnection(URI uri, String user, String passwd)
  {
    try {
      httpConn = new HttpClient();
      URI httpURI = toHttpURI(uri);
      method = new PostMethod(httpURI.toString());
      method.addRequestHeader("Content-type", "application/ipp");
      method.addRequestHeader("Accept", "application/ipp, */*; q=.2");
     
      // authentication
      if (user != null && user.trim().length() > 0) {
        LOG.debug("Using username: {}, passwd.length: {}", user, passwd.length());
        method.setDoAuthentication(true);
        AuthScope authScope = new AuthScope(httpURI.getHost(), httpURI.getPort());
        Credentials creds = new UsernamePasswordCredentials(user, passwd);
        httpConn.getState().setCredentials(authScope, creds);
      }
    }
    catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * @return content of the response
   * @throws IOException
   */
  @Override
  public InputStream getIppResponse() throws IOException
  {
    return method.getResponseBodyAsStream();
  }

  /**
   * @return the statuscode of last request
   * @throws IOException
   */
  @Override
  public int getStatusCode() throws IOException
  {
    return method.getStatusCode();
  }

  private URI toHttpURI(URI uri)
  {
    if (uri.getScheme().equals("ipp")) {
      String uriString = uri.toString().replaceAll("^ipp", "http");

      try {
        uri = new URI(uriString);
      }
      catch (URISyntaxException e) {
        throw new RuntimeException("toHttpURI buggy? : uri was " + uri);
      }
    }
    return uri;
  }

  /**
   * @param stream
   */
  @Override
  public void setIppRequest(InputStream stream)
  {
    method.setRequestEntity(new InputStreamRequestEntity(stream));
  }

  @Override
  public boolean execute() throws HttpException, IOException
  {
    if (method.validate()) {
      httpConn.executeMethod(method);
      if (this.getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new AuthenticationException(method.getStatusText());
      }
      return true;
    }
    else {
      return false;
    }
  }
}
