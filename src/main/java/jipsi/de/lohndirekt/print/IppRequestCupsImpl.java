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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Objects;
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
import jipsi.de.lohndirekt.print.attribute.IppIoUtils;
import jipsi.de.lohndirekt.print.attribute.IppStatus;
import jipsi.de.lohndirekt.print.attribute.ipp.Charset;
import jipsi.de.lohndirekt.print.attribute.ipp.NaturalLanguage;
import jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported.OperationsSupported;

/**
 * @author bpusch, speters, sefftinge
 *
 */
public class IppRequestCupsImpl implements IppRequest
{
  private static final NaturalLanguage NATURAL_LANGUAGE_DEFAULT = NaturalLanguage.EN_US;
  private static final Charset CHARSET_DEFAULT = Charset.UTF_8;
  private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

  private final int id;
  private final URI path;
  private final OperationsSupported operation;
  
  private InputStream data;
  private PrintJobAttributeSet jobAttributes = new HashPrintJobAttributeSet();
  private AttributeSet operationAttributes = new HashAttributeSet();
  private AttributeSet printerAttributes = new HashAttributeSet();
  private List<IppAttributeName> requestedAttributes;
  
  /**
   * @param operation
   */
  public IppRequestCupsImpl(URI path, OperationsSupported operation)
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
   * @param attributes
   */
  @Override
  public void addOperationAttributes(AttributeSet attributes)
  {
    this.operationAttributes.addAll(attributes);
  }

  @Override
  public void setRequestedAttributes(List<IppAttributeName> requestedAttributes)
  {
    this.requestedAttributes = requestedAttributes;
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
    IppConnection conn = new IppHttpConnection(path, findUserName(), findPassword());
    IppConnection.IppConnectionResponse connectionResponse = conn.send(new ByteArrayInputStream(buildRequestBuffer()));
      
    if (connectionResponse.getStatusCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException("Cups seems to be busy - STATUSCODE " + connectionResponse.getStatusCode());
    }

    return parseResponse(connectionResponse);
  }
  
  private byte[] buildRequestBuffer() throws IOException
  {
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    serializeIppHeader(byteOut);
    
    serializeAttributes(IppDelimiterTag.BEGIN_OPERATION_ATTRIBUTES, AttributeHelper.getOrderedOperationAttributeArray(operationAttributes), byteOut);
    if (requestedAttributes!= null && !requestedAttributes.isEmpty()) {
      new AttributeWriter().requestedAttributeBytes(requestedAttributes, byteOut);
    }
    
    serializeAttributes(IppDelimiterTag.BEGIN_PRINTER_ATTRIBUTES, printerAttributes.toArray(), byteOut);
    serializeAttributes(IppDelimiterTag.BEGIN_JOB_ATTRIBUTES, jobAttributes.toArray(), byteOut);
    
    serializeIppFooter(byteOut);
    if (data != null) {
      data.transferTo(byteOut);
    }
    return byteOut.toByteArray();
  }

  /**
   *
   */
  private void serializeIppFooter(OutputStream output) throws IOException
  {
    output.write((byte) IppDelimiterTag.END_ATTRIBUTES.getValue());
  }
  
  private void serializeAttributes(IppDelimiterTag beginTag, Attribute[] attributes, OutputStream out) throws UnsupportedEncodingException, IOException
  {
    AttributeWriter attributeWriter = new AttributeWriter();
    
    if (attributes.length > 0) {
      out.write((byte)beginTag.getValue());

      for (Attribute attribute : attributes) {
        attributeWriter.attributeBytes(attribute, out);
      }
    }
  }
  
  /**
   *
   */
  private void serializeIppHeader(OutputStream output) throws IOException
  {
    //Ipp header data according to http://www.ietf.org/rfc/rfc2910.txt

    //The first 2 bytes represent the IPP version number (1.1)
    //major version-number
    output.write((byte) 2);
    //minor version-number
    output.write((byte) 0);
    //2 byte operation id
    IppIoUtils.writeInt2(this.operation.getValue(), output);
    //4 byte request id
    IppIoUtils.writeInt4(this.id, output);
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

  private IppResponse parseResponse(IppConnection.IppConnectionResponse response) throws IOException
  {
    if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
      return null;
    }
    
    InputStream input = response.getResponseBody();
    byte[] header = input.readNBytes(8);
    if (header.length != 8) {
      throw new IOException("Error reading header bytes");
    }
      
    IppStatus status = IppStatus.fromStatusId((int) (header[2] << 8) + (int) header[3]);
    AttributeMap attributes;

    if (input.available() != 0) {
      attributes = new AttributeParser().parseResponse(input);
    }
    else {
      attributes = new AttributeMap();
    }
    return new IppResponseCupsImpl(status, attributes);    
  }
  
  
  static class IppResponseCupsImpl implements IppResponse
  {
    private final IppStatus status;
    private final AttributeMap attributes;

    IppResponseCupsImpl(IppStatus status, AttributeMap attributes)
    {
      this.status = Objects.requireNonNull(status);
      this.attributes = Objects.requireNonNull(attributes);
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
}
