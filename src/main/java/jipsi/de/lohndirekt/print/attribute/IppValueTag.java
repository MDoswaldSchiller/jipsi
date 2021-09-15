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

import java.util.Objects;

/**
 * Descriptions taken from <link>www.ietf.org/rfc/rfc2910.txt</link>. Tags without
 * description are not mentioned there, but are used by the CUPS-API
 */
public enum IppValueTag
{
  UNSUPPORTED_VALUE(0x10, "unsupported"),
  DEFAULT(0x11, "'default' "),
  UNKNOWN(0x12, "unknown"),
  NOVALUE(0x13, "no-value"),
  NOTSETTABLE(0x15, ""),
  DELETEATTR(0x16, ""),
  ADMINDEFINE(0x17, ""),
  INTEGER(0x21, "integer"),
  BOOLEAN(0x22, "boolean"),
  ENUM(0x23, "enum"),
  STRING(0x30, "octetString"),
  DATE(0x31, "dateTime"),
  RESOLUTION(0x32, "resolution"),
  RANGE(0x33, "rangeOfInteger"),
  BEGIN_COLLECTION(0x34, ""),
  TEXTLANG(0x35, "resolution"),
  NAMELANG(0x36, "nameWithLanguage"),
  END_COLLECTION(0x37, ""),
  TEXT(0x41, "textWithoutLanguage"),
  NAME(0x42, "nameWithoutLanguage"),
  KEYWORD(0x44, "keyword"),
  URI(0x45, "uri"),
  URISCHEME(0x46, "uriScheme"),
  CHARSET(0x47, "charset"),
  LANGUAGE(0x48, "naturalLanguage"),
  MIMETYPE(0x49, "mimeMediaType"),
  MEMBERNAME(0x4A, ""),
  MASK(0x7FFFFFFF, ""),
  COPY(0x80000001, "");
  
  private final String description;
  private final int id;
  
  private IppValueTag(int id, String description)
  {
    this.id = id;
    this.description = Objects.requireNonNull(description);
  }

  public int getId()
  {
    return this.id;
  }

  public String getDescription()
  {
    return description;
  }

  @Override
  public String toString()
  {
    return this.description;
  }
}
