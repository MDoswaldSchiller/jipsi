package jipsi.de.lohndirekt.print.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.PrinterURI;
import javax.print.attribute.standard.RequestingUserName;
import jipsi.de.lohndirekt.print.IppRequest;
import jipsi.de.lohndirekt.print.IppRequestFactory;
import jipsi.de.lohndirekt.print.IppResponse;
import jipsi.de.lohndirekt.print.attribute.AttributeHelper;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.auth.RequestingUserPassword;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.DocumentFormatSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jipsi.de.lohndirekt.print.attribute.IppStatus.*;

/**
 *
 * @author mdo
 */
public class IppPrinter
{
  private static final Logger LOG = LoggerFactory.getLogger(IppPrinter.class);
  
  private final URI uri;
  private RequestingUserName requestingUserName;
  private RequestingUserPassword requestingUserPassword;

  /**
   * @param uri The uri to the IPP printer
   */
  public IppPrinter(URI uri)
  {
    this.uri = Objects.requireNonNull(uri);
  }

  public URI getUri()
  {
    return uri;
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

  public void setRequestingUserPassword(RequestingUserPassword requestingUserPassword)
  {
    this.requestingUserPassword = requestingUserPassword;
  }
  
  
  public Set<DocFlavor> getSupportedDocFlavors()
  {
    Set<DocumentFormatSupported> flavorAttributes = getAttributes(IppAttributeName.DOCUMENT_FORMAT_SUPORTED).get(IppAttributeName.DOCUMENT_FORMAT_SUPORTED.getCategory());

    return flavorAttributes.stream()
          .map(attr -> getDocFlavor(attr.getValue()))
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
  }
  
  public boolean isColorSupported()
  {
    Set<ColorSupported> colorSupported = getAttributes(IppAttributeName.COLOR_SUPPORTED).get(IppAttributeName.COLOR_SUPPORTED.getCategory());
    return colorSupported.stream()
            .anyMatch(attr -> attr == ColorSupported.SUPPORTED);
  }
  
  
  private DocFlavor getDocFlavor(String mimeType)
  {
    DocFlavor docFlavor = null;
    
    if (mimeType.equals(DocFlavor.BYTE_ARRAY.AUTOSENSE.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.GIF.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.GIF;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.JPEG.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.JPEG;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PCL.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.PCL;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PDF.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.PDF;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.POSTSCRIPT;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.PNG.getMimeType())) {
      docFlavor = DocFlavor.BYTE_ARRAY.PNG;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST.getMimeType().substring(0, 9))) {
      docFlavor = DocFlavor.BYTE_ARRAY.TEXT_HTML_HOST;
    }
    else if (mimeType.equals(DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST.getMimeType().substring(0, 10))) {
      docFlavor = DocFlavor.BYTE_ARRAY.TEXT_PLAIN_HOST;
    }
    return docFlavor;
  }
  
  public IppPreparedJob createJob(PrintRequestAttributeSet attributes) throws PrintException
  {
    IppRequest request = IppRequestFactory.createIppRequest(uri, OperationsSupported.CREATE_JOB, requestingUserName, requestingUserPassword);
    request.addOperationAttributes(new HashAttributeSet(new PrinterURI(this.uri)));
    request.addOperationAttributes(AttributeHelper.jobOperationAttributes(attributes));
    
    //set the job template attributes
    request.setJobAttributes(AttributeHelper.jobAttributes(attributes));
    try {
      IppResponse response = request.send();
      
      if (response == null) {
        throw new PrintException("No response from printer");
      }
      
      switch (response.getStatus()) {
        case SUCCESSFUL_OK:
        case SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES:
        case SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES:
          return new IppPreparedJob(this, IppJob.filterPrintJobAttributes(response.getAttributes()));
          
        default:
          throw new PrintException("Error in print-job: " + response.getStatus());
      }
    }
    catch (IOException e) {
      throw new PrintException("Error sending " + description(attributes) + " to IPP service: " + e.getMessage(), e);
    }
  }

  
  
  public IppSimpleJob printDocument(Doc doc, AttributeSet attributes) throws PrintException
  {
    IppRequest request = IppRequestFactory.createIppRequest(uri, OperationsSupported.PRINT_JOB, requestingUserName, requestingUserPassword);
    request.addOperationAttributes(new HashAttributeSet(new PrinterURI(this.uri)));
    request.addOperationAttributes(AttributeHelper.jobOperationAttributes(attributes));
    request.addOperationAttributes(AttributeHelper.docOperationAttributes(doc));
    
    try {
      request.setData(doc.getStreamForBytes());
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new PrintException("Error getting document data (" + description(attributes) + "): " + e.getMessage());
    }

    //set the job template attributes
    request.setJobAttributes(AttributeHelper.jobAttributes(attributes));
    try {
      IppResponse response = request.send();
      
      if (response == null) {
        throw new PrintException("No response from printer");
      }
      
      switch (response.getStatus()) {
        case SUCCESSFUL_OK:
        case SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES:
        case SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES:
          return new IppSimpleJob(this, IppJob.filterPrintJobAttributes(response.getAttributes()));
          
        default:
          throw new PrintException("Error in print-job: " + response.getStatus());
      }
    }
    catch (IOException e) {
      throw new PrintException("Error sending " + description(attributes) + " to IPP service: " + e.getMessage(), e);
    }
  }
  
  private String description(AttributeSet attributes)
  {
    String description = "job";
    if (attributes != null) {
      Attribute jobName = attributes.get(IppAttributeName.JOB_NAME.getCategory());
      if (jobName != null) {
        description += " " + jobName.toString();
      }
    }
    return description;
  }
  
  private AttributeMap getAttributes(IppAttributeName... attributes)
  {
    try {
      IppRequest request = IppRequestFactory.createIppRequest(uri, OperationsSupported.GET_PRINTER_ATTRIBUTES, requestingUserName, requestingUserPassword);
      request.addOperationAttributes(new HashAttributeSet(new PrinterURI(this.uri)));
      request.setRequestedAttributes(List.of(attributes));

      IppResponse response = request.send();
      if (response != null) {
        return response.getAttributes();
      }
    }
    catch (IOException ex) {
      LOG.error("Error calling Get-Printer-Attributes", ex);
    }
    
    return new AttributeMap();
  }
}
