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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.print.Doc;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.DocumentName;
import jipsi.de.lohndirekt.print.SimpleMultiDoc;
import jipsi.de.lohndirekt.print.attribute.ipp.DocumentFormat;
import jipsi.de.lohndirekt.print.attribute.ipp.LastDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ld-development
 *
 *
 */
public final class AttributeHelper
{

  private final static Logger LOG = LoggerFactory.getLogger(AttributeHelper.class);

  /**
   * filters the given attributes
   *
   * @param attributes
   * @return only the attributes wich are of type <code>PrintJobAttribute</code>
   * and not operation attributes
   */
  public final static PrintJobAttributeSet jobAttributes(PrintRequestAttributeSet attributes)
  {
    PrintJobAttributeSet jobAttributes = new HashPrintJobAttributeSet();
    if (attributes != null) {
      AttributeSet invalidAttributes = jobOperationAttributes(attributes);
      Object[] attributeArray = attributes.toArray();
      for (int i = 0; i < attributeArray.length; i++) {
        Attribute attribute = (Attribute) attributeArray[i];
        //attributes-charset, attributes-natural-language etc. are not set by the user
        if (attribute instanceof PrintJobAttribute && !(invalidAttributes.containsValue(attribute))) {
          jobAttributes.add(attribute);
        }
      }
    }
    return jobAttributes;
  }

  /**
   * filters the given attributes
   *
   * @param attributes
   * @return only job-operation attributes
   */
  public final static AttributeSet jobOperationAttributes(PrintRequestAttributeSet attributes)
  {
    AttributeSet operationAttributes = new HashAttributeSet();
    if (attributes != null) {
      Object[] attributeArray = attributes.toArray();
      for (int i = 0; i < attributeArray.length; i++) {
        Attribute attribute = (Attribute) attributeArray[i];
        //attributes-charset, attributes-natural-language etc. are not set by the user
        if (attribute.getCategory().equals(IppAttributeName.JOB_NAME.getCategory())
            || attribute.getCategory().equals(IppAttributeName.DOCUMENT_FORMAT.getCategory())
            || attribute.getCategory().equals(IppAttributeName.FIDELITY.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_IMPRESSIONS.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_K_OCTETS.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_MEDIA_SHEETS.getCategory())
            || attribute.getCategory().equals(IppAttributeName.COMPRESSION.getCategory())
            || attribute.getCategory().equals(IppAttributeName.REQUESTING_USER_NAME.getCategory())
            || attribute.getCategory().equals(IppAttributeName.REQUESTING_USER_PASSWD.getCategory())) {
          operationAttributes.add(attribute);
        }
      }
    }
    return operationAttributes;
  }

  /**
   * @param multiDoc
   * @return a <code>List</code> with the document-format, document-name and
   * last-document
   */
  public final static AttributeSet docOperationAttributes(SimpleMultiDoc multiDoc)
  {
    AttributeSet operationAttributes = new HashAttributeSet();
    try {
      operationAttributes = docOperationAttributes(multiDoc.getDoc());
    }
    catch (IOException e) {
      LOG.error("Could not get Doc from multiDoc", e);
    }
    LastDocument lastDocument;
    if (multiDoc.isLast()) {
      lastDocument = LastDocument.TRUE;
    }
    else {
      lastDocument = LastDocument.FALSE;
    }
    operationAttributes.add(lastDocument);
    return operationAttributes;
  }

  /**
   * @param doc
   * @return a <code>List</code> with the document-format and document-name
   */
  public final static AttributeSet docOperationAttributes(Doc doc)
  {
    AttributeSet operationAttributes = new HashAttributeSet();
    if (doc.getAttributes() != null) {
      DocumentName docName = (DocumentName) doc.getAttributes().get(IppAttributeName.DOCUMENT_NAME.getCategory());
      if (docName != null) {
        operationAttributes.add(docName);
      }
    }
    operationAttributes.add(new DocumentFormat(doc.getDocFlavor().getMimeType(), Locale.getDefault()));
    return operationAttributes;
  }

  /**
   * @param operationAttributes2
   * @return
   */
  public final static Attribute[] getOrderedOperationAttributeArray(AttributeSet operationAttributes2)
  {
    return orderAttributeSet(operationAttributes2,
                             List.of(
                                 IppAttributeName.CHARSET.getCategory(),
                                 IppAttributeName.NATURAL_LANGUAGE.getCategory(),
                                 IppAttributeName.PRINTER_URI.getCategory(),
                                 IppAttributeName.JOB_ID.getCategory(),
                                 IppAttributeName.REQUESTING_USER_NAME.getCategory(),
                                 IppAttributeName.JOB_NAME.getCategory(),
                                 IppAttributeName.FIDELITY.getCategory(),
                                 IppAttributeName.DOCUMENT_NAME.getCategory(),
                                 IppAttributeName.DOCUMENT_FORMAT.getCategory()
                             ));
  }

  private static Attribute[] orderAttributeSet(AttributeSet attributes, List<Class<? extends Attribute>> categoryOrder)
  {
    AttributeSet copy = new HashAttributeSet(attributes);
    List<Attribute> orderedAttributes = new ArrayList<>(copy.size());
    
    for (Class<? extends Attribute> category : categoryOrder) {
      if (copy.containsKey(category)) {
        orderedAttributes.add(copy.get(category));
        copy.remove(category);
      }
    }
    
    for (Attribute attr : copy.toArray()) {
      orderedAttributes.add(attr);
    }
    
    return orderedAttributes.toArray(new Attribute[0]);
  }
  
}
