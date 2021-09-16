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
package jipsi.de.lohndirekt.print.attribute.ipp;

import java.util.Arrays;
import javax.print.attribute.SupportedValuesAttribute;

public class UnknownAttribute implements SupportedValuesAttribute
{
  private final String name;
  private final Object[] values;

  /**
   * @param name Name of the attribute
   * @param values The attribute values
   */
  public UnknownAttribute(String name, Object[] values)
  {
    this.name = name;
    this.values = Arrays.copyOf(values, values.length);
  }

  @Override
  public Class getCategory()
  {
    return UnknownAttribute.class;
  }

  @Override
  public String getName()
  {
    return name;
  }

  public Object[] getValues()
  {
    return values;
  }
}
