package jipsi.de.lohndirekt.print.api;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.standard.JobState;
import javax.print.attribute.standard.PrinterURI;
import jipsi.de.lohndirekt.print.IppRequest;
import jipsi.de.lohndirekt.print.IppRequestFactory;
import jipsi.de.lohndirekt.print.IppResponse;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobId;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.JobUri;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mdo
 */
public abstract class IppJob
{
  private static final Logger LOG = LoggerFactory.getLogger(IppJob.class);

  private final IppPrinter printer;
  private final JobId jobId;
  private final JobUri jobUri;

  IppJob(IppPrinter printer, HashPrintJobAttributeSet jobAttributes)
  {
    this.printer = Objects.requireNonNull(printer);
    this.jobId = (JobId) jobAttributes.get(JobId.class);
    this.jobUri = (JobUri) jobAttributes.get(JobUri.class);

    if (jobId == null && jobUri == null) {
      throw new IllegalArgumentException("JobId or JobUri must be available");
    }
  }
  
  public JobId getJobId()
  {
    return jobId;
  }

  public JobState getStatus()
  {
    IppRequest jobAttributeRequest = IppRequestFactory.createIppRequest(printer.getUri(), OperationsSupported.GET_JOB_ATTRIBUTES, printer.getRequestingUserName(), null);
    addJobReference(jobAttributeRequest);
    jobAttributeRequest.setRequestedAttributes(List.of(IppAttributeName.JOB_ID, 
            IppAttributeName.JOB_IMPRESSIONS_COMPLETED,
            IppAttributeName.JOB_MEDIA_SHEETS_COMPLETED,
            IppAttributeName.JOB_NAME,
            IppAttributeName.JOB_ORIGINATING_USER_NAME,
            IppAttributeName.JOB_STATE,
            IppAttributeName.JOB_STATE_REASONS));

    try {
      IppResponse response = jobAttributeRequest.send();
      HashPrintJobAttributeSet jobAttributes = filterPrintJobAttributes(response.getAttributes());
      
      JobState jobState = (JobState)jobAttributes.get(JobState.class);
      if (jobState != null) {
        return jobState;
      }
    }
    catch (IOException ex) {
      LOG.error("Error while fetching job attributes", ex);
    }
    
    return JobState.UNKNOWN;
  }

  /**
   *  Send a {@link OperationsSupported#CANCEL_JOB} command to the printer
   * to cancel the job that is represented by this job object.
   * 
   * @return {@code true} if the printer returns a success message
   */
  public boolean cancel()
  {
    IppRequest cancelJobRequest = IppRequestFactory.createIppRequest(printer.getUri(), OperationsSupported.CANCEL_JOB, printer.getRequestingUserName(), null);
    addJobReference(cancelJobRequest);

    try {
      IppResponse response = cancelJobRequest.send();

      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
        return true;
      }
    }
    catch (IOException ex) {
      LOG.error(ex.getMessage(), ex);
    }
    
    return false;
  }
  
  /**
   * Send a {@link OperationsSupported#CANCEL_CURRENT_JOB} command to the printer
   * to cancel the current job. Note that this command will send the job-id of
   * this job to make sure that the current job is the job represented by this
   * job object.
   * 
   * @return {@code true} if the printer returns a success message
   */
  public boolean cancelCurrentJob()
  {
    IppRequest cancelCurrentJobRequest = IppRequestFactory.createIppRequest(printer.getUri(), OperationsSupported.CANCEL_CURRENT_JOB, printer.getRequestingUserName(), null);
    addJobReference(cancelCurrentJobRequest);

    try {
      IppResponse response = cancelCurrentJobRequest.send();

      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
        return true;
      }
    }
    catch (IOException ex) {
      LOG.error(ex.getMessage(), ex);
    }
    
    return false;
  }
  
  
  private void addJobReference(IppRequest request)
  {
    if (jobId != null) {
      request.addOperationAttributes(new HashAttributeSet(new PrinterURI(printer.getUri())));
      request.addOperationAttributes(new HashAttributeSet(jobId));
    }
    else {
      request.addOperationAttributes(new HashAttributeSet(jobUri));
    }
  }
  
  static HashPrintJobAttributeSet filterPrintJobAttributes(AttributeMap responseAttributes)
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
}
