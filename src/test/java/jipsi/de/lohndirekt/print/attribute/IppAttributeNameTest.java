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

import junit.framework.TestCase;

/**
 * @author bpusch
 *
 *
 */
public class IppAttributeNameTest extends TestCase
{

  /**
   * Constructor for IppAttributeNameTest.
   *
   * @param name
   */
  public IppAttributeNameTest(String name)
  {
    super(name);
  }


  /*
     * Test for IppAttributeName get(String)
   */
  public void testGetString()
  {
    IppAttributeName attributeName = IppAttributeName.fromAttributeName("attribute-does-not-exist");
    assertNull("IppAttributeName null", attributeName);
  }
}
