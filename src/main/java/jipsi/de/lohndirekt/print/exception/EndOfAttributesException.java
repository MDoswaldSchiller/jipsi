package jipsi.de.lohndirekt.print.exception;

/**
 *
 * Exception used to signal that the EndAttributes tag has been reached while
 * parsing an IppResponse
 *
 * @author bpusch
 *
 */
public class EndOfAttributesException extends Exception
{

  /* (Kein Javadoc)
	 * @see java.lang.Throwable#fillInStackTrace()
   */
  @Override
  public synchronized Throwable fillInStackTrace()
  {
    return this;
  }

}
