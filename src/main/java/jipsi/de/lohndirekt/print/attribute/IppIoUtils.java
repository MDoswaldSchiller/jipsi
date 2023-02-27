package jipsi.de.lohndirekt.print.attribute;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class contains static helper methods to read and write data for IPP
 * requests and responses.
 *
 * @author mdo
 */
public final class IppIoUtils
{

  private IppIoUtils()
  {
    //not instantiable, only static methods
  }

  public static void writeInt4(int value, OutputStream out) throws IOException
  {
    out.write((byte) ((value & 0xff000000) >> 24));
    out.write((byte) ((value & 0xff0000) >> 16));
    out.write((byte) ((value & 0xff00) >> 8));
    out.write((byte) (value & 0xff));
  }

  public static void writeInt2(int value, OutputStream out) throws IOException
  {
    out.write((byte) ((value & 0xff00) >> 8));
    out.write((byte) (value & 0xff));
  }

}
