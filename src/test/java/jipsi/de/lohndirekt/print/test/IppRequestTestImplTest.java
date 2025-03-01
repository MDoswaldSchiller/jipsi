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
package jipsi.de.lohndirekt.print.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import javax.print.attribute.Attribute;
import jipsi.de.lohndirekt.print.IppRequest;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.ipp.UnknownAttribute;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import junit.framework.TestCase;

/**
 * @author bpusch
 *
 *
 */
public class IppRequestTestImplTest extends TestCase
{

  public void testSendGetPrinterAttributes() throws URISyntaxException, IOException
  {
    Class unknownAttributeCategory = new UnknownAttribute("x", new Object[0]).getCategory();
    IppRequest request = new IppRequestTestImpl(new URI("http://127.0.0.1"), OperationsSupported.GET_PRINTER_ATTRIBUTES);
    AttributeMap attributes = request.send().getAttributes();
    for (Iterator<Class<? extends Attribute>> iter = attributes.keyIterator(); iter.hasNext();) {
      Class<? extends Attribute> category = iter.next();
      //Should not return any unknown Attributes
      assertFalse(category.equals(unknownAttributeCategory));
      Set<? extends Attribute> attrs = attributes.get(category);
      assertNotNull(attrs);
      for (Attribute element : attrs) {
        assertEquals(category, element.getCategory());
      }
    }
  }

  public void testSendCupsGetPrinter() throws URISyntaxException, IOException
  {
    IppRequest request = new IppRequestTestImpl(new URI("http://127.0.0.1"), OperationsSupported.CUPS_GET_PRINTERS);
    AttributeMap attributes = request.send().getAttributes();
    assertTrue("Response must contain an attribute of category printer-uri-spported", attributes.containsCategory(IppAttributeName.PRINTER_URI_SUPPORTED.getCategory()));
  }

  public void testGetResponse() throws URISyntaxException, IOException
  {
    //response is now returned by send()
  }

}
