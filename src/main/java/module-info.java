open module jipsi.de.lohndirekt.print
{
  requires java.desktop;
  requires java.net.http;
  requires org.slf4j;
  
  exports jipsi.de.lohndirekt.print;
  exports jipsi.de.lohndirekt.print.api;
  exports jipsi.de.lohndirekt.print.attribute;
  exports jipsi.de.lohndirekt.print.attribute.auth;
  exports jipsi.de.lohndirekt.print.attribute.cups;
  exports jipsi.de.lohndirekt.print.attribute.ipp;
  exports jipsi.de.lohndirekt.print.attribute.ipp.jobdesc;
  exports jipsi.de.lohndirekt.print.attribute.ipp.jobtempl;
  exports jipsi.de.lohndirekt.print.attribute.ipp.printerdesc;
  exports jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.defaults;
  exports jipsi.de.lohndirekt.print.attribute.ipp.printerdesc.supported;
  exports jipsi.de.lohndirekt.print.attribute.undocumented;
  exports jipsi.de.lohndirekt.print.exception;
}
