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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.print.attribute.Attribute;
import javax.print.attribute.EnumSyntax;
import javax.print.attribute.standard.JobStateReason;
import javax.print.attribute.standard.JobStateReasons;
import javax.print.attribute.standard.PrinterStateReason;
import javax.print.attribute.standard.PrinterStateReasons;
import javax.print.attribute.standard.Severity;
import jipsi.de.lohndirekt.print.attribute.ipp.Charset;
import jipsi.de.lohndirekt.print.attribute.ipp.UnknownAttribute;
import jipsi.de.lohndirekt.print.attribute.ipp.jobdesc.LdJobStateReason;
import jipsi.de.lohndirekt.print.exception.EndOfAttributesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bpusch
 *
 */
public final class AttributeParser
{
  private final static Logger LOG = LoggerFactory.getLogger(AttributeParser.class);
  private final static EndOfAttributesException END_OF_ATTRIBUTES_EXCEPTION = new EndOfAttributesException();

/**
   * @param response The input stream containing the response data
   * 
   * @return map of attributes (key -> category, value -> Set with attributes)
   */
  public AttributeMap parseResponse(InputStream response) throws IOException
  {
    AttributeMap attributes = new AttributeMap();
    
    long start = System.currentTimeMillis();
    Attribute lastAttribute = null;
    boolean finished = false;
    response.read();

    while (!finished) {
      try {
        Attribute attribute = parseAttribute(response, lastAttribute);
        if (attribute != null) {
          lastAttribute = attribute;
          attributes.put(attribute);
          LOG.info("parsed attribute({}): {}", attribute.getName(), attribute);

        }
        else {
          LOG.debug("Attribute was null");
        }
      }
      catch (EndOfAttributesException e) {
        finished = true;
        LOG.debug("--- Attribute parsing finished ---");
      }
    }
    long end = System.currentTimeMillis();
    LOG.debug("Parsing took {} ms", (end - start));
    return attributes;
  }  
  
  /**
   * @param name
   * @param values
   * @return
   */
  private Attribute createAttribute(String name, Object[] values)
  {
    IppAttributeName attrName = IppAttributeName.fromAttributeName(name);
    
    if (attrName == null) {
      return new UnknownAttribute(name, values);
    }
    
    Attribute attribute = null;
    Class attrClass = attrName.getAttributeClass();
    Class superClass = attrClass.getSuperclass();
    if (superClass != null) {
      if (superClass.equals(EnumSyntax.class)) {
        try {
          Field[] fields = attrClass.getDeclaredFields();
          for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getType().equals(attrClass)) {
              EnumSyntax attr = (EnumSyntax) field.get(null);
              if (values[0] instanceof String) {
                if (attr.toString().equals(values[0])) {
                  attribute = (Attribute) attr;
                  break;
                }
              }
              else {
                if (attr.getValue() == ((Integer) values[0])) {
                  attribute = (Attribute) attr;
                  break;
                }
              }
            }
          }
        }
        catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
          LOG.error(e.getMessage(), e);
        }
      }
      else {
        Class[] parameters = toClassArray(values);
        try {
          Constructor constructor = attrClass.getDeclaredConstructor(parameters);
          attribute = (Attribute) constructor.newInstance(values);
        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
          LOG.error("Error constructing attribute object for " + name, e);
        }
      }
    }

    return attribute;
  }

  /**
   * @param in The input stream to parse
   * @param lastAttribute The previous attribute read
   * 
   * @return 
   */
  private Attribute parseAttribute(InputStream in, Attribute lastAttribute)
      throws IOException, EndOfAttributesException
  {

    int valueTag;
    while ((valueTag = in.read()) < IppValueTag.UNSUPPORTED_VALUE.getId()) {
      if (valueTag == IppDelimiterTag.END_ATTRIBUTES.getValue()) {
        throw END_OF_ATTRIBUTES_EXCEPTION;
      }
    }

    int nameLength = parseInt2(in);
    //          parse the Attribute-Name
    String name;
    if (nameLength == 0) {
      name = lastAttribute.getName();
    }
    else {
      name = parseString(in, nameLength);
    }

    Object[] values = parseValue(IppValueTag.fromId(valueTag), in);

    if (name.equals(IppAttributeName.PRINTER_STATE_REASONS.getName())) {
      return parsePrinterStateReasons((String) values[0], lastAttribute);
    }
    else if (name.equals(IppAttributeName.JOB_STATE_REASONS.getName())) {
      return parseJobStateReasons(values, lastAttribute);
    }
    else {
      return createAttribute(name, values);
    }
  }

  private String parseString(InputStream in, int nameLength) throws IOException
  {
    return parseString(in, nameLength, Charset.US_ASCII.getValue());
  }

  private String parseNameAndTextString(InputStream in, int nameLength) throws IOException
  {
    return parseString(in, nameLength, AttributeWriter.DEFAULT_CHARSET.getValue());
  }

  /**
   * @param in
   * @param nameLength
   */
  private String parseString(InputStream in, int nameLength, String charsetName) throws IOException
  {
    byte[] bytes = new byte[nameLength];
    in.read(bytes);
    return new String(bytes, charsetName);

  }

  /**
   * @param byteArray
   * @param valueOffset
   * @return
   */
  private Date parseDate(InputStream in) throws IOException
  {
    DecimalFormat twoDigits = new DecimalFormat("00");
    DecimalFormat threeDigits = new DecimalFormat("000");
    DecimalFormat fourDigits = new DecimalFormat("0000");
    //year is encoded in network-byte order
    int year = parseInt2(in);
    int month = in.read();
    int day = in.read();
    int hour = in.read();
    int minute = in.read();
    int second = in.read();
    int deci = in.read();
    int mili = deci * 100;
    char direction = (char) in.read();
    int hoursFromUtc = (int) in.read();
    int minutesFromUtc = (int) in.read();

    String yearString = fourDigits.format((long) year);
    String monthString = twoDigits.format((long) month);
    String dayString = twoDigits.format((long) day);
    String hourString = twoDigits.format((long) hour);
    String minuteString = twoDigits.format((long) minute);
    String secondString = twoDigits.format((long) second);
    String miliString = threeDigits.format((long) mili);
    String timeZone = direction + twoDigits.format((long) hoursFromUtc) + twoDigits.format((long) minutesFromUtc);
    String dateString
        = yearString
          + "-"
          + monthString
          + "-"
          + dayString
          + " "
          + hourString
          + ":"
          + minuteString
          + ":"
          + secondString
          + ":"
          + miliString
          + timeZone;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSZ");
    Date date = null;
    try {
      date = dateFormat.parse(dateString);
    }
    catch (ParseException e) {
      LOG.error(e.getMessage(), e);
    }
    return date;
  }

  private int parseInt4(InputStream in) throws IOException
  {

    //Same parsing as in java.io.DataInput readInt()
    int a = in.read();
    int b = in.read();
    int c = in.read();
    int d = in.read();
    int value = (((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff));
    return value;
  }

  private int parseInt2(InputStream in) throws IOException
  {

    //Same parsing as in java.io.DataInput readInt()
    int a = in.read();
    int b = in.read();
    int value = ((a & 0xff) << 8) | ((b & 0xff));
    return value;
  }

  /**
   * @param string
   * @param lastAttribute
   * @return
   */
  private Attribute parseJobStateReasons(Object[] values, Attribute lastAttribute)
  {
    JobStateReasons reasons;
    if (lastAttribute instanceof JobStateReasons) {
      reasons = (JobStateReasons) lastAttribute;
    }
    else {
      reasons = new JobStateReasons();
    }
    JobStateReason reason = null;
    if (values[0].equals(LdJobStateReason.NONE.toString())) {
      reason = LdJobStateReason.NONE;
    }
    else {
      reason = (JobStateReason) createAttribute(IppAttributeName.JOB_STATE_REASON.getName(), values);
    }
    reasons.add(reason);
    return reasons;
  }

  /**
   * @param reasonAndSeverity
   * @param lastAttribute
   * @return
   */
  private PrinterStateReasons parsePrinterStateReasons(String reasonAndSeverity, Attribute lastAttribute)
  {
    Severity severity = null;
    int severityOffset = 0;
    if ((severityOffset = reasonAndSeverity.indexOf(Severity.ERROR.toString())) > 0) {
      severity = Severity.ERROR;
    }
    else if ((severityOffset = reasonAndSeverity.indexOf(Severity.REPORT.toString())) > 0) {
      severity = Severity.REPORT;
    }
    else if ((severityOffset = reasonAndSeverity.indexOf(Severity.WARNING.toString())) > 0) {
      severity = Severity.WARNING;
    }
    String reasonString;
    if (severityOffset != -1) {
      //subtract 1 for the hyphen
      severityOffset--;
      reasonString = reasonAndSeverity.substring(0, severityOffset - 1);
    }
    else {
      reasonString = reasonAndSeverity;
    }
    Object[] values = new Object[]{reasonString};
    PrinterStateReason reason
        = (PrinterStateReason) createAttribute(IppAttributeName.PRINTER_STATE_REASON.getName(), values);
    PrinterStateReasons reasons;
    if (lastAttribute instanceof PrinterStateReasons) {
      reasons = (PrinterStateReasons) lastAttribute;
    }
    else {
      reasons = new PrinterStateReasons();
    }
    if (reason != null) {
      if (severity == null) {
        severity = Severity.ERROR;
      }
      reasons.put(reason, severity);
    }
    return reasons;
  }

  /**
   * @param byteArray
   * @param valueOffset
   * @param valueLength
   * @return
   */
  private URI parseUri(InputStream in, int valueLength) throws IOException
  {
    try {
      return new URI(parseString(in, valueLength));
    }
    catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param valueTag
   * @param byteArray
   * @param valueOffset
   * @param valueLength
   * @return
   */
  private Object[] parseValue(IppValueTag valueTag, InputStream in) throws IOException
  {
    //Read the specified number of bytes into a buffer. This ensures that we read
    //the exact amount of data for the value.
    int valueLength = parseInt2(in);
    byte[] valueData = in.readNBytes(valueLength);
    if (valueData.length != valueLength) {
      throw new IOException("Could not read requested number of bytes from value stream" + valueLength);
    }
    
    ByteArrayInputStream valueIn = new ByteArrayInputStream(valueData);
    Object[] values = null;
    
    switch (valueTag) {
      case INTEGER:
      case ENUM:
        values = new Object[]{ parseInt4(valueIn) };
        break;
        
      case STRING:
      case TEXT:
      case NAME:
      case MEMBERNAME:
        values = new Object[]{parseNameAndTextString(valueIn, valueLength), Locale.getDefault()};
        break;
        
      case CHARSET:
      case LANGUAGE:
      case MIMETYPE:
      case KEYWORD:
        values = new Object[]{parseString(valueIn, valueLength), Locale.getDefault()};
        break;
        
      case URI:
        values = new Object[]{ parseUri(valueIn, valueLength) };
        break;
        
      case BOOLEAN:
        values = new Object[]{ valueIn.read() };
        break;
        
      case RANGE: {
        Integer lowerBound = parseInt4(valueIn);
        Integer upperBound = parseInt4(valueIn);
        values = new Object[]{lowerBound, upperBound};
        } break;
      
      case DATE:
        values = new Object[]{ parseDate(valueIn) };
        break;
        
      case RESOLUTION:
        int crossFeedDirectionResolution = parseInt4(valueIn);
        int feedDirectionResolution = parseInt4(valueIn);
        byte units = (byte)valueIn.read();
        values = new Object[]{ crossFeedDirectionResolution, feedDirectionResolution, units };
        break;
        
      case NOVALUE:
      case UNKNOWN:
        values = new Object[]{};
        break;
        
      case BEGIN_COLLECTION:
      case END_COLLECTION:
        //not supported so skip
        valueIn.skipNBytes(valueLength);
        values = new Object[]{};
        break;
        
      default:
        throw new UnsupportedOperationException(String.format("Unsupported value type: %s", valueTag.name()));
    }
    
    if (valueIn.available() > 0) {
      LOG.warn("Not all data read for value type {} (len: {}, remaining: {})", valueTag, valueLength, valueIn.available());
    }
    
    return values;
  }

  /**
   * @param values
   * @return
   */
  private Class[] toClassArray(Object[] values)
  {
    Class[] classes = new Class[values.length];
    for (int i = 0; i < values.length; i++) {
      Class clazz = values[i].getClass();
      if (clazz.equals(Integer.class)) {
        clazz = int.class;
      }
      else if (clazz.equals(Byte.class)) {
        clazz = byte.class;
      }
      
      classes[i] = clazz;
    }
    return classes;
  }

}
