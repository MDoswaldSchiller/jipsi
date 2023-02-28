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
 */package jipsi.de.lohndirekt.print;

import java.io.IOException;
import javax.print.CancelablePrintJob;
import javax.print.PrintException;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.event.PrintJobEvent;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch
 *
 */
class CancelableJob extends Job implements CancelablePrintJob
{
  private static final Logger LOG = LoggerFactory.getLogger(CancelableJob.class);

  /**
   * @param service
   */
  public CancelableJob(IppPrintService service)
  {
    super(service);
  }

  @Override
  public void cancel() throws PrintException
  {
    if (!ok) {
      throw new PrintException("Job has not been sent. Cannot be canceled");
    }
    AttributeSet operationAttributes = new HashAttributeSet();
    operationAttributes.add(this.jobUri);
    try {
      IppResponse response
          = sendRequest(
              OperationsSupported.CANCEL_JOB,
              operationAttributes);
      if (response.getStatus().equals(IppStatus.SUCCESSFUL_OK)
          || response.getStatus().equals(
              IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)
          || response.getStatus().equals(
              IppStatus.SUCCESSFUL_OK_CONFLICTING_ATTRIBUTES)) {
        notifyJobListeners(PrintJobEvent.JOB_CANCELED);
      }
    }
    catch (IOException e) {
      LOG.error(e.getMessage(), e);
      throw new PrintException("Cannot be canceled: IOException", e);
    }
  }

}
