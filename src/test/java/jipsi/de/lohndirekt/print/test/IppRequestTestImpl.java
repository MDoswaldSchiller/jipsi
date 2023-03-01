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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.Compression;
import javax.print.attribute.standard.CopiesSupported;
import javax.print.attribute.standard.JobImpressionsSupported;
import javax.print.attribute.standard.JobKOctetsSupported;
import javax.print.attribute.standard.JobMediaSheetsSupported;
import javax.print.attribute.standard.JobPrioritySupported;
import javax.print.attribute.standard.JobSheets;
import javax.print.attribute.standard.MultipleDocumentHandling;
import javax.print.attribute.standard.NumberUpSupported;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.PDLOverrideSupported;
import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import javax.print.attribute.standard.Sides;
import jipsi.de.lohndirekt.print.IppRequest;
import jipsi.de.lohndirekt.print.IppResponse;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.Charset;
import jipsi.de.lohndirekt.print.attribute.ipp.NaturalLanguage;
import jipsi.de.lohndirekt.print.attribute.ipp.jobtempl.LdJobHoldUntil;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.PrinterDriverInstaller;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.CompressionSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.FinishingsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.JobHoldUntilSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.JobSheetsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.MediaSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.MultipleDocumentHandlingSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OrientationRequestedSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.PageRangesSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.PrinterUriSupported;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.SidesSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IppRequestTestImpl implements IppRequest
{

  private static final Logger LOG = LoggerFactory.getLogger(IppRequestTestImpl.class);

  class IppResponseTestImpl implements IppResponse
  {

    IppStatus status;
    AttributeMap attributes;

    IppResponseTestImpl()
    {

    }

    /**
     * @return
     */
    @Override
    public AttributeMap getAttributes()
    {
      return attributes;
    }

    /**
     * @return
     */
    @Override
    public IppStatus getStatus()
    {
      return status;
    }

  }

  private IppResponseTestImpl response;
  private InputStream data;
  private OperationsSupported operation;
  //Id wird in der Cups-API zwar übergeben, ist aber auch immer 1.
  private PrintJobAttributeSet jobAttributes = new HashPrintJobAttributeSet();
  private AttributeSet operationAttributes = new HashAttributeSet();
  private AttributeSet printerAttributes = new HashAttributeSet();

  /**
   * @param attrs
   */
  @Override
  public void setPrinterAttributes(AttributeSet attrs)
  {
    this.printerAttributes = attrs;
  }

  @Override
  public void setRequestedAttributes(List<IppAttributeName> attributes)
  {
  
  }
  
  /**
   * @param operation
   */
  public IppRequestTestImpl(URI path, OperationsSupported operation)
  {
    this.operation = operation;
    init();
  }

  /**
   *
   */
  private void init()
  {
    setStandardAttributes();
  }

  /**
   *
   */
  private void setStandardAttributes()
  {
    operationAttributes.add(Charset.ISO_8859_1);
    operationAttributes.add(NaturalLanguage.EN);
  }

  /**
   * @param attributes
   */
  @Override
  public void addOperationAttributes(AttributeSet attributes)
  {
    this.operationAttributes.addAll(attributes);
  }

  /**
   * @param data the data as input stream
   */
  @Override
  public void setData(InputStream data)
  {
    this.data = data;
  }

  /**
   * @param attributes
   */
  @Override
  public void setJobAttributes(PrintJobAttributeSet attributes)
  {
    this.jobAttributes = attributes;
  }

  @Override
  public IppResponse send() throws IOException
  {
    try {
      this.response = new IppResponseTestImpl();
      this.send(this.operation);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return response;
  }

  /**
   * @param operation
   */
  private void send(OperationsSupported operation) throws URISyntaxException
  {
    if (operation.equals(OperationsSupported.GET_PRINTER_ATTRIBUTES)) {
      sendGetPrinterAttributes();
    }
    else if (operation.equals(OperationsSupported.CUPS_GET_PRINTERS)) {
      sendCupsGetPrinter();
    }
    else {
      LOG.warn("Call to request {} not implemented", operation);
      this.response.attributes = new AttributeMap();
    }
    response.status = IppStatus.SUCCESSFUL_OK;
  }

  /**
   *
   */
  private void sendCupsGetPrinter() throws URISyntaxException
  {
    AttributeMap attributes = new AttributeMap();
    attributes.put(new PrinterUriSupported(new URI("http://127.0.0.1")));
    this.response.attributes = attributes;
  }

  /**
   *
   */
  private void sendGetPrinterAttributes() throws URISyntaxException
  {
    //
    //		- Printer attributes that are Job Template attributes ("xxx-
    //				default" "xxx-supported", and "xxx-ready" in the Table in
    //				Section 4.2),
    //			  - "pdl-override-supported",
    //			  - "compression-supported",
    //			  - "job-k-octets-supported",
    //			  - "job-impressions-supported",
    //			  - "job-media-sheets-supported",
    //			  - "printer-driver-installer",
    //			  - "color-supported", and
    //			  - "reference-uri-schemes-supported"

    AttributeMap attributes = new AttributeMap();
    attributes.put(PDLOverrideSupported.ATTEMPTED);
    attributes.put(PDLOverrideSupported.NOT_ATTEMPTED);
    attributes.put(new CompressionSupported(Compression.GZIP.toString(), Locale.getDefault()));
    attributes.put(new CompressionSupported(Compression.NONE.toString(), Locale.getDefault()));
    attributes.put(new JobKOctetsSupported(1, 10));
    attributes.put(new JobImpressionsSupported(1, 10));
    attributes.put(new JobMediaSheetsSupported(1, 10));
    attributes.put(new PrinterDriverInstaller(new URI("http://127.0.0.1")));
    attributes.put(ColorSupported.SUPPORTED);
    attributes.put(ReferenceUriSchemesSupported.HTTP);
    attributes.put(ReferenceUriSchemesSupported.FTP);

    //Attributes named in 4.2 of rfc2911
    attributes.put(new JobPrioritySupported(99));
    attributes.put(new JobHoldUntilSupported(new LdJobHoldUntil("12:00:00", Locale.getDefault()).toString(), Locale.getDefault()));
    attributes.put(new JobHoldUntilSupported(LdJobHoldUntil.THIRD_SHIFT.toString(), Locale.getDefault()));
    attributes.put(new JobSheetsSupported(JobSheets.NONE.toString(), Locale.getDefault()));
    attributes.put(new JobSheetsSupported(JobSheets.STANDARD.toString(), Locale.getDefault()));

    attributes.put(
        new MultipleDocumentHandlingSupported(
            MultipleDocumentHandling.SEPARATE_DOCUMENTS_COLLATED_COPIES.toString(),
            Locale.getDefault()));
    attributes.put(
        new MultipleDocumentHandlingSupported(
            MultipleDocumentHandling.SINGLE_DOCUMENT.toString(),
            Locale.getDefault()));

    attributes.put(new CopiesSupported(1, 100));

    attributes.put(new FinishingsSupported(1));
    attributes.put(new FinishingsSupported(2));

    attributes.put(PageRangesSupported.SUPPORTED);

    attributes.put(new SidesSupported(Sides.DUPLEX.toString(), Locale.getDefault()));
    attributes.put(new SidesSupported(Sides.TWO_SIDED_SHORT_EDGE.toString(), Locale.getDefault()));
    
    attributes.put(new NumberUpSupported(1, 10));
    attributes.put(new NumberUpSupported(100));
    
    attributes.put(new OrientationRequestedSupported(OrientationRequested.LANDSCAPE.getValue()));
    attributes.put(new OrientationRequestedSupported(OrientationRequested.PORTRAIT.getValue()));

    attributes.put(new MediaSupported("test", Locale.getDefault()));
    attributes.put(new MediaSupported("test2", Locale.getDefault()));

    //media-ready not implemented
    //printer-resolution-supported not implemented
    //print-quality-supported not implemented
    response.attributes = attributes;
  }

  private IppResponse getResponse() throws IOException
  {
    return this.response;
  }
}
