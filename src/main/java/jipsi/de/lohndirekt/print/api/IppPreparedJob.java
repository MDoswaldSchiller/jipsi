/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jipsi.de.lohndirekt.print.api;

import javax.print.attribute.HashPrintJobAttributeSet;

/**
 *
 * @author mdo
 */
public class IppPreparedJob extends IppJob
{

  public IppPreparedJob(IppPrinter printer, HashPrintJobAttributeSet jobAttributes)
  {
    super(printer, jobAttributes);
  }
  
}
