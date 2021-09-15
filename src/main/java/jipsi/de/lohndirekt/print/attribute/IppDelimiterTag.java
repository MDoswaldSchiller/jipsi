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
 * Bezeichnungen �bernommen aus <link>www.ietf.org/rfc/rfc2910.txt</link>
 * Tags ohne Bezeichnung sind dort nicht aufgef�hrt, werden aber in der Cups-API
 * verwendet
 */
public enum IppDelimiterTag
{
  ZERO(0x00, ""),
  BEGIN_OPERATION_ATTRIBUTES(0x01, "operation-attributes-tag"),
  BEGIN_JOB_ATTRIBUTES(0x02, "job-attributes-tag"),
  END_ATTRIBUTES(0x03, "end-of-attributes-tag"),
  BEGIN_PRINTER_ATTRIBUTES(0x04, "printer-attributes-tag"),
  UNSUPPORTED_GROUP(0x05, "unsupported-attributes-tag"),
  SUBSCRIPTION(0x06, ""),
  EVENT_NOTIFICATION(0x07, "");

  private final String description;
  private final int value;
  
  private IppDelimiterTag(int value, String description)
  {
    this.value = value;
    this.description = Objects.requireNonNull(description);
  }

  public int getValue()
  {
    return this.value;
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
