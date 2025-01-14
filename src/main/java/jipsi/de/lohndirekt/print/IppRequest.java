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
import java.util.List;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;

/**
 * @author sefftinge
 *
 */
public interface IppRequest
{

  IppResponse send() throws IOException;

  void setData(InputStream data);

  void setJobAttributes(PrintJobAttributeSet attributes);

  void addOperationAttributes(AttributeSet attributes);

  void setPrinterAttributes(AttributeSet attributes);
  
  void setRequestedAttributes(List<IppAttributeName> attributes);
}
