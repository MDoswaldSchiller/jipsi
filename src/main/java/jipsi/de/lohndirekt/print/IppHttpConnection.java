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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import jipsi.de.lohndirekt.print.exception.AuthenticationException;
import org.apache.http.HttpException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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

  private final CloseableHttpClient httpConn;
  private final HttpPost method;
  private int status;
  private InputStream responseData;
  /**
   * @param uri The uri of the IPP endpoint
   * @param user The user for authentication (optional)
   * @param passwd The password for authentication (optional)
   */
  IppHttpConnection(URI uri, String user, String passwd) throws IOException
  {
    URI httpURI = toHttpURI(uri);
    
    method = new HttpPost(httpURI.toString());
    method.addHeader("Content-type", "application/ipp");
    //method.addHeader("Accept", "application/ipp, */*; q=.2");

    HttpClientBuilder clientBuilder = HttpClients.custom();
    clientBuilder.disableContentCompression();
    clientBuilder.setUserAgent("JAPI");

    RequestConfig requestConfig =  RequestConfig.copy(RequestConfig.DEFAULT)
        .setExpectContinueEnabled(true)
        .setRedirectsEnabled(false)
        .build();
    clientBuilder.setDefaultRequestConfig(requestConfig);
    
    // authentication
    if (user != null && user.trim().length() > 0 && passwd != null) {
      LOG.debug("Using username: {}, passwd.length: {}", user, passwd.length());
      AuthScope authScope = new AuthScope(httpURI.getHost(), httpURI.getPort());
      Credentials creds = new UsernamePasswordCredentials(user, passwd);
      
      BasicCredentialsProvider credentialProvider = new BasicCredentialsProvider();
      credentialProvider.setCredentials(authScope, creds);
      
      clientBuilder.setDefaultCredentialsProvider(credentialProvider);
    }
    
    httpConn = clientBuilder.build();
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

  /**
   * @return content of the response
   * @throws IOException
   */
  @Override
  public InputStream getIppResponse() throws IOException
  {
    return responseData;
  }

  /**
   * @return the statuscode of last request
   * @throws IOException
   */
  @Override
  public int getStatusCode() throws IOException
  {
    return status;
  }

  /**
   * @param stream
   */
  @Override
  public void setIppRequest(InputStream stream)
  {
    try {
      method.setEntity(new BufferedHttpEntity(new InputStreamEntity(stream)));
    }
    catch (IOException ex) {
      LOG.error("Error while buffering ipp request", ex);
    }
  }

  @Override
  public void execute() throws HttpException, IOException
  {
//    if (!method.validate()) {
//      throw new IllegalStateException("Request is not ready");
//    }
      
    try (CloseableHttpResponse response = httpConn.execute(method)) {
      status = response.getStatusLine().getStatusCode();
      
      if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new AuthenticationException(response.getStatusLine().getReasonPhrase());
      }
      
      InputStream content = response.getEntity().getContent();
      ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
      content.transferTo(contentBuffer);
      
      responseData = new ByteArrayInputStream(contentBuffer.toByteArray());
    }
  }
}
