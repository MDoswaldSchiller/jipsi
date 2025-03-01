/*
 * Created on 22.01.2004
 *
 *
 */
package jipsi.de.lohndirekt.print.attribute.ipp.jobdesc;

import java.util.Locale;
import javax.print.attribute.standard.JobStateReason;

/**
 * @author bpusch
 *
 *
 */
public class LdJobStateReason extends JobStateReason
{
  public static LdJobStateReason NONE = new LdJobStateReason("none", Locale.getDefault(), -1);

  private final String stringValue;
  
  /**
   * @param value
   */
  private LdJobStateReason(String stringValue, Locale locale, int value)
  {
    super(value);
    this.stringValue = stringValue;
  }

  @Override
  public String toString()
  {
    if (this.stringValue != null) {
      return this.stringValue;
    }
    else {
      return super.toString();
    }
  }

}
