package jipsi.de.lohndirekt.print.api;

import javax.print.attribute.HashPrintJobAttributeSet;

/**
 *
 * @author mdo
 */
public class IppSimpleJob extends IppJob
{

  public IppSimpleJob(IppPrinter printer, HashPrintJobAttributeSet jobAttributes)
  {
    super(printer, jobAttributes);
  }
  
  
}
