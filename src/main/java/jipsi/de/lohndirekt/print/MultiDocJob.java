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
import java.util.Set;
import javax.print.DocFlavor;
import javax.print.MultiDoc;
import javax.print.MultiDocPrintJob;
import javax.print.PrintException;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobEvent;
import jipsi.de.lohndirekt.print.attribute.AttributeHelper;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobUri;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch
 *
 */
class MultiDocJob extends Job implements MultiDocPrintJob
{

  private static final Logger LOG = LoggerFactory.getLogger(MultiDocJob.class);

  /**
   *
   */
  protected MultiDocJob(IppMultiDocPrintService service)
  {
    super(service);
  }

  void processMultiDocEvent(MultiDocEvent event)
  {
    SimpleMultiDoc doc = (SimpleMultiDoc) event.getSource();
    sendDocument(doc);
  }

  /**
   *
   */
  @Override
  public void print(MultiDoc multiDoc, PrintRequestAttributeSet attributes) throws PrintException
  {
    SimpleMultiDoc multi = (SimpleMultiDoc) multiDoc;
    multi.addMultiDocListener(new MDListener());
    createJob(attributes);
    sendDocument(multi);
  }

  private void createJob(PrintRequestAttributeSet attributes)
  {
    IppRequest request = null;
    request = this.printService.createRequest(OperationsSupported.CREATE_JOB);
    //add the operationn attributes
    request.addOperationAttributes(AttributeHelper.jobOperationAttributes(attributes));
    request.setJobAttributes(AttributeHelper.jobAttributes(attributes));
    IppResponse response = null;
    try {
      response = request.send();
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    if (response != null) {
      AttributeMap responseAttributes = response.getAttributes();
      if (responseAttributes.containsCategory(IppAttributeName.JOB_URI.getCategory())) {
        Set<JobUri> jobUriSet = responseAttributes.get(IppAttributeName.JOB_URI.getCategory());
        this.jobUri = jobUriSet.iterator().next();
      }
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
      }
      else {
        notifyJobListeners(PrintJobEvent.JOB_FAILED);
      }
    }
    else {
      notifyJobListeners(PrintJobEvent.JOB_FAILED);
    }
  }

  protected void sendDocument(SimpleMultiDoc multiDoc)
  {
    IppRequest request = null;
    AttributeSet operationAttributes = new HashAttributeSet();
    request = this.request(OperationsSupported.SEND_DOCUMENT);
    IppResponse response = null;
    try {
      operationAttributes.addAll(AttributeHelper.docOperationAttributes(multiDoc));
      request.addOperationAttributes(operationAttributes);
      if (multiDoc.getDoc().getDocFlavor() instanceof DocFlavor.INPUT_STREAM) {
        DocAttributeSet set = multiDoc.getDoc().getAttributes();
        request.setData((InputStream) multiDoc.getDoc().getPrintData());
      }
      response = request.send();
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    if (response != null) {
      AttributeMap responseAttributes = response.getAttributes();
      if (responseAttributes.containsCategory(IppAttributeName.JOB_URI.getCategory())) {
        Set<JobUri> jobUriSet = responseAttributes.get(IppAttributeName.JOB_URI.getCategory());
        this.jobUri = jobUriSet.iterator().next();
      }
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
      }
      else {
        notifyJobListeners(PrintJobEvent.JOB_FAILED);
      }
    }
    else {
      notifyJobListeners(PrintJobEvent.JOB_FAILED);
    }
    if (multiDoc.isLast()) {
      releaseJob();
    }
  }

  /**
   *
   */
  private void releaseJob()
  {
    IppRequest request = this.request(OperationsSupported.RELEASE_JOB);
    IppResponse response = null;
    try {
      response = request.send();

    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
    }
    if (response != null) {
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
        notifyJobListeners(PrintJobEvent.JOB_COMPLETE);
      }
      else {
        notifyJobListeners(PrintJobEvent.JOB_FAILED);
      }
    }
    else {
      notifyJobListeners(PrintJobEvent.JOB_FAILED);
    }
  }

  private IppRequest request(OperationsSupported operation)
  {
    IppRequest request = IppRequestFactory.createIppRequest(this.jobUri.getURI(), operation, this.printService.getRequestingUserName(), this.printService.getRequestingUserPassword());
    AttributeSet operationAttributes = new HashAttributeSet();
    operationAttributes.add(new JobUri(this.jobUri.getURI()));
    request.addOperationAttributes(operationAttributes);
    return request;
  }

  private class MDListener implements MultiDocListener
  {

    @Override
    public void processEvent(MultiDocEvent event) throws IOException
    {
      LOG.debug("MultiDocevent");
      processMultiDocEvent(event);
    }
  }
}
