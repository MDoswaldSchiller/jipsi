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

/**
 * 
 * 
 *
 * @author sefftinge
 */
interface IppConnection
{
  /**
   * Sends a request to the IPP host
   * 
   * @param requestBody The input stream that contains the request body data
   * 
   * @throws IOException If the communication failed
   */
  IppConnectionResponse send(InputStream requestBody) throws IOException;
  
  
  class IppConnectionResponse
  {
    private final int statusCode;
    private final InputStream responseBody;

    public IppConnectionResponse(int statusCode, InputStream responseBody)
    {
      this.statusCode = statusCode;
      this.responseBody = responseBody;
    }

    public int getStatusCode()
    {
      return statusCode;
    }

    public InputStream getResponseBody()
    {
      return responseBody;
    }
  }
}
