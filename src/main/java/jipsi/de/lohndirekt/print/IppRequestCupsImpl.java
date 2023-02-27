/**
 * Copyright (C) 2004 <a href="http://www.lohndirekt.de/">lohndirekt.de</a>
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import javax.print.attribute.Attribute;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashAttributeSet;
import javax.print.attribute.HashPrintJobAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.TextSyntax;
import jipsi.de.lohndirekt.print.attribute.AttributeHelper;
import jipsi.de.lohndirekt.print.attribute.AttributeMap;
import jipsi.de.lohndirekt.print.attribute.AttributeParser;
import jipsi.de.lohndirekt.print.attribute.AttributeWriter;
import jipsi.de.lohndirekt.print.attribute.IppAttributeName;
import jipsi.de.lohndirekt.print.attribute.IppDelimiterTag;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.Charset;
import jipsi.de.lohndirekt.print.attribute.ipp.NaturalLanguage;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch, speters, sefftinge
 *
 */
class IppRequestCupsImpl implements IppRequest
{

  static class IppResponseImpl implements IppResponse
  {

    private static final Logger LOG = LoggerFactory.getLogger(IppResponseImpl.class);

    private IppStatus status;
    private AttributeMap attributes;

    IppResponseImpl(InputStream response)
    {
      try {
        parseResponse(response);
      }
      catch (IOException e) {
        LOG.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }

    }

    private void parseResponse(InputStream response) throws IOException
    {
      byte[] header = new byte[8];
      response.read(header);
      this.status = IppStatus.fromStatusId((int) (header[2] << 8) + (int) header[3]);

      if (response.available() != 0) {
        this.attributes = new AttributeParser().parseResponse(response);
      }
      else {
        this.attributes = new AttributeMap();
      }
      LOG.debug("Status: {}", status.getText());
    }

    @Override
    public AttributeMap getAttributes()
    {
      return attributes;
    }

    @Override
    public IppStatus getStatus()
    {
      return status;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(IppRequestCupsImpl.class);
  private static final int SEND_REQUEST_COUNT = 1;
  private static final int SEND_REQUEST_TIMEOUT = 50;
  private static final NaturalLanguage NATURAL_LANGUAGE_DEFAULT = NaturalLanguage.EN;
  private static final Charset CHARSET_DEFAULT = Charset.UTF_8;

  private IppConnection conn;
  private boolean sent;
  private InputStream data;

  private final int id;
  private PrintJobAttributeSet jobAttributes = new HashPrintJobAttributeSet();
  private AttributeSet operationAttributes = new HashAttributeSet();
  private AttributeSet printerAttributes = new HashAttributeSet();

  private final URI path;
  private final OperationsSupported operation;

  private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);
  
  /**
   * @param operation
   */
  IppRequestCupsImpl(URI path, OperationsSupported operation)
  {
    this.path = Objects.requireNonNull(path);
    this.operation = Objects.requireNonNull(operation);
    this.id = ID_COUNTER.addAndGet(1);
    setStandardAttributes();
  }

  private void setStandardAttributes()
  {
    operationAttributes.add(CHARSET_DEFAULT);
    operationAttributes.add(NATURAL_LANGUAGE_DEFAULT);
  }
  
  /**
   * @param printerAttributes
   */
  @Override
  public void setPrinterAttributes(AttributeSet attrs)
  {
    this.printerAttributes = attrs;
  }

  /**
   *
   */
  private byte[] ippFooter()
  {
    byte[] footer = new byte[1];
    footer[0] = (byte) IppDelimiterTag.END_ATTRIBUTES.getValue();
    return footer;
  }

  /**
   *
   */
  private byte[] ippAttributes() throws UnsupportedEncodingException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    
    serializeAttributes(IppDelimiterTag.BEGIN_OPERATION_ATTRIBUTES, AttributeHelper.getOrderedOperationAttributeArray(operationAttributes), out);
    serializeAttributes(IppDelimiterTag.BEGIN_PRINTER_ATTRIBUTES, printerAttributes.toArray(), out);
    serializeAttributes(IppDelimiterTag.BEGIN_JOB_ATTRIBUTES, jobAttributes.toArray(), out);
    
    return out.toByteArray();
  }
  
  private void serializeAttributes(IppDelimiterTag beginTag, Attribute[] attributes, ByteArrayOutputStream out) throws UnsupportedEncodingException
  {
    if (attributes.length > 0) {
      out.write((byte)beginTag.getValue());

      for (Attribute attribute : attributes) {
        AttributeWriter.attributeBytes(attribute, out);
      }
    }
  }
  

  /**
   *
   */
  private byte[] ippHeader()
  {
    //Ipp header data according to http://www.ietf.org/rfc/rfc2910.txt
    ByteArrayOutputStream out = new ByteArrayOutputStream(8);
    //The first 2 bytes represent the IPP version number (1.1)
    //major version-number
    out.write((byte) 2);
    //minor version-number
    out.write((byte) 0);
    //2 byte operation id
    AttributeWriter.writeInt2(this.operation.getValue(), out);
    //4 byte request id
    AttributeWriter.writeInt4(this.id, out);
    return out.toByteArray();
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
   * @param stream
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

  /**
   * @see de.lohndirekt.print.IppRequest#send()
   * @throws IllegalArgumentException when called twice
   */
  @Override
  public IppResponse send() throws IOException
  {
    if (sent) {
      throw new IllegalStateException("Send must not be called twice");
    }

    Vector<InputStream> v = new Vector<>();
    v.add(new ByteArrayInputStream(this.ippHeader()));
    v.add(new ByteArrayInputStream(this.ippAttributes()));
    v.add(new ByteArrayInputStream(this.ippFooter()));
    if (data != null) {
      v.add(data);
    }
    SequenceInputStream stream = new SequenceInputStream(v.elements());
    conn = new IppHttpConnection(path, findUserName(), findPassword());

    boolean ok = false;
    int tries = 0;

    do {
      try {
        conn.send(stream);
      }
      catch (IOException ex) {
        LOG.error("Error communicating", ex);
      }

      if (conn.getStatusCode() != HttpURLConnection.HTTP_OK) {
        if (LOG.isInfoEnabled()) {
          String msg = "Cups seems to be busy - STATUSCODE " + conn.getStatusCode();
          if (tries < SEND_REQUEST_COUNT) {
            msg += " - going to retry in " + SEND_REQUEST_TIMEOUT + " ms";
          }
          LOG.info(msg);
        }
        try {
          Thread.sleep(SEND_REQUEST_TIMEOUT);
        }
        catch (InterruptedException e) {
          LOG.info("Send interrupted", e);
        }
      }
      else {
        ok = true;
      }
      tries++;
    }
    while (!ok && tries < SEND_REQUEST_COUNT);

    sent = true;
    return getResponse();
  }

  /**
   * @param list
   * @return
   */
  private String findUserName()
  {
    TextSyntax attr = (TextSyntax) operationAttributes.get(IppAttributeName.REQUESTING_USER_NAME.getCategory());
    if (attr != null) {
      return attr.getValue();
    }
    return null;
  }

  /**
   * @param list
   * @return
   */
  private String findPassword()
  {
    TextSyntax attr = (TextSyntax) operationAttributes.get(IppAttributeName.REQUESTING_USER_PASSWD.getCategory());
    if (attr != null) {
      return attr.getValue();
    }
    return null;
  }

  private IppResponse getResponse() throws IOException
  {
    if (conn.getStatusCode() == HttpURLConnection.HTTP_OK) {
      return new IppResponseImpl(conn.getIppResponse());
    }
    else {
      return null;
    }
  }
}
