package jipsi.de.lohndirekt.print.attribute.ipp.jobdesc;

import java.util.Locale;
import javax.print.attribute.Attribute;
import javax.print.attribute.PrintJobAttribute;
import javax.print.attribute.TextSyntax;

/**
 *
 * @author mdo
 */
public class PrintColorMode extends TextSyntax implements PrintJobAttribute
{
  public static final PrintColorMode COLOR = new PrintColorMode("color", Locale.ENGLISH);
  public static final PrintColorMode MONOCHROME = new PrintColorMode("monochrome", Locale.ENGLISH);
  public static final PrintColorMode BI_LEVEL = new PrintColorMode("bi-level", Locale.ENGLISH);
  
  
  public PrintColorMode(String value, Locale locale)
  {
    super(value, locale);
  }
  
  @Override
  public Class<? extends Attribute> getCategory()
  {
    return this.getClass();
  }

  @Override
  public String getName()
  {
    return "print-color-mode";
  }
  
}
