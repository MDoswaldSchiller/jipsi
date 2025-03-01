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
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.ServiceUIFactory;
import javax.print.attribute.*;
import javax.print.attribute.standard.PrinterURI;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.event.PrintServiceAttributeListener;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import jipsi.de.lohndirekt.print.attribute.ipp.DocumentFormat;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobId;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.DocumentFormatSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported.GET_PRINTER_ATTRIBUTES;

/**
 * @author bpusch
 *
 *
 */
public class IppPrintService implements PrintService
{
  private static final Logger LOG = LoggerFactory.getLogger(IppPrintService.class);
  
  private final URI uri;
  private DocFlavor[] supportedFlavors;
  private RequestingUserName requestingUserName;
  private RequestingUserPassword requestingUserPassword;
  private AttributeMap attributes;

  /**
   * @param uri
   */
  public IppPrintService(URI uri)
  {
    this.uri = Objects.requireNonNull(uri);
  }

  /**
   * @return
   */
  URI getUri()
  {
    return this.uri;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getName()
   */
  @Override
  public String getName()
  {
    return uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
  }

  /**
   *
   */
  public void reset()
  {
    supportedFlavors = null;
    attributes = null;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#createPrintJob()
   */
  @Override
  public DocPrintJob createPrintJob()
  {
    return new CancelableJob(this);
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getAttributes()
   */
  @Override
  public PrintServiceAttributeSet getAttributes()
  {
    PrintServiceAttributeSet set = new HashPrintServiceAttributeSet();
    for (Iterator<Set<Attribute>> valueIter = getAllAttributes().valueIterator(); valueIter.hasNext();) {
      Set<Attribute> values = valueIter.next();
      for (var listIter = values.iterator(); listIter.hasNext();) {
        Attribute attribute = listIter.next();
        if (attribute instanceof PrintServiceAttribute) {
          set.add(attribute);
        }
      }
    }
    return set;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getAttribute(java.lang.Class)
   */
  @Override
  public PrintServiceAttribute getAttribute(Class category)
  {
    Objects.requireNonNull(category, "category must not be null");
    
    //TODO As CUPS seems not to support this operation-tag (requested-attributes), we need to get all attributes
    Set<Attribute> attr = getAllAttributes().get(category);
    if (!attr.isEmpty()) {
      try {
        return (PrintServiceAttribute) attr.iterator().next();
      }
      catch (ClassCastException e) {
        throw new IllegalArgumentException("category must be a Class that implements interface PrintServiceAttribute");
      }
    }
    return null;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getSupportedDocFlavors()
   */
  @Override
  public DocFlavor[] getSupportedDocFlavors()
  {
    if (supportedFlavors == null) {
      Set<DocumentFormatSupported> flavorAttributes = getAllAttributes().get(IppAttributeName.DOCUMENT_FORMAT_SUPORTED.getCategory());

      supportedFlavors = flavorAttributes.stream()
          .map(attr -> getDocFlavor(attr.getValue()))
          .filter(Objects::nonNull)
          .collect(Collectors.toList())
          .toArray(new DocFlavor[0]);
    }
    return supportedFlavors;
  }
  
  private DocFlavor getDocFlavor(String mimeType)
  {
    DocFlavor docFlavor = null;
    
    if (mimeType.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.GIF.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.GIF;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.JPEG.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.JPEG;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PCL.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.PCL;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PDF.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.PDF;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.POSTSCRIPT;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PNG.getMimeType())) {
      docFlavor = DocFlavor.INPUT_STREAM.PNG;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST.getMimeType().substring(0, 9))) {
      docFlavor = DocFlavor.INPUT_STREAM.TEXT_HTML_HOST;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.getMimeType().substring(0, 10))) {
      docFlavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
    }
    return docFlavor;
  }
  
  

  /* (non-Javadoc)
	 * @see javax.print.PrintService#isDocFlavorSupported(javax.print.DocFlavor)
   */
  @Override
  public boolean isDocFlavorSupported(DocFlavor flavor)
  {
    return Arrays.asList(getSupportedDocFlavors()).contains(flavor);
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getSupportedAttributeCategories()
   */
  @Override
  public Class[] getSupportedAttributeCategories()
  {
    Set supportedCategories = new HashSet();
    //Attributes named in 4.2 of rfc2911 are optional
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_PRIORIY_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_PRIORIY.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_HOLD_UNTIL_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_HOLD_UNTIL.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_SHEETS_SUPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_SHEETS.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.COPIES_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.COPIES.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.FINISHINGS_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.FINISHINGS.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.PAGE_RANGES_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.PAGE_RANGES.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.SIDES_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.SIDES.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.NUMBER_UP_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.NUMBER_UP.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.ORIENTATION_REQUESTED_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.ORIENTATION_REQUESTED.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.MEDIA_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.MEDIA.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.PRINTER_RESOLUTION.getCategory())) {
      //printer-resolution-supported attribute currently not implemented
    }
    if (getAllAttributes().containsCategory(IppAttributeName.PRINT_QUALITY.getCategory())) {
      //print-quality-supported attribute currently not implemented
    }
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_K_OCTETS_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_K_OCTETS.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_IMPRESSIONS_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_IMPRESSIONS.getCategory());
    }
    if (getAllAttributes().containsCategory(IppAttributeName.JOB_MEDIA_SHEETS_SUPPORTED.getCategory())) {
      supportedCategories.add(IppAttributeName.JOB_MEDIA_SHEETS.getCategory());
    }
    //Printer object MUST support compression attribute
    supportedCategories.add(IppAttributeName.COMPRESSION.getCategory());
    Class[] categories = new Class[supportedCategories.size()];
    categories = (Class[]) supportedCategories.toArray(categories);
    return categories;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#isAttributeCategorySupported(java.lang.Class)
   */
  @Override
  public boolean isAttributeCategorySupported(Class category)
  {
    return Arrays.asList(this.getSupportedAttributeCategories()).contains(category);
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getDefaultAttributeValue(java.lang.Class)
   */
  @Override
  public Object getDefaultAttributeValue(Class category)
  {
    //Only the attributes listed in rfc2911 4.2(Job Template Attributes) make sense here
    Object value = null;
    if (category.equals(IppAttributeName.JOB_PRIORIY.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.JOB_PRIORITY_DEFAULT.getCategory());
      if (attr != null) {
        value = ((IntegerSyntax)attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.JOB_HOLD_UNTIL.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.JOB_HOLD_UNTIL.getCategory());
      if (attr != null) {
        value = ((TextSyntax)attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.JOB_SHEETS_DEFAULT.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.JOB_SHEETS_DEFAULT.getCategory());
      if (attr != null) {
        value = ((TextSyntax)attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.COPIES.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.COPIES_DEFAULT.getCategory());
      if (attr != null) {
        value = ((IntegerSyntax) attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.FINISHINGS.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.FINISHINGS_DEFAULT.getCategory());
      if (attr != null) {
        value = ((IntegerSyntax) attr).toString();
      }
    }
    else if (category.equals(IppAttributeName.SIDES.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.SIDES_DEFAULT.getCategory());
      if (attr != null) {
        value = ((TextSyntax) attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.NUMBER_UP.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.NUMBER_UP_DEFAULT.getCategory());
      if (attr != null) {
        value = ((IntegerSyntax) attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.ORIENTATION_REQUESTED.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.ORIENTATION_REQUESTED_DEFAULT.getCategory());
      if (attr != null) {
        value = ((TextSyntax) attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.MEDIA.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.MEDIA_DEFAULT.getCategory());
      if (attr != null) {
        value = ((TextSyntax) attr).getValue();
      }
    }
    else if (category.equals(IppAttributeName.PRINTER_RESOLUTION.getCategory())) {
      //Cups does not support the printer-resolution-default attribute
    }
    else if (category.equals(IppAttributeName.PRINT_QUALITY.getCategory())) {
      //Cups does not support the print-quality-default attribute
    }
    else if (category.equals(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING.getCategory())) {
      Attribute attr = getAttribute(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING_DEFAULT.getCategory());
      if (attr != null) {
        value = ((TextSyntax) attr).getValue();
      }
    }
    return value;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getSupportedAttributeValues(java.lang.Class, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
   */
  @Override
  public Object getSupportedAttributeValues(Class category, DocFlavor flavor, AttributeSet attributes)
  {
    Set<Attribute> supportedAttributes = new HashSet();
    // Only the attributes listed in rfc2911 4.2(Job Template Attributes) do make sense here
    if (category.equals(IppAttributeName.JOB_PRIORIY.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_PRIORIY_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.JOB_HOLD_UNTIL.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_HOLD_UNTIL_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.JOB_SHEETS.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_SHEETS_SUPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.COPIES.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.COPIES_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.FINISHINGS.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.FINISHINGS_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.SIDES.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.SIDES_SUPPORTED.getCategory());
    }
    //TODO page-ranges
    else if (category.equals(IppAttributeName.NUMBER_UP.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.NUMBER_UP_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.ORIENTATION_REQUESTED.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.ORIENTATION_REQUESTED_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.MEDIA.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.MEDIA_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.PRINTER_RESOLUTION.getCategory())) {
      //printer-resolution-supported attribute currently not implemented
    }
    else if (category.equals(IppAttributeName.PRINT_QUALITY.getCategory())) {
      //printer-quality-supported attribute currently not implemented
    }
    else if (category.equals(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.MULTIPLE_DOCUMENT_HANDLING_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.JOB_IMPRESSIONS.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_IMPRESSIONS_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.JOB_K_OCTETS.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_K_OCTETS_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.JOB_MEDIA_SHEETS.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.JOB_MEDIA_SHEETS_SUPPORTED.getCategory());
    }
    else if (category.equals(IppAttributeName.COMPRESSION.getCategory())) {
      supportedAttributes = getAllAttributes().get(IppAttributeName.COMPRESSION_SUPORTED.getCategory());
    }
    if (supportedAttributes == null) {
      supportedAttributes = new HashSet<>();
    }
    return supportedAttributes.toArray(new Attribute[0]);
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#isAttributeValueSupported(javax.print.attribute.Attribute, javax.print.DocFlavor, javax.print.attribute.AttributeSet)
   */
  @Override
  public boolean isAttributeValueSupported(Attribute attrVal, DocFlavor flavor, AttributeSet attributes)
  {
    AttributeSet operationAttributes = new HashAttributeSet();
    if (flavor != null) {
      operationAttributes.add(new DocumentFormat(flavor.getMimeType(), Locale.getDefault()));
    }
    IppRequest request = this.createRequest(OperationsSupported.VALIDATE_JOB);
    if (attributes == null) {
      attributes = new HashAttributeSet();
    }
    attributes.add(attrVal);
    Object[] attributeArray = attributes.toArray();
    PrintJobAttributeSet jobAttributes = new HashPrintJobAttributeSet();
    for (int i = 0; i < attributeArray.length; i++) {
      Attribute attribute = (Attribute) attributeArray[i];
      //attributes-charset, attributes-natural-language etc. are not set by the user
      if (attribute instanceof PrintRequestAttribute && attribute instanceof PrintJobAttribute) {
        if (attribute.getCategory().equals(IppAttributeName.JOB_NAME.getCategory())
            || attribute.getCategory().equals(IppAttributeName.FIDELITY.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_IMPRESSIONS.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_K_OCTETS.getCategory())
            || attribute.getCategory().equals(IppAttributeName.JOB_MEDIA_SHEETS.getCategory())) {
          operationAttributes.add(attribute);
        }
        else if (attribute.getCategory().equals(IppAttributeName.JOB_NAME.getCategory())
                 || attribute.getCategory().equals(IppAttributeName.FIDELITY.getCategory())) {
          //do nothing, Job Template Attributes can not be used in a Validate-Job operation
        }
        else {
          jobAttributes.add(attribute);
        }
      }
    }
    request.addOperationAttributes(operationAttributes);
    request.setJobAttributes(jobAttributes);
    IppResponse response = null;
    try {
      response = request.send();
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    boolean supported = false;
    if (response != null) {
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
        supported = true;
      }
    }
    return supported;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getUnsupportedAttributes(javax.print.DocFlavor, javax.print.attribute.AttributeSet)
   */
  @Override
  public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes)
  {
    return null;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#getServiceUIFactory()
   */
  @Override
  public ServiceUIFactory getServiceUIFactory()
  {
    return null;
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#addPrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
   */
  @Override
  public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener)
  {
  }

  /* (non-Javadoc)
	 * @see javax.print.PrintService#removePrintServiceAttributeListener(javax.print.event.PrintServiceAttributeListener)
   */
  @Override
  public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener)
  {
  }

  /**
   * @return
   */
  private AttributeMap getAllAttributes()
  {
    if (attributes == null) {
      try {
        IppResponse response = createRequest(GET_PRINTER_ATTRIBUTES).send();
        if (response != null) {
          attributes = response.getAttributes();
        }
      }
      catch (IOException ex) {
        LOG.error("Error calling Get-Printer-Attributes", ex);
      }
    }
    return attributes;
  }

  @Override
  public String toString()
  {
    return this.getName();
  }

  /**
   * @param operation
   * @return
   */
  protected IppRequest createRequest(OperationsSupported operation)
  {
    IppRequest request = IppRequestFactory.createIppRequest(this.uri, operation, this.getRequestingUserName(), this.getRequestingUserPassword());
    AttributeSet operationAttributes = new HashAttributeSet();
    operationAttributes.add(new PrinterURI(this.uri));
    request.addOperationAttributes(operationAttributes);
    return request;
  }

  /**
   * Returns a job with the given JobId, or null if no such Job exists.
   * <br>
   * Use jobExists to check for a Job with this JobId.
   * <br>
   * This method might return a Job which is not hold by this PrintService but
   * the same CUPS server
   *
   * @param jobId
   * @return the corresponding Job wihth the given JobId, or null if no such Job
   * exists
   * @throws PrintException
   */
  //public Methods which are not part of the JPS API
  public DocPrintJob getJob(JobId jobId) throws PrintException
  {
    Job job = Job.getJob(this, jobId);
    return job;
  }

  /**
   *
   * Check for a Job with the given JobId.
   * <br>
   * This method might return true, even if the printservice does not hold any
   * job with the given JobId, but another PrintService on the same Cups server
   * does.
   *
   * @param jobId
   * @return true if a Job with the given JobId exists, false otherwise
   * @throws PrintException
   */
  public boolean jobExists(JobId jobId) throws PrintException
  {
    return Job.getJob(this, jobId) != null;
  }

  public RequestingUserName getRequestingUserName()
  {
    return requestingUserName;
  }

  public void setRequestingUserName(RequestingUserName requestingUserName)
  {
    this.requestingUserName = requestingUserName;
  }

  public RequestingUserPassword getRequestingUserPassword()
  {
    return requestingUserPassword;
  }

  public void setRequestingUserPassword(
      RequestingUserPassword requestingUserPassword)
  {
    this.requestingUserPassword = requestingUserPassword;
  }
}
