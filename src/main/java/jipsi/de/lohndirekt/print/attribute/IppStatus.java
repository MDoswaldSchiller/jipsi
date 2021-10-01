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
package jipsi.de.lohndirekt.print.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public enum IppStatus
{
  SUCCESSFUL_OK("successful-ok", 0x0000),
  SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES("successful-ok-ignored-or-substituted-attributes", 0x0001),
  SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES("successful-ok-conflicting-attributes", 0x0002),
  CLIENT_ERROR_BAD_REQUEST("client-error-bad-request", 0x0400),
  CLIENT_ERROR_FORBIDDEN("client-error-forbidden", 0x0401),
  CLIENT_ERROR_NOT_AUTHENTICATED("client-error-not-authenticated", 0x0402),
  CLIENT_ERROR_NOT_AUTHORIZED("client-error-not-authorized", 0x0403),
  CLIENT_ERROR_NOT_POSSIBLE("client-error-not-possible", 0x0404),
  CLIENT_ERROR_TIMEOUT("client-error-timeout", 0x0405),
  CLIENT_ERROR_NOT_FOUND("client-error-not-found", 0x0406),
  CLIENT_ERROR_GONE("client-error-gone", 0x0407),
  CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE("client-error-request-entity-too-large", 0x0408),
  CLIENT_ERROR_REQUEST_VALUE_TOO_LONG("client-error-request-value-too-long", 0x0409),
  CLIENT_ERROR_DOCUMENT_FORMAT_NOT_SUPPORTED("client-error-document-format-not-supported", 0x040A),
  CLIENT_ERROR_ATTRIBUTES_OR_VALUES_NOT_SUPPORTED("client-error-attributes-or-values-not-supported", 0x040B),
  CLIENT_ERROR_URI_SCHEME_NOT_SUPPORTED("client-error-uri-scheme-not-supported", 0x040C),
  CLIENT_ERROR_CHARSET_NOT_SUPPORTED("client-error-charset-not-supported", 0x040D),
  CLIENT_ERROR_CONFLICTING_ATTRIBUTES("client-error-conflicting-attributes", 0x040E),
  CLIENT_ERROR_COMPRESSION_NOT_SUPPORTED("client-error-compression-not-supported", 0x040F),
  CLIENT_ERROR_COMPRESSION_ERROR("client-error-compression-error", 0x0410),
  CLIENT_ERROR_DOCUMENT_FORMAT_ERROR("client-error-document-format-error", 0x0411),
  CLIENT_ERROR_DOCUMENT_ACCESS_ERROR("client-error-document-access-error", 0x0412),
  SERVER_ERROR_INTERNAL_ERROR("server-error-internal-error", 0x0500),
  SERVER_ERROR_OPERATION_NOT_SUPPORTED("server-error-operation-not-supported", 0x0501),
  SERVER_ERROR_SERVICE_UNAVAILABLE("server-error-service-unavailable", 0x0502),
  SERVER_ERROR_VERSION_NOT_SUPPORTED("server-error-version-not-supported", 0x0503),
  SERVER_ERROR_DEVICE_ERROR("server-error-device-error", 0x0504),
  SERVER_ERROR_TEMPORARY_ERROR("server-error-temporary-error", 0x0505),
  SERVER_ERROR_NOT_ACCEPTING_JOBS("server-error-not-accepting-jobs", 0x0506),
  SERVER_ERROR_BUSY("server-error-busy", 0x0507),
  SERVER_ERROR_JOB_CANCELED("server-error-job-canceled", 0x0508),
  SERVER_ERROR_MULTIPLE_DOCUMENT_JOBS_NOT_SUPPORTED("server-error-multiple-document-jobs-not-supported", 0x0509);


  /**
   * Build map for faster lookup of status ids
   */
  private static final Map<Integer,IppStatus> CACHE;
  static
  {
    Map<Integer,IppStatus> cache = new HashMap<>();
    for (IppStatus status : values()) {
      cache.put(status.getId(), status);
    }
    CACHE = Map.copyOf(cache);
  }

  private final String text;
  private final int id;

  private IppStatus(String statusText, int statusCode)
  {
    this.id = statusCode;
    this.text = Objects.requireNonNull(statusText);
  }

  public String getText()
  {
    return this.text;
  }

  public int getId()
  {
    return this.id;
  }
  
  public static IppStatus fromStatusId(int statusId)
  {
    IppStatus status = CACHE.get(statusId);
    if (status == null) {
      throw new NoSuchElementException("Unknown status id: " + statusId);
    }
    
    return status;
  }
}
