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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.print.Doc;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrinterURI;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import jipsi.de.lohndirekt.print.attribute.AttributeHelper;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobId;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobUri;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch
 *
 */
class Job implements DocPrintJob
{
  private static final Logger LOG = LoggerFactory.getLogger(Job.class);

  protected final IppPrintService printService;
  protected boolean ok;
  protected JobUri jobUri;
  protected JobId jobId;
  private PrintJobAttributeSet jobAttributes;
  private List jobListeners;
  private Map attributeListeners;

  /**
   * @param service
   */
  public Job(IppPrintService service)
  {
    this.printService = Objects.requireNonNull(service);
  }

  /**
   *
   */
  @Override
  public PrintService getPrintService()
  {
    return this.printService;
  }

  /**
   *
   */
  @Override
  public PrintJobAttributeSet getAttributes()
  {
    if (jobId != null) {
      IppRequest getJobAttributes = IppRequestFactory.createIppRequest(printService.getUri(), OperationsSupported.GET_JOB_ATTRIBUTES, null, null);
      getJobAttributes.addOperationAttributes(new HashAttributeSet(new PrinterURI(printService.getUri())));
      getJobAttributes.addOperationAttributes(new HashAttributeSet(jobId));
      
      try {
        IppResponse response = getJobAttributes.send();
        return filterPrintJobAttributes(response.getAttributes());
      }
      catch (IOException ex) {
        LOG.error("Error while fetching job attributes", ex);
      }
    }
    
    return this.jobAttributes;
  }

  /**
   *
   */
  @Override
  public void print(Doc doc, PrintRequestAttributeSet attributes) throws PrintException
  {
    IppRequest request = null;
    request = this.printService.createRequest(OperationsSupported.PRINT_JOB);
    try {
      request.setData(doc.getStreamForBytes());
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new PrintException("Error getting document data (" + description(attributes) + "): " + e.getMessage());
    }
    //add the operation attributes
    AttributeSet operationAttributes = new HashAttributeSet();
    operationAttributes.addAll(AttributeHelper.jobOperationAttributes(attributes));
    operationAttributes.addAll(AttributeHelper.docOperationAttributes(doc));
    request.addOperationAttributes(operationAttributes);
    //set the job template attributes
    request.setJobAttributes(AttributeHelper.jobAttributes(attributes));
    IppResponse response = null;
    try {
      response = request.send();
      notifyJobListeners(PrintJobEvent.DATA_TRANSFER_COMPLETE);
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new PrintException("Error sending " + description(attributes) + " to IPP service: " + e.getMessage());
    }
    if (response != null && response.getStatus() != null) {
      AttributeMap responseAttributes = response.getAttributes();
      updateAttributes(responseAttributes);
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES)) {
        if (responseAttributes.containsCategory(IppAttributeName.JOB_URI.getCategory())) {
          Set<JobUri> jobUriList = responseAttributes.get(IppAttributeName.JOB_URI.getCategory());
          this.jobUri = jobUriList.iterator().next();
        }
        if (responseAttributes.containsCategory(IppAttributeName.JOB_ID.getCategory())) {
          Set<JobId> jobIdList = responseAttributes.get(IppAttributeName.JOB_ID.getCategory());
          this.jobId = jobIdList.iterator().next();
        }
        
        notifyJobListeners(PrintJobEvent.JOB_COMPLETE);
        this.ok = true;
      }
      else {
        notifyJobListeners(PrintJobEvent.JOB_FAILED);
        this.ok = false;
      }
    }
    else {
      notifyJobListeners(PrintJobEvent.JOB_FAILED);
      this.ok = false;
    }
    notifyJobListeners(PrintJobEvent.NO_MORE_EVENTS);
    if (!this.ok) {
      String msg = "Printing " + description(attributes) + "  failed";
      if (response != null && response.getStatus() != null) {
        msg += ": Server status was '" + response.getStatus().getId() + " - " + response.getStatus().getText() + "'!";
      }
      throw new PrintException(msg);
    }
  }

  @Override
  public void addPrintJobListener(PrintJobListener listener)
  {
    if (listener != null) {
      if (jobListeners == null) {
        jobListeners = new ArrayList();
      }
      jobListeners.add(listener);
    }
  }

  @Override
  public void removePrintJobListener(PrintJobListener listener)
  {
    if (listener != null) {
      jobListeners.remove(listener);
    }
  }

  @Override
  public void addPrintJobAttributeListener(PrintJobAttributeListener listener, PrintJobAttributeSet attributes)
  {
    if (listener != null) {
      if (attributeListeners == null) {
        attributeListeners = new HashMap();
      }
      attributeListeners.put(listener, attributes);
    }
  }

  @Override
  public void removePrintJobAttributeListener(PrintJobAttributeListener listener)
  {
    if (listener != null) {
      attributeListeners.remove(listener);
    }
  }

  void notifyJobListeners(int eventType)
  {
    if (jobListeners != null) {
      PrintJobEvent event = new PrintJobEvent(this, eventType);
      for (Iterator iter = jobListeners.iterator(); iter.hasNext();) {
        PrintJobListener listener = (PrintJobListener) iter.next();
        if (eventType == PrintJobEvent.DATA_TRANSFER_COMPLETE) {
          listener.printDataTransferCompleted(event);
        }
        else if (eventType == PrintJobEvent.JOB_CANCELED) {
          listener.printJobCanceled(event);
        }
        else if (eventType == PrintJobEvent.JOB_COMPLETE) {
          listener.printJobCompleted(event);
        }
        else if (eventType == PrintJobEvent.JOB_FAILED) {
          listener.printJobFailed(event);
        }
        else if (eventType == PrintJobEvent.NO_MORE_EVENTS) {
          listener.printJobNoMoreEvents(event);
        }
        else if (eventType == PrintJobEvent.REQUIRES_ATTENTION) {
          listener.printJobRequiresAttention(event);
        }
      }
    }
  }

  IppResponse sendRequest(OperationsSupported operation, AttributeSet operationAttributes) throws IOException
  {
    IppRequest request = this.request(operation);
    request.addOperationAttributes(operationAttributes);
    IppResponse response = null;
    response = request.send();
    return response;
  }

  private IppRequest request(OperationsSupported operation)
  {
    IppRequest request = IppRequestFactory.createIppRequest(this.jobUri.getURI(), operation, this.printService.getRequestingUserName(), this.printService.getRequestingUserPassword());
    AttributeSet operationAttributes = new HashAttributeSet();
    operationAttributes.add(this.jobUri);
    request.addOperationAttributes(operationAttributes);
    return request;
  }

  //  public methods which are not part of the JPS API
  /**
   *
   * This method returns the Job with the given JobId that is held by the given
   * PrintService.
   * <br>
   * This method might return a Job which is not hold by this PrintService but
   * the same CUPS server
   *
   * @param service
   * @param id
   * @return the corresponding Job wihth the given JobId, or null if no such Job
   * exists
   * @throws PrintException
   */
  static Job getJob(IppPrintService service, JobId id) throws PrintException
  {
    Job job = new Job(service);
    
    URI jobUri;
    try {
      jobUri
          = new URI(
              service.getUri().getScheme(),
              service.getUri().getAuthority(),
              "/jobs/" + id.getValue(),
              service.getUri().getQuery(),
              service.getUri().getFragment());
    }
    catch (URISyntaxException e) {
      throw new PrintException("Internal bug.", e);
    }
    job.jobUri = new JobUri(jobUri);
    try {
      job.updateAttributes();
    }
    catch (IllegalStateException e) {
      job = null;
    }
    return job;
  }

  private HashPrintJobAttributeSet filterPrintJobAttributes(AttributeMap responseAttributes)
  {
    HashPrintJobAttributeSet attributes = new HashPrintJobAttributeSet();
    for (var iter = responseAttributes.valueIterator(); iter.hasNext();) {
      Set<Attribute> values = iter.next();
      for (var listIter = values.iterator(); listIter.hasNext();) {
        Attribute attribute = listIter.next();
        if (attribute instanceof PrintJobAttribute) {
          attributes.add(attribute);
        }
      }
    }
    return attributes;
  }
  
  
  /**
   * @param responseAttributes
   */
  private void updateAttributes(AttributeMap responseAttributes)
  {
    this.jobAttributes = new HashPrintJobAttributeSet();
    for (var iter = responseAttributes.valueIterator(); iter.hasNext();) {
      Set<Attribute> values = iter.next();
      for (var listIter = values.iterator(); listIter.hasNext();) {
        Attribute attribute = listIter.next();
        if (attribute instanceof PrintJobAttribute) {
          this.jobAttributes.add(attribute);
        }
      }
    }
  }

  /**
   * Updates the Job's attributes to the current values.
   *
   * @throws PrintException
   */
  private void updateAttributes() throws PrintException
  {
    try {
      AttributeSet operationAttributes = new HashAttributeSet();
      IppResponse response = sendRequest(OperationsSupported.GET_JOB_ATTRIBUTES, operationAttributes);
      if (!response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          && !response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          && !response.getStatus().equals(IppStatus.SUCCESSFUL_OK_IGNORED_OR_SUBSTITUTED_ATTRIBUTES)) {
        if (response.getStatus().equals(IppStatus.CLIENT_ERROR_NOT_FOUND)) {
          throw new IllegalStateException("Job with uri '" + this.jobUri.toString() + "does not exist.");
        }
        throw new PrintException("Request not successful.");
      }
      AttributeMap attribsMap = response.getAttributes();
      updateAttributes(attribsMap);
    }
    catch (IOException e) {
      throw new PrintException("Update failed.", e);
    }
  }

  private String description(PrintRequestAttributeSet attributes)
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
}
